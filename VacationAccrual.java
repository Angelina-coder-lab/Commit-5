package ru.payroll;

/**
 * Отпускные — фиксированная сумма в копейках за период.
 */
public final class VacationAccrual extends Accrual {

    private final long amountKopecks;

    /**
     * @param amountKopecks сумма отпускных, коп. (≥ 0)
     */
    public VacationAccrual(long amountKopecks) {
        super("VACATION", "Отпускные", true);
        if (amountKopecks < 0) {
            throw new IllegalArgumentException("vacation must be ≥ 0: " + amountKopecks);
        }
        this.amountKopecks = amountKopecks;
    }

    @Override
    public long calculate(PayrollContext ctx) {
        return amountKopecks;
    }
}
