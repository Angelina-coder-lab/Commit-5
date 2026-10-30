package ru.payroll;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Интервал дат [from, to] включительно. Конец не раньше начала.
 *
 * @param from дата начала (включительно)
 * @param to   дата конца (включительно)
 */
public record DateInterval(LocalDate from, LocalDate to) {

    public DateInterval {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "Конец интервала не может быть раньше начала: " + from + " .. " + to);
        }
    }

    /**
     * @param date проверяемая дата
     * @return {@code true}, если дата попадает в интервал
     */
    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return !date.isBefore(from) && !date.isAfter(to);
    }

    /**
     * Пересекается ли с другим интервалом.
     */
    public boolean intersects(DateInterval other) {
        Objects.requireNonNull(other, "other");
        return !to.isBefore(other.from) && !other.to.isBefore(from);
    }

    /**
     * Число календарных дней в интервале (включительно).
     */
    public long calendarDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
    }

    @Override
    public String toString() {
        return from + " — " + to;
    }
}
