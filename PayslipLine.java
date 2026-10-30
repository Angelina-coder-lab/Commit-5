package ru.payroll;

import java.util.Objects;

/**
 * Одна строка расчётного листка.
 *
 * @param element элемент расчёта
 * @param amount  сумма в копейках (всегда ≥ 0; знак задаётся kind)
 * @param note    пояснение (может быть пустым)
 */
public record PayslipLine(PayrollElement element, long amount, String note) {

    public PayslipLine {
        Objects.requireNonNull(element, "element");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be ≥ 0: " + amount);
        }
        note = note == null ? "" : note;
    }

    /**
     * Форматированная строка для вывода.
     */
    public String format() {
        return String.format("%s %-28s %10s  %s",
                element.kind().sign(),
                element.title() + " [" + element.code() + "]",
                Money.format(amount),
                note.isEmpty() ? "" : "(" + note + ")");
    }
}
