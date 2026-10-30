package ru.payroll;

/**
 * Аванс — фиксированная сумма удержания в копейках.
 */
public final class AdvanceDeduction extends Deduction {

    private final long amountKopecks;

    /**
     * @param amountKopecks сумма аванса, коп. (≥ 0)
     */
    public AdvanceDeduction(long amountKopecks) {
        super("ADVANCE", "Аванс", false);
        if (amountKopecks < 0) {
            throw new IllegalArgumentException("advance must be ≥ 0: " + amountKopecks);
        }
        this.amountKopecks = amountKopecks;
    }

    @Override
    public long calculate(PayrollContext ctx) {
        return amountKopecks;
    }
}
