package ru.payroll;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ставки/лимиты, действующие на интервалах дат.
 * Все значения — целые копейки (или базисные пункты для процентов: 1300 = 13.00%).
 */
public final class RateSchedule {

    /**
     * Одна запись: значение действует на интервале.
     *
     * @param interval интервал действия
     * @param value    значение в копейках или в базисных пунктах (для %)
     */
    public record Entry(DateInterval interval, long value) {
        public Entry {
            Objects.requireNonNull(interval, "interval");
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    /**
     * Добавляет ставку на интервал.
     */
    public void add(DateInterval interval, long value) {
        Objects.requireNonNull(interval, "interval");
        entries.add(new Entry(interval, value));
    }

    /**
     * Добавляет ставку на интервал дат.
     */
    public void add(LocalDate from, LocalDate to, long value) {
        add(new DateInterval(from, to), value);
    }

    /**
     * Находит значение, действующее на указанную дату.
     *
     * @return empty, если на дату ставки нет
     */
    public Optional<Long> valueOn(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return entries.stream()
                .filter(e -> e.interval().contains(date))
                .map(Entry::value)
                .findFirst();
    }

    /**
     * Значение на дату или исключение, если ставки нет.
     */
    public long requireOn(LocalDate date, String what) {
        return valueOn(date).orElseThrow(() ->
                new PayrollException("Нет ставки «" + what + "» на дату " + date));
    }

    /**
     * @return неизменяемый список записей
     */
    public List<Entry> entries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }
}
