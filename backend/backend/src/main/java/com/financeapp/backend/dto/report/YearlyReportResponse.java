package com.financeapp.backend.dto.report;

import com.financeapp.backend.dto.FinancialHealthResponse;
import com.financeapp.backend.dto.SavingsGoalResponse;
import com.financeapp.backend.dto.investment.InvestmentSummaryResponse;

import java.util.List;

public class YearlyReportResponse {

    private final int year;
    private final Double totalIncome;
    private final Double totalExpense;
    private final Double totalSavings;
    private final Double savingsPercentage;
    private final Double budgetAmount;
    private final Double budgetUsagePercentage;
    private final Double budgetRemaining;
    private final String topSpendingCategory;
    private final Double topSpendingAmount;
    private final List<ReportCategoryBreakdown> categoryBreakdown;
    private final InvestmentSummaryResponse investmentSummary;
    private final List<SavingsGoalResponse> goals;
    private final FinancialHealthResponse healthScore;
    private final ReportComparison spendingTrend;
    private final List<ReportTrendPoint> monthlyTrend;
    private final List<String> insights;
    private final List<String> warnings;
    private final List<String> suggestions;

    public YearlyReportResponse(int year,
                                Double totalIncome,
                                Double totalExpense,
                                Double totalSavings,
                                Double savingsPercentage,
                                Double budgetAmount,
                                Double budgetUsagePercentage,
                                Double budgetRemaining,
                                String topSpendingCategory,
                                Double topSpendingAmount,
                                List<ReportCategoryBreakdown> categoryBreakdown,
                                InvestmentSummaryResponse investmentSummary,
                                List<SavingsGoalResponse> goals,
                                FinancialHealthResponse healthScore,
                                ReportComparison spendingTrend,
                                List<ReportTrendPoint> monthlyTrend,
                                List<String> insights,
                                List<String> warnings,
                                List<String> suggestions) {
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.totalSavings = totalSavings;
        this.savingsPercentage = savingsPercentage;
        this.budgetAmount = budgetAmount;
        this.budgetUsagePercentage = budgetUsagePercentage;
        this.budgetRemaining = budgetRemaining;
        this.topSpendingCategory = topSpendingCategory;
        this.topSpendingAmount = topSpendingAmount;
        this.categoryBreakdown = categoryBreakdown;
        this.investmentSummary = investmentSummary;
        this.goals = goals;
        this.healthScore = healthScore;
        this.spendingTrend = spendingTrend;
        this.monthlyTrend = monthlyTrend;
        this.insights = insights;
        this.warnings = warnings;
        this.suggestions = suggestions;
    }

    public int getYear() {
        return year;
    }

    public Double getTotalIncome() {
        return totalIncome;
    }

    public Double getTotalExpense() {
        return totalExpense;
    }

    public Double getTotalSavings() {
        return totalSavings;
    }

    public Double getSavingsPercentage() {
        return savingsPercentage;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public Double getBudgetUsagePercentage() {
        return budgetUsagePercentage;
    }

    public Double getBudgetRemaining() {
        return budgetRemaining;
    }

    public String getTopSpendingCategory() {
        return topSpendingCategory;
    }

    public Double getTopSpendingAmount() {
        return topSpendingAmount;
    }

    public List<ReportCategoryBreakdown> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public InvestmentSummaryResponse getInvestmentSummary() {
        return investmentSummary;
    }

    public List<SavingsGoalResponse> getGoals() {
        return goals;
    }

    public FinancialHealthResponse getHealthScore() {
        return healthScore;
    }

    public ReportComparison getSpendingTrend() {
        return spendingTrend;
    }

    public List<ReportTrendPoint> getMonthlyTrend() {
        return monthlyTrend;
    }

    public List<String> getInsights() {
        return insights;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
