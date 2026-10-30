package ru.payroll;

/**
 * Больничный — фиксированная сумма в копейках за период.
 * В данной модели не входит в базу НДФЛ (упрощение для демонстрации).
 */
public final class SickAccrual extends Accrual {

    private final long amountKopecks;

    /**
     * @param amountKopecks сумма больничного, коп. (≥ 0)
     */
    public SickAccrual(long amountKopecks) {
        super("SICK", "Больничный", false);
        if (amountKopecks < 0) {
            throw new IllegalArgumentException("sick must be ≥ 0: " + amountKopecks);
        }
        this.amountKopecks = amountKopecks;
    }

    @Override
    public long calculate(PayrollContext ctx) {
        return amountKopecks;
    }
}
