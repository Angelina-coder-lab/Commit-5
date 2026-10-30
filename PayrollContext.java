package ru.payroll;

import java.util.Objects;

/**
 * Контекст одного прохода расчёта периода.
 * Накапливает базу начислений (для налога) и суммы удержаний.
 */
public final class PayrollContext {

    private final Employee employee;
    private final PayrollPeriod period;

    private long accruedTotal;      // сумма всех начислений, коп.
    private long taxBase;           // база для НДФЛ, коп.
    private long deductedTotal;     // сумма всех удержаний, коп.
    private long nonTaxDeducted;    // удержания кроме налога (для лимита доли)

    /**
     * @param employee сотрудник
     * @param period   расчётный период
     */
    public PayrollContext(Employee employee, PayrollPeriod period) {
        this.employee = Objects.requireNonNull(employee, "employee");
        this.period = Objects.requireNonNull(period, "period");
    }

    /** @return сотрудник */
    public Employee getEmployee() {
        return employee;
    }

    /** @return период */
    public PayrollPeriod getPeriod() {
        return period;
    }

    /** @return сумма начислений, коп. */
    public long getAccruedTotal() {
        return accruedTotal;
    }

    /** @return налоговая база, коп. */
    public long getTaxBase() {
        return taxBase;
    }

    /** @return сумма удержаний, коп. */
    public long getDeductedTotal() {
        return deductedTotal;
    }

    /** @return удержания без налога, коп. */
    public long getNonTaxDeducted() {
        return nonTaxDeducted;
    }

    /**
     * Учитывает результат начисления.
     *
     * @param amount        сумма, коп.
     * @param includedInTax входит ли в базу НДФЛ
     */
    public void addAccrual(long amount, boolean includedInTax) {
        if (amount < 0) {
            throw new IllegalArgumentException("accrual amount must be ≥ 0: " + amount);
        }
        accruedTotal += amount;
        if (includedInTax) {
            taxBase += amount;
        }
    }

    /**
     * Учитывает удержание.
     *
     * @param amount сумма, коп.
     * @param isTax  является ли налогом
     */
    public void addDeduction(long amount, boolean isTax) {
        if (amount < 0) {
            throw new IllegalArgumentException("deduction amount must be ≥ 0: " + amount);
        }
        deductedTotal += amount;
        if (!isTax) {
            nonTaxDeducted += amount;
        }
    }

    /** @return к выдаче (начислено − удержано), коп. */
    public long net() {
        return accruedTotal - deductedTotal;
    }
}
