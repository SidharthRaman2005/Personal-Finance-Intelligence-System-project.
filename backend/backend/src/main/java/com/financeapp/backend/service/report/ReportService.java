package com.financeapp.backend.service.report;

import com.financeapp.backend.dto.FinancialHealthResponse;
import com.financeapp.backend.dto.FinancialInsightResponse;
import com.financeapp.backend.dto.SavingsGoalResponse;
import com.financeapp.backend.dto.investment.InvestmentSummaryResponse;
import com.financeapp.backend.dto.report.MonthlyReportResponse;
import com.financeapp.backend.dto.report.ReportCategoryBreakdown;
import com.financeapp.backend.dto.report.ReportComparison;
import com.financeapp.backend.dto.report.ReportTrendPoint;
import com.financeapp.backend.dto.report.YearlyReportResponse;
import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.entity.Income;
import com.financeapp.backend.entity.SavingsGoal;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.BudgetRepository;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.IncomeRepository;
import com.financeapp.backend.repository.SavingsGoalRepository;
import com.financeapp.backend.repository.UserRepository;
import com.financeapp.backend.service.insight.FinancialScoreService;
import com.financeapp.backend.service.insight.InsightService;
import com.financeapp.backend.service.investment.InvestmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final BudgetRepository budgetRepository;
    private final InvestmentService investmentService;
    private final FinancialScoreService financialScoreService;
    private final InsightService insightService;

    public ReportService(UserRepository userRepository,
                         ExpenseRepository expenseRepository,
                         IncomeRepository incomeRepository,
                         SavingsGoalRepository savingsGoalRepository,
                         BudgetRepository budgetRepository,
                         InvestmentService investmentService,
                         FinancialScoreService financialScoreService,
                         InsightService insightService) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.budgetRepository = budgetRepository;
        this.investmentService = investmentService;
        this.financialScoreService = financialScoreService;
        this.insightService = insightService;
    }

    public MonthlyReportResponse getMonthlyReport(String username, int month, int year) {
        User user = findUser(username);

        YearMonth period = YearMonth.of(year, month);
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findMonthlyExpenses(user, month, year);
        List<Income> incomes = incomeRepository.findByUserUsernameAndDateBetween(username, start, end);

        double totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();
        double totalSavings = totalIncome - totalExpense;
        double savingsPercentage = totalIncome > 0 ? (totalSavings / totalIncome) * 100.0 : 0.0;

        double budgetAmount = resolveMonthlyBudget(user, month, year);
        double budgetRemaining = budgetAmount > 0 ? budgetAmount - totalExpense : 0.0;
        double budgetUsage = budgetAmount > 0 ? (totalExpense / budgetAmount) * 100.0 : 0.0;

        List<Object[]> categoryTotals = expenseRepository.getMonthlyCategoryTotals(user, month, year);
        List<ReportCategoryBreakdown> categoryBreakdown = buildCategoryBreakdown(categoryTotals, totalExpense);
        String topCategory = categoryBreakdown.isEmpty() ? "None" : categoryBreakdown.get(0).getCategory();
        Double topCategoryAmount = categoryBreakdown.isEmpty() ? 0.0 : categoryBreakdown.get(0).getTotalAmount();

        InvestmentSummaryResponse investmentSummary = investmentService.getSummary(username);
        List<SavingsGoalResponse> goals = buildGoalProgress(savingsGoalRepository.findByUserUsername(username));

        FinancialHealthResponse healthScore = financialScoreService.calculateFinancialHealth(
                totalIncome,
                totalExpense,
                budgetAmount,
                expenses
        );

        YearMonth previousPeriod = period.minusMonths(1);
        double previousExpense = getMonthlyExpense(user, previousPeriod);
        double previousIncome = getMonthlyIncome(username, previousPeriod);

        ReportComparison spendingTrend = buildComparison(totalExpense, previousExpense);

        List<ReportTrendPoint> monthlyTrend = buildLastSixMonthsTrend(user, username, period);

        FinancialInsightResponse baseInsights = insightService.generateInsights(username, month, year);
        List<String> insights = new ArrayList<>(safeList(baseInsights.getInsights()));
        appendMonthlyInsights(insights, totalExpense, previousExpense, totalIncome, previousIncome, topCategory);

        List<String> warnings = buildMonthlyWarnings(totalExpense, totalIncome, budgetAmount, budgetRemaining, topCategory,
                categoryBreakdown);
        List<String> suggestions = buildMonthlySuggestions(totalSavings, savingsPercentage, budgetAmount, budgetRemaining,
                categoryBreakdown, goals);

        return new MonthlyReportResponse(
                month,
                year,
                totalIncome,
                totalExpense,
                totalSavings,
                savingsPercentage,
                budgetAmount,
                budgetUsage,
                budgetRemaining,
                topCategory,
                topCategoryAmount,
                categoryBreakdown,
                investmentSummary,
                goals,
                healthScore,
                spendingTrend,
                monthlyTrend,
                dedupe(insights),
                dedupe(warnings),
                dedupe(suggestions)
        );
    }

    public YearlyReportResponse getYearlyReport(String username, int year) {
        User user = findUser(username);

        Map<Integer, Double> expenseByMonth = toMonthlyTotals(expenseRepository.getMonthlyTotalsByYear(user, year));
        Map<Integer, Double> incomeByMonth = toMonthlyTotals(incomeRepository.getMonthlyTotalsByYear(username, year));

        List<ReportTrendPoint> monthlyTrend = new ArrayList<>();
        double totalIncome = 0.0;
        double totalExpense = 0.0;

        for (int month = 1; month <= 12; month++) {
            double income = incomeByMonth.getOrDefault(month, 0.0);
            double expense = expenseByMonth.getOrDefault(month, 0.0);
            totalIncome += income;
            totalExpense += expense;
            monthlyTrend.add(new ReportTrendPoint(monthLabel(month), income, expense, income - expense));
        }

        double totalSavings = totalIncome - totalExpense;
        double savingsPercentage = totalIncome > 0 ? (totalSavings / totalIncome) * 100.0 : 0.0;

        double yearlyBudget = resolveYearlyBudget(user, year);
        double budgetRemaining = yearlyBudget > 0 ? yearlyBudget - totalExpense : 0.0;
        double budgetUsage = yearlyBudget > 0 ? (totalExpense / yearlyBudget) * 100.0 : 0.0;

        List<Object[]> categoryTotals = expenseRepository.getYearlyCategoryTotals(user, year);
        List<ReportCategoryBreakdown> categoryBreakdown = buildCategoryBreakdown(categoryTotals, totalExpense);
        String topCategory = categoryBreakdown.isEmpty() ? "None" : categoryBreakdown.get(0).getCategory();
        Double topCategoryAmount = categoryBreakdown.isEmpty() ? 0.0 : categoryBreakdown.get(0).getTotalAmount();

        InvestmentSummaryResponse investmentSummary = investmentService.getSummary(username);
        List<SavingsGoalResponse> goals = buildGoalProgress(savingsGoalRepository.findByUserUsername(username));

        FinancialHealthResponse healthScore = buildYearlyHealthScore(username, user, year, totalIncome, totalExpense,
                yearlyBudget);

        double previousYearExpense = getYearlyExpense(user, year - 1);
        ReportComparison spendingTrend = buildComparison(totalExpense, previousYearExpense);

        List<String> insights = new ArrayList<>();
        appendYearlyInsights(insights, totalExpense, previousYearExpense, totalIncome, year, topCategory,
                categoryBreakdown, monthlyTrend);

        List<String> warnings = buildYearlyWarnings(totalExpense, totalIncome, yearlyBudget, budgetRemaining,
                categoryBreakdown);
        List<String> suggestions = buildYearlySuggestions(totalSavings, savingsPercentage, yearlyBudget,
                categoryBreakdown, goals);

        return new YearlyReportResponse(
                year,
                totalIncome,
                totalExpense,
                totalSavings,
                savingsPercentage,
                yearlyBudget,
                budgetUsage,
                budgetRemaining,
                topCategory,
                topCategoryAmount,
                categoryBreakdown,
                investmentSummary,
                goals,
                healthScore,
                spendingTrend,
                monthlyTrend,
                dedupe(insights),
                dedupe(warnings),
                dedupe(suggestions)
        );
    }

    private User findUser(String username) {
        String trimmed = username == null ? "" : username.trim();
        return userRepository.findByUsernameIgnoreCase(trimmed)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private double resolveMonthlyBudget(User user, int month, int year) {
        return budgetRepository.findByUserAndMonthAndYear(user, month, year)
                .map(budget -> budget.getAmount())
                .orElseGet(() -> user.getMonthlyBudget() != null ? user.getMonthlyBudget() : 0.0);
    }

    private double resolveYearlyBudget(User user, int year) {
        double total = 0.0;
        for (int month = 1; month <= 12; month++) {
            total += resolveMonthlyBudget(user, month, year);
        }
        return total;
    }

    private double getMonthlyExpense(User user, YearMonth period) {
        Double total = expenseRepository.getTotalExpenseByUserAndDateBetween(user,
                period.atDay(1),
                period.atEndOfMonth());
        return total == null ? 0.0 : total;
    }

    private double getMonthlyIncome(String username, YearMonth period) {
        List<Income> incomes = incomeRepository.findByUserUsernameAndDateBetween(
                username,
                period.atDay(1),
                period.atEndOfMonth()
        );
        return incomes.stream().mapToDouble(Income::getAmount).sum();
    }

    private double getYearlyExpense(User user, int year) {
        YearMonth start = YearMonth.of(year, 1);
        YearMonth end = YearMonth.of(year, 12);
        Double total = expenseRepository.getTotalExpenseByUserAndDateBetween(
                user,
                start.atDay(1),
                end.atEndOfMonth()
        );
        return total == null ? 0.0 : total;
    }

    private List<ReportTrendPoint> buildLastSixMonthsTrend(User user, String username, YearMonth period) {
        List<ReportTrendPoint> points = new ArrayList<>();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth target = period.minusMonths(offset);
            double income = getMonthlyIncome(username, target);
            double expense = getMonthlyExpense(user, target);
            points.add(new ReportTrendPoint(monthLabel(target.getMonthValue()), income, expense, income - expense));
        }
        return points;
    }

    private FinancialHealthResponse buildYearlyHealthScore(String username,
                                                           User user,
                                                           int year,
                                                           double totalIncome,
                                                           double totalExpense,
                                                           double yearlyBudget) {
        double totalScore = 0.0;
        double budgetPoints = 0.0;
        double savingsPoints = 0.0;
        double stabilityPoints = 0.0;
        int monthsWithData = 0;

        for (int month = 1; month <= 12; month++) {
            List<Expense> monthlyExpenses = expenseRepository.findMonthlyExpenses(user, month, year);
            YearMonth period = YearMonth.of(year, month);
            double income = getMonthlyIncome(username, period);
            double expense = monthlyExpenses.stream().mapToDouble(Expense::getAmount).sum();
            if (income == 0.0 && expense == 0.0) {
                continue;
            }
            double budget = resolveMonthlyBudget(user, month, year);
            FinancialHealthResponse monthly = financialScoreService.calculateFinancialHealth(
                    income,
                    expense,
                    budget,
                    monthlyExpenses
            );

            totalScore += monthly.getScore();
            budgetPoints += monthly.getBreakdown().getBudgetControlPoints();
            savingsPoints += monthly.getBreakdown().getSavingsPoints();
            stabilityPoints += monthly.getBreakdown().getStabilityPoints();
            monthsWithData++;
        }

        if (monthsWithData == 0) {
            return new FinancialHealthResponse(
                    0,
                    "No Data",
                    0.0,
                    "No income or expenses recorded this year",
                    new FinancialHealthResponse.ScoreBreakdown(
                            0,
                            "No Data",
                            0,
                            "No Data",
                            0,
                            "No Data"
                    )
            );
        }

        int avgScore = (int) Math.round(totalScore / monthsWithData);
        int avgBudget = (int) Math.round(budgetPoints / monthsWithData);
        int avgSavings = (int) Math.round(savingsPoints / monthsWithData);
        int avgStability = (int) Math.round(stabilityPoints / monthsWithData);

        String status = resolveHealthStatus(avgScore);
        double savingsRate = totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100.0 : 0.0;

        String message;
        if (totalExpense > totalIncome) {
            message = "You spent more than you earned this year.";
        } else if (savingsRate >= 20) {
            message = "Strong yearly savings momentum.";
        } else if (yearlyBudget > 0 && totalExpense > yearlyBudget) {
            message = "Yearly spending exceeded the planned budget.";
        } else {
            message = "Solid baseline for next year improvements.";
        }

        return new FinancialHealthResponse(
                avgScore,
                status,
                savingsRate,
                message,
                new FinancialHealthResponse.ScoreBreakdown(
                        avgBudget,
                        resolveComponentStatus(avgBudget, 40),
                        avgSavings,
                        resolveComponentStatus(avgSavings, 30),
                        avgStability,
                        resolveComponentStatus(avgStability, 30)
                )
        );
    }

    private List<SavingsGoalResponse> buildGoalProgress(List<SavingsGoal> goals) {
        if (goals == null || goals.isEmpty()) {
            return Collections.emptyList();
        }
        List<SavingsGoalResponse> response = new ArrayList<>();
        for (SavingsGoal goal : goals) {
            double target = goal.getTargetAmount() == null ? 0.0 : goal.getTargetAmount();
            double current = goal.getCurrentAmount() == null ? 0.0 : goal.getCurrentAmount();
            double progress = target > 0 ? (current / target) * 100.0 : 0.0;
            response.add(new SavingsGoalResponse(
                    goal.getId(),
                    goal.getGoalName(),
                    goal.getTargetAmount(),
                    goal.getCurrentAmount(),
                    goal.getTargetDate(),
                    progress
            ));
        }
        response.sort(Comparator.comparing(SavingsGoalResponse::getGoalName, String.CASE_INSENSITIVE_ORDER));
        return response;
    }

    private List<ReportCategoryBreakdown> buildCategoryBreakdown(List<Object[]> categoryTotals,
                                                                 double totalExpense) {
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReportCategoryBreakdown> breakdown = new ArrayList<>();
        for (Object[] row : categoryTotals) {
            if (row == null || row.length < 2) {
                continue;
            }
            String category = row[0] == null ? "Other" : String.valueOf(row[0]);
            double amount = toDouble(row[1]);
            double percent = totalExpense > 0 ? (amount / totalExpense) * 100.0 : 0.0;
            breakdown.add(new ReportCategoryBreakdown(category, amount, percent));
        }
        breakdown.sort(Comparator.comparing(ReportCategoryBreakdown::getTotalAmount).reversed());
        return breakdown;
    }

    private ReportComparison buildComparison(double current, double previous) {
        if (previous <= 0) {
            String direction = current > 0 ? "up" : "flat";
            return new ReportComparison(current, previous, current > 0 ? 100.0 : 0.0, direction);
        }

        double change = ((current - previous) / previous) * 100.0;
        String direction = Math.abs(change) < 1 ? "flat" : change > 0 ? "up" : "down";
        return new ReportComparison(current, previous, change, direction);
    }

    private void appendMonthlyInsights(List<String> insights,
                                       double totalExpense,
                                       double previousExpense,
                                       double totalIncome,
                                       double previousIncome,
                                       String topCategory) {
        if (previousExpense > 0) {
            double change = ((totalExpense - previousExpense) / previousExpense) * 100.0;
            if (Math.abs(change) >= 5) {
                String direction = change > 0 ? "more" : "less";
                insights.add("You spent " + formatPercent(Math.abs(change)) + "% " + direction + " than last month.");
            }
        }

        double savingsRate = totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100.0 : 0.0;
        double previousSavingsRate = previousIncome > 0
                ? ((previousIncome - previousExpense) / previousIncome) * 100.0
                : 0.0;
        if (previousIncome > 0 && savingsRate - previousSavingsRate >= 4) {
            insights.add("Your savings rate improved compared to last month.");
        }

        if (topCategory != null && !"None".equalsIgnoreCase(topCategory)) {
            insights.add("Your top spending category was " + topCategory + ".");
        }
    }

    private void appendYearlyInsights(List<String> insights,
                                      double totalExpense,
                                      double previousYearExpense,
                                      double totalIncome,
                                      int year,
                                      String topCategory,
                                      List<ReportCategoryBreakdown> categories,
                                      List<ReportTrendPoint> monthlyTrend) {
        if (previousYearExpense > 0) {
            double change = ((totalExpense - previousYearExpense) / previousYearExpense) * 100.0;
            if (Math.abs(change) >= 5) {
                String direction = change > 0 ? "higher" : "lower";
                insights.add("Total spending was " + formatPercent(Math.abs(change)) + "% " + direction
                        + " than last year.");
            }
        }

        if (totalIncome > 0) {
            double savingsRate = ((totalIncome - totalExpense) / totalIncome) * 100.0;
            if (savingsRate >= 15) {
                insights.add("Savings rate stayed healthy at " + formatPercent(savingsRate) + "%." );
            }
        }

        if (topCategory != null && !"None".equalsIgnoreCase(topCategory)) {
            insights.add("Most yearly spending went to " + topCategory + ".");
        }

        ReportTrendPoint peakMonth = monthlyTrend.stream()
                .max(Comparator.comparing(point -> point.getExpense() == null ? 0.0 : point.getExpense()))
                .orElse(null);
        if (peakMonth != null && peakMonth.getExpense() != null && peakMonth.getExpense() > 0) {
            insights.add("Peak spending month: " + peakMonth.getLabel() + ".");
        }

        if (categories.size() >= 2) {
            insights.add("Top categories make up " + formatPercent(categories.get(0).getPercentage()
                + categories.get(1).getPercentage()) + "% of annual spend.");
        }
    }

    private List<String> buildMonthlyWarnings(double totalExpense,
                                              double totalIncome,
                                              double budgetAmount,
                                              double budgetRemaining,
                                              String topCategory,
                                              List<ReportCategoryBreakdown> breakdown) {
        List<String> warnings = new ArrayList<>();

        if (budgetAmount > 0 && totalExpense > budgetAmount) {
            warnings.add("You exceeded your monthly budget by " + formatAmount(totalExpense - budgetAmount) + " INR.");
        }

        if (totalExpense > totalIncome) {
            warnings.add("Expenses are higher than income this month.");
        }

        ReportCategoryBreakdown top = breakdown.isEmpty() ? null : breakdown.get(0);
        if (top != null && top.getPercentage() >= 45) {
            warnings.add("Spending is heavily concentrated in " + topCategory + ".");
        }

        if (budgetAmount > 0 && budgetRemaining <= 0) {
            warnings.add("Budget buffer is exhausted for the month.");
        }

        return warnings;
    }

    private List<String> buildYearlyWarnings(double totalExpense,
                                             double totalIncome,
                                             double yearlyBudget,
                                             double budgetRemaining,
                                             List<ReportCategoryBreakdown> breakdown) {
        List<String> warnings = new ArrayList<>();

        if (yearlyBudget > 0 && totalExpense > yearlyBudget) {
            warnings.add("Yearly spending exceeded the planned budget.");
        }

        if (totalExpense > totalIncome) {
            warnings.add("Total spending surpassed total income this year.");
        }

        ReportCategoryBreakdown top = breakdown.isEmpty() ? null : breakdown.get(0);
        if (top != null && top.getPercentage() >= 45) {
            warnings.add("High concentration in " + top.getCategory() + " expenses this year.");
        }

        if (yearlyBudget > 0 && budgetRemaining <= 0) {
            warnings.add("Yearly budget buffer is exhausted.");
        }

        return warnings;
    }

    private List<String> buildMonthlySuggestions(double totalSavings,
                                                 double savingsPercentage,
                                                 double budgetAmount,
                                                 double budgetRemaining,
                                                 List<ReportCategoryBreakdown> breakdown,
                                                 List<SavingsGoalResponse> goals) {
        List<String> suggestions = new ArrayList<>();

        if (budgetAmount > 0 && budgetRemaining > 0) {
            suggestions.add("Keep daily spend under " + formatAmount(budgetRemaining / 7) + " INR for the next week.");
        }

        if (savingsPercentage < 15) {
            suggestions.add("Target at least 15% of income as savings next month.");
        }

        ReportCategoryBreakdown top = breakdown.isEmpty() ? null : breakdown.get(0);
        if (top != null && top.getPercentage() >= 35) {
            suggestions.add("Set a weekly cap for " + top.getCategory() + " expenses.");
        }

        if (!goals.isEmpty()) {
            SavingsGoalResponse goal = goals.get(0);
            suggestions.add("Allocate a small monthly transfer toward " + goal.getGoalName() + ".");
        }

        if (totalSavings < 0) {
            suggestions.add("Review recurring expenses to reduce overspending quickly.");
        }

        return suggestions;
    }

    private List<String> buildYearlySuggestions(double totalSavings,
                                                double savingsPercentage,
                                                double yearlyBudget,
                                                List<ReportCategoryBreakdown> breakdown,
                                                List<SavingsGoalResponse> goals) {
        List<String> suggestions = new ArrayList<>();

        if (savingsPercentage < 18) {
            suggestions.add("Aim to lift yearly savings rate above 18%.");
        }

        if (yearlyBudget > 0) {
            suggestions.add("Recalibrate monthly budgets based on your actual yearly spend.");
        }

        ReportCategoryBreakdown top = breakdown.isEmpty() ? null : breakdown.get(0);
        if (top != null && top.getPercentage() >= 35) {
            suggestions.add("Plan quarterly limits for " + top.getCategory() + " expenses.");
        }

        if (!goals.isEmpty()) {
            suggestions.add("Increase contributions to the most important savings goal.");
        }

        if (totalSavings < 0) {
            suggestions.add("Trim high-cost categories and renegotiate recurring commitments.");
        }

        return suggestions;
    }

    private String resolveHealthStatus(int score) {
        if (score >= 80) {
            return "Excellent";
        }
        if (score >= 65) {
            return "Good";
        }
        if (score >= 45) {
            return "Average";
        }
        return "Poor";
    }

    private String resolveComponentStatus(int points, int maxPoints) {
        double ratio = maxPoints == 0 ? 0 : (double) points / maxPoints;
        if (ratio >= 0.8) {
            return "Good";
        }
        if (ratio >= 0.5) {
            return "Average";
        }
        return "High";
    }

    private List<String> safeList(List<String> items) {
        return items == null ? Collections.emptyList() : items;
    }

    private List<String> dedupe(List<String> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (item == null || item.trim().isEmpty()) {
                continue;
            }
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    private Map<Integer, Double> toMonthlyTotals(List<Object[]> rows) {
        Map<Integer, Double> totals = new HashMap<>();
        if (rows == null) {
            return totals;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            int month = row[0] instanceof Number ? ((Number) row[0]).intValue() : 0;
            double amount = toDouble(row[1]);
            totals.put(month, amount);
        }
        return totals;
    }

    private String monthLabel(int month) {
        return java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private String formatPercent(double value) {
        return String.format(Locale.US, "%.0f", value);
    }

    private String formatAmount(double value) {
        return String.format(Locale.US, "%.0f", value);
    }
}
