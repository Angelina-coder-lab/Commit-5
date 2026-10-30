package ru.payroll;

/**
 * Премия — фиксированная сумма в копейках (задаётся при создании элемента
 * или через ставку на период).
 */
public final class BonusAccrual extends Accrual {

    private final long amountKopecks;

    /**
     * @param amountKopecks сумма премии в копейках (≥ 0)
     */
    public BonusAccrual(long amountKopecks) {
        super("BONUS", "Премия", true);
        if (amountKopecks < 0) {
            throw new IllegalArgumentException("bonus must be ≥ 0: " + amountKopecks);
        }
        this.amountKopecks = amountKopecks;
    }

    @Override
    public long calculate(PayrollContext ctx) {
        return amountKopecks;
    }
}
