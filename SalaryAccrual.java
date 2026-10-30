package ru.payroll;

import java.util.Objects;

/**
 * Оклад: месячная ставка в копейках по расписанию ставок на дату начала периода.
 * При неполном месяце — пропорционально календарным дням.
 */
public final class SalaryAccrual extends Accrual {

    private final RateSchedule monthlyRate; // копейки за полный месяц

    /**
     * @param monthlyRate расписание месячных окладов (коп.)
     */
    public SalaryAccrual(RateSchedule monthlyRate) {
        super("SALARY", "Оклад", true);
        this.monthlyRate = Objects.requireNonNull(monthlyRate, "monthlyRate");
    }

    @Override
    public long calculate(PayrollContext ctx) {
        var period = ctx.getPeriod();
        long fullMonthRate = monthlyRate.requireOn(period.from(), "оклад");
        // полный календарный месяц ставки
        var month = java.time.YearMonth.from(period.from());
        long daysInMonth = month.lengthOfMonth();
        long periodDays = period.days();
        if (periodDays >= daysInMonth
                && period.from().getDayOfMonth() == 1
                && period.to().equals(month.atEndOfMonth())) {
            return fullMonthRate;
        }
        // пропорция по календарным дням
        return Math.round((double) fullMonthRate * periodDays / daysInMonth);
    }
}
