package ru.payroll;

/**
 * Базовый класс удержания. Закрытая иерархия.
 */
public abstract sealed class Deduction implements PayrollElement
        permits TaxDeduction, AdvanceDeduction, WritDeduction {

    private final String code;
    private final String title;
    private final boolean tax;

    /**
     * @param code  код
     * @param title название
     * @param tax   является ли налогом (не учитывается в лимите «доли удержаний»)
     */
    protected Deduction(String code, String title, boolean tax) {
        this.code = code;
        this.title = title;
        this.tax = tax;
    }

    @Override
    public final String code() {
        return code;
    }

    @Override
    public final String title() {
        return title;
    }

    @Override
    public final ElementKind kind() {
        return ElementKind.DEDUCTION;
    }

    /** @return {@code true}, если это налог */
    public final boolean isTax() {
        return tax;
    }
}
