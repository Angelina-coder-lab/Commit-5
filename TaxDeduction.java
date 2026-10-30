package ru.payroll;

import java.util.Objects;

/**
 * НДФЛ: процент от налоговой базы (базисные пункты, 1300 = 13.00%).
 * Ставка берётся из расписания на дату начала периода.
 */
public final class TaxDeduction extends Deduction {

    private final RateSchedule rateBp;

    /**
     * @param rateBp расписание ставки НДФЛ в базисных пунктах
     */
    public TaxDeduction(RateSchedule rateBp) {
        super("NDFL", "НДФЛ", true);
        this.rateBp = Objects.requireNonNull(rateBp, "rateBp");
    }

    @Override
    public long calculate(PayrollContext ctx) {
        long base = ctx.getTaxBase();
        long bp = rateBp.requireOn(ctx.getPeriod().from(), "НДФЛ");
        return Math.round(base * (bp / 10_000.0));
    }
}
