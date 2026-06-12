package com.financeapp.backend.service.analysis;

import com.financeapp.backend.dto.analysis.AnalysisOverviewResponse;
import com.financeapp.backend.dto.analysis.FinancialTwinSimulatorResponse;
import com.financeapp.backend.dto.analysis.HiddenExpenseDetectorResponse;
import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.entity.Income;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.IncomeRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public AnalysisServiceImpl(ExpenseRepository expenseRepository,
                               IncomeRepository incomeRepository,
                               UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AnalysisOverviewResponse getAnalysisOverview(String username, double threshold) {
        User user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        double effectiveThreshold = threshold > 0 ? threshold : 2000.0;
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        List<Expense> currentMonthExpenses = expenseRepository.findMonthlyExpenses(
                user,
                currentMonth.getMonthValue(),
                currentMonth.getYear()
        );
        List<Income> allIncome = incomeRepository.findByUserUsername(username);
        List<Expense> allExpenses = expenseRepository.findByUser(user);

        HiddenExpenseDetectorResponse hiddenExpenseDetector = buildHiddenExpenseDetector(
                currentMonthExpenses,
                effectiveThreshold,
                currentMonth
        );

        FinancialTwinSimulatorResponse financialTwinSimulator = buildFinancialTwinSimulator(allIncome, allExpenses);

        return new AnalysisOverviewResponse(
                hiddenExpenseDetector,
                financialTwinSimulator,
                java.time.LocalDateTime.now()
        );
    }

    private HiddenExpenseDetectorResponse buildHiddenExpenseDetector(List<Expense> currentMonthExpenses,
                                                                     double threshold,
                                                                     YearMonth currentMonth) {
        double totalMonthlyExpense = currentMonthExpenses.stream()
                .mapToDouble(expense -> expense.getAmount() == null ? 0.0 : expense.getAmount())
                .sum();

        List<Expense> microExpenses = currentMonthExpenses.stream()
                .filter(expense -> expense.getAmount() != null && expense.getAmount() < threshold)
                .collect(Collectors.toList());

        double microTransactionTotal = microExpenses.stream()
                .mapToDouble(expense -> expense.getAmount() == null ? 0.0 : expense.getAmount())
                .sum();

        int microTransactionCount = microExpenses.size();
        double microTransactionSharePercent = totalMonthlyExpense > 0
                ? (microTransactionTotal / totalMonthlyExpense) * 100.0
                : 0.0;

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : microExpenses) {
            String category = expense.getCategory() == null ? "Other" : expense.getCategory().trim();
            categoryTotals.merge(category, expense.getAmount() == null ? 0.0 : expense.getAmount(), Double::sum);
        }

        List<HiddenExpenseDetectorResponse.CategorySpend> topCategories = categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> new HiddenExpenseDetectorResponse.CategorySpend(
                        entry.getKey(),
                        entry.getValue(),
                        microTransactionTotal > 0 ? (entry.getValue() / microTransactionTotal) * 100.0 : 0.0
                ))
                .collect(Collectors.toList());

        String warningLevel;
        if (microTransactionSharePercent >= 15) {
            warningLevel = "High";
        } else if (microTransactionSharePercent >= 8) {
            warningLevel = "Medium";
        } else {
            warningLevel = "Low";
        }

        String thresholdLabel = String.format(Locale.ENGLISH, "₹%,.0f", threshold);
        String insight = String.format(
                Locale.ENGLISH,
                "You spent %s on purchases below %s this month.",
                money(microTransactionTotal),
                thresholdLabel
        );
        String savingsOpportunityInsight = String.format(
                Locale.ENGLISH,
                "Reducing these expenses by 25%% could save %s per month.",
                money(microTransactionTotal * 0.25)
        );

        return new HiddenExpenseDetectorResponse(
                threshold,
                totalMonthlyExpense,
                microTransactionTotal,
                microTransactionCount,
                microTransactionSharePercent,
                thresholdLabel,
                warningLevel,
                insight,
                savingsOpportunityInsight,
                topCategories
        );
    }

    private FinancialTwinSimulatorResponse buildFinancialTwinSimulator(List<Income> allIncome,
                                                                       List<Expense> allExpenses) {
        Map<YearMonth, Double> incomeByMonth = new LinkedHashMap<>();
        Map<YearMonth, Double> expenseByMonth = new LinkedHashMap<>();

        LocalDate firstActivity = determineFirstActivityDate(allIncome, allExpenses);
        LocalDate lastActivity = determineLastActivityDate(allIncome, allExpenses);

        if (firstActivity == null || lastActivity == null) {
            FinancialTwinSimulatorResponse.ProjectionResponse emptyProjection = new FinancialTwinSimulatorResponse.ProjectionResponse(
                    "No Data",
                    0,
                    0,
                    0
            );
            return new FinancialTwinSimulatorResponse(
                    0,
                    0,
                    0,
                    emptyProjection,
                    emptyProjection,
                    emptyProjection,
                    0,
                    "No financial history available yet.",
                    "Add income and expenses to activate the simulator.",
                    List.of()
            );
        }

        YearMonth cursor = YearMonth.from(firstActivity);
        YearMonth finalMonth = YearMonth.from(lastActivity);
        while (!cursor.isAfter(finalMonth)) {
            incomeByMonth.put(cursor, 0.0);
            expenseByMonth.put(cursor, 0.0);
            cursor = cursor.plusMonths(1);
        }

        for (Income income : allIncome) {
            if (income.getDate() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(income.getDate());
            incomeByMonth.merge(month, income.getAmount() == null ? 0.0 : income.getAmount(), Double::sum);
        }

        for (Expense expense : allExpenses) {
            if (expense.getDate() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(expense.getDate());
            expenseByMonth.merge(month, expense.getAmount() == null ? 0.0 : expense.getAmount(), Double::sum);
        }

        int observedMonths = Math.max(incomeByMonth.size(), expenseByMonth.size());
        if (observedMonths <= 0) {
            observedMonths = 1;
        }

        double totalIncome = incomeByMonth.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = expenseByMonth.values().stream().mapToDouble(Double::doubleValue).sum();
        double averageMonthlyIncome = totalIncome / observedMonths;
        double averageMonthlyExpense = totalExpense / observedMonths;
        double averageMonthlySavings = averageMonthlyIncome - averageMonthlyExpense;

        double optimizedMonthlyExpense = averageMonthlyExpense * 0.9;
        double savingsImprovementMonthly = averageMonthlyExpense - optimizedMonthlyExpense;

        FinancialTwinSimulatorResponse.ProjectionResponse currentPath = buildProjectionResponse(
                "Current Path",
                averageMonthlySavings,
                1,
                3,
                5
        );
        FinancialTwinSimulatorResponse.ProjectionResponse optimizedPath = buildProjectionResponse(
                "Optimized Path",
                averageMonthlyIncome - optimizedMonthlyExpense,
                1,
                3,
                5
        );
        FinancialTwinSimulatorResponse.ProjectionResponse savingsGrowthPath = buildProjectionResponse(
                "Savings Growth Path",
                averageMonthlySavings * 1.1,
                1,
                3,
                5
        );

        double projectedGainFromOptimization = optimizedPath.getFiveYearSavings() - currentPath.getFiveYearSavings();

        List<FinancialTwinSimulatorResponse.ProjectionPoint> projectionSeries = List.of(
                new FinancialTwinSimulatorResponse.ProjectionPoint(
                        1,
                        currentPath.getOneYearSavings(),
                        optimizedPath.getOneYearSavings(),
                        savingsGrowthPath.getOneYearSavings()
                ),
                new FinancialTwinSimulatorResponse.ProjectionPoint(
                        3,
                        currentPath.getThreeYearSavings(),
                        optimizedPath.getThreeYearSavings(),
                        savingsGrowthPath.getThreeYearSavings()
                ),
                new FinancialTwinSimulatorResponse.ProjectionPoint(
                        5,
                        currentPath.getFiveYearSavings(),
                        optimizedPath.getFiveYearSavings(),
                        savingsGrowthPath.getFiveYearSavings()
                )
        );

        String currentStrategyInsight = String.format(
                Locale.ENGLISH,
                "If you continue your current spending pattern, your projected savings after 5 years will be %s.",
                money(currentPath.getFiveYearSavings())
        );
        String improvedStrategyInsight = String.format(
                Locale.ENGLISH,
                "Reducing monthly expenses by 10%% could increase your savings by %s over 5 years.",
                money(projectedGainFromOptimization)
        );

        return new FinancialTwinSimulatorResponse(
                averageMonthlyIncome,
                averageMonthlyExpense,
                averageMonthlySavings,
                currentPath,
                optimizedPath,
                savingsGrowthPath,
                projectedGainFromOptimization,
                currentStrategyInsight,
                improvedStrategyInsight,
                projectionSeries
        );
    }

    private FinancialTwinSimulatorResponse.ProjectionResponse buildProjectionResponse(String scenario,
                                                                                      double monthlySavings,
                                                                                      int oneYear,
                                                                                      int threeYear,
                                                                                      int fiveYear) {
        double oneYearSavings = monthlySavings * 12 * oneYear;
        double threeYearSavings = monthlySavings * 12 * threeYear;
        double fiveYearSavings = monthlySavings * 12 * fiveYear;
        return new FinancialTwinSimulatorResponse.ProjectionResponse(
                scenario,
                oneYearSavings,
                threeYearSavings,
                fiveYearSavings
        );
    }

    private LocalDate determineFirstActivityDate(List<Income> incomes, List<Expense> expenses) {
        List<LocalDate> dates = new ArrayList<>();
        for (Income income : incomes) {
            if (income.getDate() != null) {
                dates.add(income.getDate());
            }
        }
        for (Expense expense : expenses) {
            if (expense.getDate() != null) {
                dates.add(expense.getDate());
            }
        }
        return dates.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    private LocalDate determineLastActivityDate(List<Income> incomes, List<Expense> expenses) {
        List<LocalDate> dates = new ArrayList<>();
        for (Income income : incomes) {
            if (income.getDate() != null) {
                dates.add(income.getDate());
            }
        }
        for (Expense expense : expenses) {
            if (expense.getDate() != null) {
                dates.add(expense.getDate());
            }
        }
        return dates.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    private String money(double value) {
        return String.format(Locale.ENGLISH, "₹%,.0f", value);
    }
}
