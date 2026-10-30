package ru.payroll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PayrollServiceTest {

    private PayrollService service;
    private Employee emp;
    private RateSchedule salaryRates;
    private RateSchedule ndflRates;
    private RateSchedule nightBp;
    private PayrollPeriod june;

    @BeforeEach
    void setUp() {
        service = new PayrollService(50);
        emp = new Employee("E001", "Иванов И.И.");
        salaryRates = new RateSchedule();
        salaryRates.add(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), Money.ofRubles(100_000));
        ndflRates = new RateSchedule();
        ndflRates.add(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1300);
        nightBp = new RateSchedule();
        nightBp.add(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 2000);
        june = PayrollPeriod.ofMonth(2026, 6);
    }

    // ---------- DateInterval / Period ----------

    @Test
    @DisplayName("DateInterval: конец раньше начала — исключение")
    void intervalInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new DateInterval(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 6, 1)));
    }

    @Test
    @DisplayName("DateInterval: contains и calendarDays")
    void intervalContains() {
        var i = new DateInterval(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        assertTrue(i.contains(LocalDate.of(2026, 6, 15)));
        assertFalse(i.contains(LocalDate.of(2026, 7, 1)));
        assertEquals(30, i.calendarDays());
    }

    @Test
    @DisplayName("PayrollPeriod.ofMonth")
    void periodOfMonth() {
        var p = PayrollPeriod.ofMonth(2026, 2);
        assertEquals(LocalDate.of(2026, 2, 1), p.from());
        assertEquals(LocalDate.of(2026, 2, 28), p.to());
        assertEquals(28, p.days());
    }

    // ---------- Employee equals/hashCode ----------

    @Test
    @DisplayName("Employee: equals/hashCode по id")
    void employeeEquals() {
        var a = new Employee("E001", "Иванов");
        var b = new Employee("E001", "Другое");
        var c = new Employee("E002", "Иванов");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
    }

    // ---------- Money ----------

    @Test
    @DisplayName("Money: of и format")
    void moneyFormat() {
        assertEquals(12345, Money.of(123, 45));
        assertEquals(50000, Money.ofRubles(500));
        assertTrue(Money.format(1_234_56).contains("1 234"));
        assertTrue(Money.format(1_234_56).endsWith(",56"));
    }

    // ---------- RateSchedule ----------

    @Test
    @DisplayName("RateSchedule: ставка на дату")
    void rateOnDate() {
        assertEquals(Money.ofRubles(100_000), salaryRates.requireOn(LocalDate.of(2026, 6, 1), "оклад"));
        assertThrows(PayrollException.class,
                () -> salaryRates.requireOn(LocalDate.of(2025, 1, 1), "оклад"));
    }

    // ---------- Calculation order ----------

    @Test
    @DisplayName("Расчёт: оклад + НДФЛ 13%")
    void salaryAndTax() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new TaxDeduction(ndflRates)
        );
        var slip = service.calculate(emp, june, elements);
        assertEquals(Money.ofRubles(100_000), slip.getAccruedTotal());
        long expectedTax = Math.round(Money.ofRubles(100_000) * 0.13);
        assertEquals(expectedTax, slip.getDeductedTotal());
        assertEquals(Money.ofRubles(100_000) - expectedTax, slip.getNet());
        assertEquals(2, slip.getLines().size());
    }

    @Test
    @DisplayName("Расчёт: премия входит в базу НДФЛ")
    void bonusInTaxBase() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new BonusAccrual(Money.ofRubles(20_000)),
                new TaxDeduction(ndflRates)
        );
        var slip = service.calculate(emp, june, elements);
        assertEquals(Money.ofRubles(120_000), slip.getAccruedTotal());
        long expectedTax = Math.round(Money.ofRubles(120_000) * 0.13);
        assertEquals(expectedTax, slip.getDeductedTotal());
    }

    @Test
    @DisplayName("Расчёт: больничный вне базы НДФЛ")
    void sickOutsideTaxBase() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new SickAccrual(Money.ofRubles(10_000)),
                new TaxDeduction(ndflRates)
        );
        var slip = service.calculate(emp, june, elements);
        assertEquals(Money.ofRubles(110_000), slip.getAccruedTotal());
        long expectedTax = Math.round(Money.ofRubles(100_000) * 0.13); // только оклад
        assertEquals(expectedTax, slip.getDeductedTotal());
    }

    @Test
    @DisplayName("Расчёт: доплата за ночные 20% от оклада")
    void nightSupplement() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new NightAccrual(nightBp, salaryRates),
                new TaxDeduction(ndflRates)
        );
        var slip = service.calculate(emp, june, elements);
        long night = Math.round(Money.ofRubles(100_000) * 0.20);
        assertEquals(Money.ofRubles(100_000) + night, slip.getAccruedTotal());
    }

    @Test
    @DisplayName("Расчёт: аванс и исполнительный лист")
    void advanceAndWrit() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new TaxDeduction(ndflRates),
                new AdvanceDeduction(Money.ofRubles(20_000)),
                new WritDeduction(Money.ofRubles(5_000))
        );
        var slip = service.calculate(emp, june, elements);
        long tax = Math.round(Money.ofRubles(100_000) * 0.13);
        assertEquals(tax + Money.ofRubles(25_000), slip.getDeductedTotal());
        assertTrue(slip.getNet() > 0);
    }

    @Test
    @DisplayName("Лимит удержаний: превышение → DeductionLimitException")
    void deductionLimitExceeded() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new TaxDeduction(ndflRates),
                new AdvanceDeduction(Money.ofRubles(40_000)),
                new WritDeduction(Money.ofRubles(20_000)) // 60k > 50% of 100k
        );
        var ex = assertThrows(DeductionLimitException.class,
                () -> service.calculate(emp, june, elements));
        assertEquals(50, ex.getLimitPercent());
        assertEquals(Money.ofRubles(60_000), ex.getNonTaxDeducted());
    }

    @Test
    @DisplayName("Лимит удержаний: граница 50% — ок")
    void deductionLimitBoundaryOk() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new TaxDeduction(ndflRates),
                new AdvanceDeduction(Money.ofRubles(50_000)) // ровно 50%
        );
        var slip = service.calculate(emp, june, elements);
        assertEquals(Money.ofRubles(50_000) + Math.round(Money.ofRubles(100_000) * 0.13),
                slip.getDeductedTotal());
    }

    @Test
    @DisplayName("Payslip: расшифровка строк")
    void payslipLines() {
        var elements = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new BonusAccrual(Money.ofRubles(5_000)),
                new TaxDeduction(ndflRates)
        );
        var slip = service.calculate(emp, june, elements);
        assertEquals(3, slip.getLines().size());
        assertEquals("SALARY", slip.getLines().get(0).element().code());
        assertEquals(ElementKind.ACCRUAL, slip.getLines().get(0).element().kind());
        assertEquals(ElementKind.DEDUCTION, slip.getLines().get(2).element().kind());
        assertFalse(slip.render().isBlank());
    }

    @Test
    @DisplayName("OCP: новый элемент через интерфейс без правки сервиса")
    void ocpNewElement() {
        PayrollElement grant = new PayrollElement() {
            @Override
            public String code() {
                return "GRANT";
            }

            @Override
            public String title() {
                return "Грант";
            }

            @Override
            public ElementKind kind() {
                return ElementKind.ACCRUAL;
            }

            @Override
            public long calculate(PayrollContext ctx) {
                return Money.ofRubles(8_000);
            }
        };
        var elements = List.of(
                new SalaryAccrual(salaryRates),
                grant,
                new TaxDeduction(ndflRates)
        );
        var slip = service.calculate(emp, june, elements);
        assertEquals(Money.ofRubles(108_000), slip.getAccruedTotal());
        assertTrue(slip.getLines().stream().anyMatch(l -> l.element().code().equals("GRANT")));
    }

    @Test
    @DisplayName("ElementKind: поля и sign")
    void elementKindEnum() {
        assertEquals("Начисление", ElementKind.ACCRUAL.getTitle());
        assertTrue(ElementKind.ACCRUAL.increasesPay());
        assertFalse(ElementKind.DEDUCTION.increasesPay());
        assertEquals("+", ElementKind.ACCRUAL.sign());
        assertEquals("−", ElementKind.DEDUCTION.sign());
    }

    @Test
    @DisplayName("Отрицательные суммы элементов — исключение")
    void negativeAmountsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BonusAccrual(-1));
        assertThrows(IllegalArgumentException.class, () -> new AdvanceDeduction(-1));
        assertThrows(IllegalArgumentException.class, () -> new WritDeduction(-100));
    }

    @Test
    @DisplayName("Пустой список элементов — нулевой листок")
    void emptyElements() {
        var slip = service.calculate(emp, june, List.of());
        assertEquals(0, slip.getAccruedTotal());
        assertEquals(0, slip.getDeductedTotal());
        assertEquals(0, slip.getNet());
        assertTrue(slip.getLines().isEmpty());
    }

    @Test
    @DisplayName("Инкапсуляция: lines payslip неизменяемы")
    void payslipLinesImmutable() {
        var slip = service.calculate(emp, june, List.of(new SalaryAccrual(salaryRates)));
        assertThrows(UnsupportedOperationException.class,
                () -> slip.getLines().add(null));
    }
}
