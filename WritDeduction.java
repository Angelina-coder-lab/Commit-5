package ru.payroll;

/**
 * Исполнительный лист — фиксированная сумма удержания в копейках.
 * Участвует в проверке лимита доли удержаний.
 */
public final class WritDeduction extends Deduction {

    private final long amountKopecks;

    /**
     * @param amountKopecks сумма по исполнительному листу, коп. (≥ 0)
     */
    public WritDeduction(long amountKopecks) {
        super("WRIT", "Исполнительный лист", false);
        if (amountKopecks < 0) {
            throw new IllegalArgumentException("writ must be ≥ 0: " + amountKopecks);
        }
        this.amountKopecks = amountKopecks;
    }

    @Override
    public long calculate(PayrollContext ctx) {
        return amountKopecks;
    }
}
