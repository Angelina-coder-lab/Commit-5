package ru.payroll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Расчётный листок с расшифровкой каждой строки.
 */
public final class Payslip {

    private final Employee employee;
    private final PayrollPeriod period;
    private final List<PayslipLine> lines;
    private final long accruedTotal;
    private final long deductedTotal;
    private final long net;

    /**
     * @param employee      сотрудник
     * @param period        период
     * @param lines         строки (копируются)
     * @param accruedTotal  итого начислено, коп.
     * @param deductedTotal итого удержано, коп.
     * @param net           к выдаче, коп.
     */
    public Payslip(Employee employee, PayrollPeriod period, List<PayslipLine> lines,
                   long accruedTotal, long deductedTotal, long net) {
        this.employee = Objects.requireNonNull(employee, "employee");
        this.period = Objects.requireNonNull(period, "period");
        this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        this.accruedTotal = accruedTotal;
        this.deductedTotal = deductedTotal;
        this.net = net;
    }

    /** @return сотрудник */
    public Employee getEmployee() {
        return employee;
    }

    /** @return период */
    public PayrollPeriod getPeriod() {
        return period;
    }

    /**
     * @return неизменяемый список строк
     */
    public List<PayslipLine> getLines() {
        return lines;
    }

    /** @return начислено, коп. */
    public long getAccruedTotal() {
        return accruedTotal;
    }

    /** @return удержано, коп. */
    public long getDeductedTotal() {
        return deductedTotal;
    }

    /** @return к выдаче, коп. */
    public long getNet() {
        return net;
    }

    /**
     * Текстовое представление листка.
     */
    public String render() {
        var sb = new StringBuilder();
        sb.append("=== Расчётный листок ===\n");
        sb.append("Сотрудник: ").append(employee).append('\n');
        sb.append("Период:    ").append(period).append('\n');
        sb.append("----------------------------------------------\n");
        for (var line : lines) {
            sb.append(line.format()).append('\n');
        }
        sb.append("----------------------------------------------\n");
        sb.append(String.format("Итого начислено:  %s\n", Money.format(accruedTotal)));
        sb.append(String.format("Итого удержано:   %s\n", Money.format(deductedTotal)));
        sb.append(String.format("К выдаче:         %s\n", Money.format(net)));
        return sb.toString();
    }

    @Override
    public String toString() {
        return render();
    }
}
