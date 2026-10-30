package ru.payroll;

/**
 * Удержания (кроме налога) превысили допустимую долю от начислений.
 */
public class DeductionLimitException extends PayrollException {

    private final long nonTaxDeducted;
    private final long accruedTotal;
    private final int limitPercent;

    /**
     * @param nonTaxDeducted сумма неудерживаемых налогом удержаний, коп.
     * @param accruedTotal   сумма начислений, коп.
     * @param limitPercent   лимит в процентах
     */
    public DeductionLimitException(long nonTaxDeducted, long accruedTotal, int limitPercent) {
        super(String.format(
                "Удержания %d коп. превышают %d%% от начислений %d коп.",
                nonTaxDeducted, limitPercent, accruedTotal));
        this.nonTaxDeducted = nonTaxDeducted;
        this.accruedTotal = accruedTotal;
        this.limitPercent = limitPercent;
    }

    /** @return сумма удержаний без налога, коп. */
    public long getNonTaxDeducted() {
        return nonTaxDeducted;
    }

    /** @return сумма начислений, коп. */
    public long getAccruedTotal() {
        return accruedTotal;
    }

    /** @return лимит в процентах */
    public int getLimitPercent() {
        return limitPercent;
    }
}
