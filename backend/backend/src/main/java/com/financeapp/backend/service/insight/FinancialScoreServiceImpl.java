package com.financeapp.backend.service.insight;

import com.financeapp.backend.dto.FinancialHealthResponse;
import com.financeapp.backend.entity.Expense;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialScoreServiceImpl implements FinancialScoreService {

    @Override
    public FinancialHealthResponse calculateFinancialHealth(double totalIncome,
                                                            double totalExpense,
                                                            double monthlyBudget,
                                                            List<Expense> monthlyExpenses) {

        if (totalIncome == 0 && totalExpense == 0) {
            return new FinancialHealthResponse(
                    0,
                    "No Data",
                    0,
                    "No income or expenses recorded this month",
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

        double budgetUsagePercent = monthlyBudget > 0
                ? (totalExpense / monthlyBudget) * 100
                : 0;

        int budgetControlPoints;
        String budgetControlStatus;

        if (monthlyBudget <= 0) {
            budgetControlPoints = 20;
            budgetControlStatus = "No Budget";
        } else if (budgetUsagePercent <= 80) {
            budgetControlPoints = 40;
            budgetControlStatus = "Good";
        } else if (budgetUsagePercent <= 100) {
            budgetControlPoints = 28;
            budgetControlStatus = "Average";
        } else {
            budgetControlPoints = 10;
            budgetControlStatus = "High";
        }

        double savings = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;

        int savingsPoints;
        String savingsStatus;

        if (savingsRate >= 30) {
            savingsPoints = 30;
            savingsStatus = "Good";
        } else if (savingsRate >= 15) {
            savingsPoints = 22;
            savingsStatus = "Average";
        } else if (savingsRate >= 0) {
            savingsPoints = 12;
            savingsStatus = "Average";
        } else {
            savingsPoints = 4;
            savingsStatus = "High";
        }

        int stabilityPoints = calculateStabilityPoints(monthlyExpenses);
        String stabilityStatus = getComponentStatus(stabilityPoints, 30);

        int score = budgetControlPoints + savingsPoints + stabilityPoints;
        score = Math.max(0, Math.min(100, score));

        String status;
        if (score >= 80) {
            status = "Excellent";
        } else if (score >= 65) {
            status = "Good";
        } else if (score >= 45) {
            status = "Average";
        } else {
            status = "Poor";
        }

        String message;
        if (monthlyBudget > 0 && budgetUsagePercent > 100) {
            message = "You are overspending against your monthly budget.";
        } else if (totalExpense > totalIncome) {
            message = "You are spending more than you earn.";
        } else if (savingsRate >= 20) {
            message = "Great balance between spending and savings this month.";
        } else {
            message = "Keep improving spending consistency and savings rate.";
        }

        return new FinancialHealthResponse(
                score,
                status,
                savingsRate,
                message,
                new FinancialHealthResponse.ScoreBreakdown(
                        budgetControlPoints,
                        budgetControlStatus,
                        savingsPoints,
                        savingsStatus,
                        stabilityPoints,
                        stabilityStatus
                )
        );
    }

    @Override
    public FinancialHealthResponse calculateOverallFinancialHealth(double totalIncome,
                                                                   double totalExpense,
                                                                   List<Object[]> categoryTotals) {

        if (totalIncome == 0 && totalExpense == 0) {
            return new FinancialHealthResponse(
                    0,
                    "No Data",
                    0,
                    "No income or expenses recorded yet",
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

        double savings = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;
        double expenseRatio = totalIncome > 0 ? (totalExpense / totalIncome) * 100 : 100;

        int spendingControlPoints;
        String spendingControlStatus;

        if (expenseRatio <= 60) {
            spendingControlPoints = 40;
            spendingControlStatus = "Good";
        } else if (expenseRatio <= 85) {
            spendingControlPoints = 32;
            spendingControlStatus = "Average";
        } else if (expenseRatio <= 100) {
            spendingControlPoints = 22;
            spendingControlStatus = "Average";
        } else {
            spendingControlPoints = 10;
            spendingControlStatus = "High";
        }

        int savingsPoints;
        String savingsStatus;

        if (savingsRate >= 30) {
            savingsPoints = 30;
            savingsStatus = "Good";
        } else if (savingsRate >= 15) {
            savingsPoints = 22;
            savingsStatus = "Average";
        } else if (savingsRate >= 0) {
            savingsPoints = 12;
            savingsStatus = "Average";
        } else {
            savingsPoints = 4;
            savingsStatus = "High";
        }

        double topCategoryShare = 0;
        if (categoryTotals != null && !categoryTotals.isEmpty() && totalExpense > 0) {
            double topCategoryAmount = categoryTotals.stream()
                    .filter(row -> row != null && row.length > 1)
                    .mapToDouble(row -> toDouble(row[1]))
                    .max()
                    .orElse(0.0);
            topCategoryShare = (topCategoryAmount / totalExpense) * 100.0;
        }

        int concentrationPoints;
        String concentrationStatus;

        if (topCategoryShare <= 35) {
            concentrationPoints = 30;
            concentrationStatus = "Good";
        } else if (topCategoryShare <= 50) {
            concentrationPoints = 24;
            concentrationStatus = "Average";
        } else if (topCategoryShare <= 65) {
            concentrationPoints = 16;
            concentrationStatus = "Average";
        } else {
            concentrationPoints = 8;
            concentrationStatus = "High";
        }

        int score = spendingControlPoints + savingsPoints + concentrationPoints;
        score = Math.max(0, Math.min(100, score));

        String status;
        if (score >= 80) {
            status = "Excellent";
        } else if (score >= 65) {
            status = "Good";
        } else if (score >= 45) {
            status = "Average";
        } else {
            status = "Poor";
        }

        String message;
        if (totalExpense > totalIncome) {
            message = "Your total spending is higher than your total income.";
        } else if (savingsRate >= 20) {
            message = "Your overall savings rate is healthy.";
        } else {
            message = "Your spending is balanced, but savings can improve.";
        }

        return new FinancialHealthResponse(
                score,
                status,
                savingsRate,
                message,
                new FinancialHealthResponse.ScoreBreakdown(
                        spendingControlPoints,
                        spendingControlStatus,
                        savingsPoints,
                        savingsStatus,
                        concentrationPoints,
                        concentrationStatus
                )
        );
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private int calculateStabilityPoints(List<Expense> monthlyExpenses) {
        if (monthlyExpenses == null || monthlyExpenses.isEmpty()) {
            return 30;
        }

        Map<Integer, Double> weeklyTotals = new HashMap<>();

        for (Expense expense : monthlyExpenses) {
            int weekIndex = Math.max(0, (expense.getDate().getDayOfMonth() - 1) / 7);
            weeklyTotals.put(
                    weekIndex,
                    weeklyTotals.getOrDefault(weekIndex, 0.0) + expense.getAmount()
            );
        }

        List<Double> totals = new ArrayList<>(weeklyTotals.values());
        if (totals.size() <= 1) {
            return 26;
        }

        double mean = totals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (mean == 0) {
            return 30;
        }

        double variance = totals.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);
        double variation = standardDeviation / mean;

        if (variation <= 0.25) {
            return 30;
        }
        if (variation <= 0.50) {
            return 24;
        }
        if (variation <= 0.85) {
            return 16;
        }
        return 8;
    }

    private String getComponentStatus(int points, int maxPoints) {
        double ratio = maxPoints == 0 ? 0 : (double) points / maxPoints;
        if (ratio >= 0.8) {
            return "Good";
        }
        if (ratio >= 0.5) {
            return "Average";
        }
        return "High";
    }
}
