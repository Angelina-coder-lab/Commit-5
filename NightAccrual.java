package ru.payroll;

import java.util.Objects;

/**
 * Доплата за ночные: процент от оклада (в базисных пунктах: 2000 = 20.00%).
 * Процент берётся из расписания на дату начала периода.
 */
public final class NightAccrual extends Accrual {

    private final RateSchedule percentBp; // базисные пункты
    private final RateSchedule monthlySalary;

    /**
     * @param percentBp      расписание процента доплаты (базисные пункты)
     * @param monthlySalary  расписание оклада для базы расчёта
     */
    public NightAccrual(RateSchedule percentBp, RateSchedule monthlySalary) {
        super("NIGHT", "Доплата за ночные", true);
        this.percentBp = Objects.requireNonNull(percentBp, "percentBp");
        this.monthlySalary = Objects.requireNonNull(monthlySalary, "monthlySalary");
    }

    @Override
    public long calculate(PayrollContext ctx) {
        var period = ctx.getPeriod();
        long salary = monthlySalary.requireOn(period.from(), "оклад для ночных");
        long bp = percentBp.requireOn(period.from(), "процент ночных");
        return Math.round(salary * (bp / 10_000.0));
    }
}
