package ru.payroll;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Расчётный период — календарный месяц или произвольный интервал дат.
 *
 * @param interval интервал дат периода
 */
public record PayrollPeriod(DateInterval interval) {

    public PayrollPeriod {
        Objects.requireNonNull(interval, "interval");
    }

    /**
     * Период по календарному месяцу.
     */
    public static PayrollPeriod ofMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return new PayrollPeriod(new DateInterval(ym.atDay(1), ym.atEndOfMonth()));
    }

    /**
     * Период по явным датам.
     */
    public static PayrollPeriod of(LocalDate from, LocalDate to) {
        return new PayrollPeriod(new DateInterval(from, to));
    }

    /** @return дата начала периода */
    public LocalDate from() {
        return interval.from();
    }

    /** @return дата конца периода */
    public LocalDate to() {
        return interval.to();
    }

    /** @return число календарных дней в периоде */
    public long days() {
        return interval.calendarDays();
    }

    @Override
    public String toString() {
        return interval.toString();
    }
}
