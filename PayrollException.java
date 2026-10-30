package ru.payroll;

/**
 * Исключение предметной области расчёта зарплаты.
 */
public class PayrollException extends RuntimeException {

    /**
     * @param message описание ошибки
     */
    public PayrollException(String message) {
        super(message);
    }

    /**
     * @param message описание
     * @param cause   причина
     */
    public PayrollException(String message, Throwable cause) {
        super(message, cause);
    }
}
