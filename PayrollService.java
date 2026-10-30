package ru.payroll;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис расчёта зарплаты.
 * Порядок применения (базовый уровень):
 * <ol>
 *   <li>все начисления;</li>
 *   <li>налог от базы;</li>
 *   <li>прочие удержания с проверкой лимита доли.</li>
 * </ol>
 * Список элементов задаётся снаружи — новый вид начисления/удержания
 * добавляется без правки этого класса (OCP).
 */
public class PayrollService {

    /**
     * Максимальная доля неудерживаемых налогом удержаний от суммы начислений, %.
     * По умолчанию 50 (типичное ограничение по исполнительным листам).
     */
    private final int deductionLimitPercent;

    /**
     * @param deductionLimitPercent лимит доли удержаний (1..100)
     */
    public PayrollService(int deductionLimitPercent) {
        if (deductionLimitPercent < 1 || deductionLimitPercent > 100) {
            throw new IllegalArgumentException(
                    "deductionLimitPercent must be 1..100: " + deductionLimitPercent);
        }
        this.deductionLimitPercent = deductionLimitPercent;
    }

    /** Лимит 50%. */
    public PayrollService() {
        this(50);
    }

    /**
     * Рассчитывает период для сотрудника по списку элементов.
     *
     * @param employee сотрудник
     * @param period   период
     * @param elements элементы в порядке объявления (сначала начисления, потом налог, потом удержания —
     *                 сервис сам разделяет по {@link ElementKind} и типу налога)
     * @return расчётный листок
     * @throws DeductionLimitException если доля удержаний превышена
     * @throws PayrollException        при отсутствии ставок и др.
     */
    public Payslip calculate(Employee employee, PayrollPeriod period, List<PayrollElement> elements) {
        Objects.requireNonNull(employee, "employee");
        Objects.requireNonNull(period, "period");
        Objects.requireNonNull(elements, "elements");

        var ctx = new PayrollContext(employee, period);
        var lines = new ArrayList<PayslipLine>();

        // 1. Начисления
        for (var el : elements) {
            if (el.kind() != ElementKind.ACCRUAL) {
                continue;
            }
            long amount = el.calculate(ctx);
            boolean inTax = el.includedInTaxBase();
            ctx.addAccrual(amount, inTax);
            lines.add(new PayslipLine(el, amount, inTax ? "в базе НДФЛ" : "вне базы НДФЛ"));
        }

        // 2. Налог от базы
        for (var el : elements) {
            if (!(el instanceof Deduction d) || !d.isTax()) {
                continue;
            }
            long amount = el.calculate(ctx);
            ctx.addDeduction(amount, true);
            lines.add(new PayslipLine(el, amount,
                    "база " + Money.format(ctx.getTaxBase())));
        }

        // 3. Прочие удержания
        for (var el : elements) {
            if (!(el instanceof Deduction d) || d.isTax()) {
                continue;
            }
            long amount = el.calculate(ctx);
            ctx.addDeduction(amount, false);
            lines.add(new PayslipLine(el, amount, ""));
        }

        // Проверка лимита доли неудерживаемых налогом удержаний
        if (ctx.getAccruedTotal() > 0) {
            long maxAllowed = ctx.getAccruedTotal() * deductionLimitPercent / 100;
            if (ctx.getNonTaxDeducted() > maxAllowed) {
                throw new DeductionLimitException(
                        ctx.getNonTaxDeducted(), ctx.getAccruedTotal(), deductionLimitPercent);
            }
        } else if (ctx.getNonTaxDeducted() > 0) {
            throw new DeductionLimitException(
                    ctx.getNonTaxDeducted(), ctx.getAccruedTotal(), deductionLimitPercent);
        }

        return new Payslip(employee, period, lines,
                ctx.getAccruedTotal(), ctx.getDeductedTotal(), ctx.net());
    }

    /** @return лимит доли удержаний, % */
    public int getDeductionLimitPercent() {
        return deductionLimitPercent;
    }
}
