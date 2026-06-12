package com.financeapp.backend.service.insight;

import com.financeapp.backend.dto.FinancialHealthResponse;
import com.financeapp.backend.dto.FinancialInsightResponse;
import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.entity.Income;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.BudgetRepository;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.IncomeRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsightServiceImpl implements InsightService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
        private final BudgetRepository budgetRepository;
        private final FinancialScoreService financialScoreService;

    public InsightServiceImpl(ExpenseRepository expenseRepository,
                              IncomeRepository incomeRepository,
                                                          UserRepository userRepository,
                                                          BudgetRepository budgetRepository,
                                                          FinancialScoreService financialScoreService) {

        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
                this.budgetRepository = budgetRepository;
                this.financialScoreService = financialScoreService;
    }

    @Override
    public FinancialInsightResponse generateInsights(String username,
                                                     int month,
                                                     int year) {

        User user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Expense> monthlyExpenses =
                expenseRepository.findMonthlyExpenses(user, month, year);

        double totalExpense = monthlyExpenses
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Income> monthlyIncome =
                incomeRepository.findByUserUsernameAndDateBetween(
                        username,
                        start,
                        end
                );

        double totalIncome = monthlyIncome
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();

        double savings = totalIncome - totalExpense;

        Expense highestExpense = monthlyExpenses
                .stream()
                .max(Comparator.comparingDouble(Expense::getAmount))
                .orElse(null);

        String highestTitle = "None";
        double highestAmount = 0;

        if (highestExpense != null) {
            highestTitle = highestExpense.getTitle();
            highestAmount = highestExpense.getAmount();
        }

        List<Object[]> categoryTotals =
                expenseRepository.getMonthlyCategoryTotals(user, month, year);

        String topCategory = "None";
        double topCategoryAmount = 0;
        double foodExpense = 0;
        Map<String, Double> categoryBreakdown = new HashMap<>();

        for (Object[] row : categoryTotals) {
            String category = String.valueOf(row[0]);
            double amount = toDouble(row[1]);

            categoryBreakdown.put(category.toLowerCase().trim(), amount);

            if (amount > topCategoryAmount) {
                topCategory = category;
                topCategoryAmount = amount;
            }

            if ("food".equalsIgnoreCase(category.trim())) {
                foodExpense += amount;
            }
        }

        Double monthlyBudget = resolveMonthlyBudget(user, month, year);
        List<String> insights = generateRuleBasedInsights(
                totalIncome,
                totalExpense,
                foodExpense,
                monthlyBudget,
                categoryBreakdown
        );

        return new FinancialInsightResponse(
                topCategory,
                topCategoryAmount,
                totalIncome,
                totalExpense,
                savings,
                highestTitle,
                highestAmount,
                monthlyBudget,
                insights
        );
    }

    @Override
    public FinancialHealthResponse getFinancialHealth(String username,
                                                      int month,
                                                      int year) {

        User user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Expense> monthlyExpenses =
                expenseRepository.findMonthlyExpenses(user, month, year);

        double totalExpense = monthlyExpenses
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Income> monthlyIncome =
                incomeRepository.findByUserUsernameAndDateBetween(
                        username,
                        start,
                        end
                );

        double totalIncome = monthlyIncome
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();

        Double monthlyBudget = resolveMonthlyBudget(user, month, year);

        return financialScoreService.calculateFinancialHealth(
                totalIncome,
                totalExpense,
                monthlyBudget,
                monthlyExpenses
        );
    }

        @Override
        public FinancialInsightResponse generateOverallInsights(String username) {

                User user = userRepository.findByUsernameIgnoreCase(username.trim())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<Expense> expenses = expenseRepository.findByUser(user);
                double totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();

                List<Income> incomes = incomeRepository.findByUserUsername(username);
                double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();
                double savings = totalIncome - totalExpense;

                Expense highestExpense = expenses
                                .stream()
                                .max(Comparator.comparingDouble(Expense::getAmount))
                                .orElse(null);

                String highestTitle = "None";
                double highestAmount = 0;

                if (highestExpense != null) {
                        highestTitle = highestExpense.getTitle();
                        highestAmount = highestExpense.getAmount();
                }

                List<Object[]> categoryTotals = expenseRepository.getCategoryTotals(user);
                String topCategory = "None";
                double topCategoryAmount = 0;
                double foodExpense = 0;
                Map<String, Double> categoryBreakdown = new HashMap<>();

                for (Object[] row : categoryTotals) {
                        String category = String.valueOf(row[0]);
                        double amount = toDouble(row[1]);

                        categoryBreakdown.put(category.toLowerCase().trim(), amount);

                        if (amount > topCategoryAmount) {
                                topCategory = category;
                                topCategoryAmount = amount;
                        }

                        if ("food".equalsIgnoreCase(category.trim())) {
                                foodExpense += amount;
                        }
                }

                Double budget = user.getMonthlyBudget() != null ? user.getMonthlyBudget() : 0.0;
                List<String> insights = generateOverallRuleBasedInsights(
                                totalIncome,
                                totalExpense,
                                foodExpense,
                                budget,
                                categoryBreakdown
                );

                return new FinancialInsightResponse(
                                topCategory,
                                topCategoryAmount,
                                totalIncome,
                                totalExpense,
                                savings,
                                highestTitle,
                                highestAmount,
                                budget,
                                insights
                );
        }

        @Override
        public FinancialHealthResponse getOverallFinancialHealth(String username) {

                User user = userRepository.findByUsernameIgnoreCase(username.trim())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<Expense> expenses = expenseRepository.findByUser(user);
                double totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();

                List<Income> incomes = incomeRepository.findByUserUsername(username);
                double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();

                List<Object[]> categoryTotals = expenseRepository.getCategoryTotals(user);

                return financialScoreService.calculateOverallFinancialHealth(
                                totalIncome,
                                totalExpense,
                                categoryTotals
                );
        }

    private Double resolveMonthlyBudget(User user, int month, int year) {
        return budgetRepository.findByUserAndMonthAndYear(user, month, year)
                .map(budget -> budget.getAmount())
                .orElseGet(() -> user.getMonthlyBudget() != null ? user.getMonthlyBudget() : 0.0);
    }

    private List<String> generateRuleBasedInsights(double totalIncome,
                                                   double totalExpense,
                                                   double foodExpense,
                                                   double budget,
                                                   Map<String, Double> categoryBreakdown) {

        List<String> insights = new ArrayList<>();

        if (budget > 0 && totalExpense > (0.8 * budget)) {
            insights.add("You are close to exceeding your budget");
        }

        if (totalExpense > 0 && foodExpense > (0.4 * totalExpense)) {
            insights.add("High food spending detected");
        }

        if (totalExpense > totalIncome) {
            insights.add("You are spending more than you earn");
        }

        if (insights.isEmpty()) {
            insights.add("Your spending is under control this month");
        }

        if (!categoryBreakdown.isEmpty() && !categoryBreakdown.containsKey("food")) {
            insights.add("Add clearer categories in expenses for more accurate AI insights");
        }

        return insights;
    }

        private List<String> generateOverallRuleBasedInsights(double totalIncome,
                                                                                                                  double totalExpense,
                                                                                                                  double foodExpense,
                                                                                                                  double budget,
                                                                                                                  Map<String, Double> categoryBreakdown) {

                List<String> insights = new ArrayList<>();

                if (totalExpense > totalIncome) {
                        insights.add("Your total spending is higher than your total income.");
                }

                if (totalIncome > 0) {
                        double savingsRate = ((totalIncome - totalExpense) / totalIncome) * 100.0;
                        if (savingsRate >= 20) {
                                insights.add("Your overall savings rate is healthy at " + String.format("%.1f", savingsRate) + "%.");
                        } else if (savingsRate < 0) {
                                insights.add("Your overall savings rate is negative, so spending is outpacing income.");
                        }
                }

                if (foodExpense > 0 && totalExpense > 0 && foodExpense > (0.4 * totalExpense)) {
                        insights.add("Food spending is a major share of your overall expenses.");
                }

                if (budget > 0 && totalExpense > budget) {
                        insights.add("Your spending is above your current budget target.");
                }

                if (insights.isEmpty()) {
                        insights.add("Your overall finances look balanced.");
                }

                if (!categoryBreakdown.isEmpty() && !categoryBreakdown.containsKey("food")) {
                        insights.add("Add clearer categories in expenses for more accurate insights.");
                }

                return insights;
        }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}