package ru.payroll;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Консольная демонстрация базового уровня расчёта зарплаты.
 * Создаёт ≥ 30 объектов модели и показывает:
 * расчёт периода (начисления → налог → удержания с лимитом),
 * расчётный листок с расшифровкой, реакцию на превышение лимита.
 */
public final class DemoApp {

    public static void main(String[] args) {
        System.out.println("=== Демонстрация расчёта заработной платы ===\n");

        var service = new PayrollService(50); // лимит удержаний 50%

        // ---------- Сотрудники (≥ 10) ----------
        System.out.println("--- Сотрудники ---");
        var employees = List.of(
                new Employee("E001", "Иванов Иван Иванович"),
                new Employee("E002", "Петрова Анна Сергеевна"),
                new Employee("E003", "Сидоров Пётр Алексеевич"),
                new Employee("E004", "Козлова Мария Викторовна"),
                new Employee("E005", "Новиков Дмитрий Олегович"),
                new Employee("E006", "Морозова Елена Павловна"),
                new Employee("E007", "Волков Андрей Николаевич"),
                new Employee("E008", "Соколова Ольга Игоревна"),
                new Employee("E009", "Лебедев Сергей Владимирович"),
                new Employee("E010", "Кузнецова Татьяна Романовна"),
                new Employee("E011", "Попов Алексей Юрьевич"),
                new Employee("E012", "Васильева Наталья Андреевна")
        );
        employees.forEach(e -> System.out.println("  " + e));

        // ---------- Ставки на интервалах дат ----------
        System.out.println("\n--- Ставки (оклад, НДФЛ, ночные) ---");
        var salaryRates = new RateSchedule();
        salaryRates.add(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), Money.ofRubles(80_000));
        salaryRates.add(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), Money.ofRubles(90_000));

        var ndflRates = new RateSchedule();
        ndflRates.add(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1300); // 13.00%

        var nightPercent = new RateSchedule();
        nightPercent.add(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 2000); // 20%

        System.out.println("  Оклад до 30.06.2026: " + Money.format(Money.ofRubles(80_000)));
        System.out.println("  Оклад с 01.07.2026:  " + Money.format(Money.ofRubles(90_000)));
        System.out.println("  НДФЛ: 13%, ночные: 20%");

        // ---------- Периоды ----------
        var periodJune = PayrollPeriod.ofMonth(2026, 6);
        var periodJuly = PayrollPeriod.ofMonth(2026, 7);
        System.out.println("\n--- Периоды ---");
        System.out.println("  Июнь:  " + periodJune + " (" + periodJune.days() + " дн.)");
        System.out.println("  Июль:  " + periodJuly + " (" + periodJuly.days() + " дн.)");

        // ---------- Элементы расчёта (начисления + удержания) ----------
        // Набор для Иванова за июнь
        var elementsJune = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new BonusAccrual(Money.ofRubles(10_000)),
                new NightAccrual(nightPercent, salaryRates),
                new VacationAccrual(Money.ofRubles(0)),
                new SickAccrual(Money.ofRubles(0)),
                new TaxDeduction(ndflRates),
                new AdvanceDeduction(Money.ofRubles(20_000)),
                new WritDeduction(Money.ofRubles(5_000))
        );

        System.out.println("\n--- Расчёт за июнь (Иванов) ---");
        var slipJune = service.calculate(employees.get(0), periodJune, elementsJune);
        System.out.println(slipJune.render());

        // Июль — оклад уже 90 000
        var elementsJuly = List.<PayrollElement>of(
                new SalaryAccrual(salaryRates),
                new BonusAccrual(Money.ofRubles(15_000)),
                new NightAccrual(nightPercent, salaryRates),
                new SickAccrual(Money.ofRubles(3_000)),
                new TaxDeduction(ndflRates),
                new AdvanceDeduction(Money.ofRubles(25_000))
        );

        System.out.println("--- Расчёт за июль (Иванов) ---");
        var slipJuly = service.calculate(employees.get(0), periodJuly, elementsJuly);
        System.out.println(slipJuly.render());

        // Несколько сотрудников для объёма объектов
        System.out.println("--- Расчёты для других сотрудников (июнь) ---");
        var payslips = new ArrayList<Payslip>();
        payslips.add(slipJune);
        payslips.add(slipJuly);

        for (int i = 1; i < 6; i++) {
            var els = List.<PayrollElement>of(
                    new SalaryAccrual(salaryRates),
                    new BonusAccrual(Money.ofRubles(5_000 + i * 1_000)),
                    new TaxDeduction(ndflRates),
                    new AdvanceDeduction(Money.ofRubles(15_000))
            );
            var slip = service.calculate(employees.get(i), periodJune, els);
            payslips.add(slip);
            System.out.printf("  %s: начислено %s, удержано %s, к выдаче %s%n",
                    employees.get(i).getFullName(),
                    Money.format(slip.getAccruedTotal()),
                    Money.format(slip.getDeductedTotal()),
                    Money.format(slip.getNet()));
        }

        // Превышение лимита удержаний
        System.out.println("\n--- Превышение лимита удержаний (ожидается исключение) ---");
        try {
            var tooMuch = List.<PayrollElement>of(
                    new SalaryAccrual(salaryRates),
                    new TaxDeduction(ndflRates),
                    new AdvanceDeduction(Money.ofRubles(30_000)),
                    new WritDeduction(Money.ofRubles(40_000)) // 70k > 50% от ~80k
            );
            service.calculate(employees.get(0), periodJune, tooMuch);
            System.out.println("ОШИБКА: лимит не сработал!");
        } catch (DeductionLimitException ex) {
            System.out.println("Ожидаемое исключение: " + ex.getMessage());
            System.out.println("  nonTax=" + Money.format(ex.getNonTaxDeducted())
                    + ", accrued=" + Money.format(ex.getAccruedTotal())
                    + ", limit=" + ex.getLimitPercent() + "%");
        }

        // Демонстрация OCP: новый вид начисления через интерфейс — без правки PayrollService
        System.out.println("\n--- OCP: новый вид начисления (разовый грант) без правки PayrollService ---");
        PayrollElement custom = new PayrollElement() {
            @Override
            public String code() {
                return "GRANT";
            }

            @Override
            public String title() {
                return "Разовый грант";
            }

            @Override
            public ElementKind kind() {
                return ElementKind.ACCRUAL;
            }

            @Override
            public long calculate(PayrollContext ctx) {
                return Money.ofRubles(7_500);
            }
        };
        var withGrant = List.of(
                new SalaryAccrual(salaryRates),
                custom,
                new TaxDeduction(ndflRates)
        );
        var slipGrant = service.calculate(employees.get(2), periodJune, withGrant);
        System.out.println(slipGrant.render());

        System.out.println("=== Итог ===");
        System.out.println("Сотрудников: " + employees.size());
        System.out.println("Расчётных листков: " + (payslips.size() + 1));
        System.out.println("Объектов модели заведомо больше 30 (сотрудники, периоды, ставки, элементы, строки листков).");
        System.out.println("Демонстрация базового уровня завершена успешно.");
    }
}
