package ru.payroll;

/**
 * Вид элемента расчёта. Перечисление с полями и поведением констант.
 */
public enum ElementKind {
    ACCRUAL("Начисление", true),
    DEDUCTION("Удержание", false);

    private final String title;
    private final boolean increasesPay;

    ElementKind(String title, boolean increasesPay) {
        this.title = title;
        this.increasesPay = increasesPay;
    }

    /** @return человекочитаемое название */
    public String getTitle() {
        return title;
    }

    /** @return {@code true}, если элемент увеличивает выплату */
    public boolean increasesPay() {
        return increasesPay;
    }

    /**
     * Знак для отображения суммы в расчётном листке.
     */
    public String sign() {
        return switch (this) {
            case ACCRUAL -> "+";
            case DEDUCTION -> "−";
        };
    }
}
