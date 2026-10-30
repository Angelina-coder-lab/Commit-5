package ru.payroll;

/**
 * Базовый класс начисления. Закрытая иерархия.
 * Полиморфный {@link #calculate(PayrollContext)} — каждая реализация считает по-своему.
 */
public abstract sealed class Accrual implements PayrollElement
        permits SalaryAccrual, BonusAccrual, NightAccrual, VacationAccrual, SickAccrual {

    private final String code;
    private final String title;
    private final boolean includedInTaxBase;

    /**
     * @param code              код
     * @param title             название
     * @param includedInTaxBase входит ли в базу НДФЛ
     */
    protected Accrual(String code, String title, boolean includedInTaxBase) {
        this.code = code;
        this.title = title;
        this.includedInTaxBase = includedInTaxBase;
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
        return ElementKind.ACCRUAL;
    }

    /**
     * @return {@code true}, если сумма входит в налоговую базу
     */
    @Override
    public final boolean includedInTaxBase() {
        return includedInTaxBase;
    }
}
