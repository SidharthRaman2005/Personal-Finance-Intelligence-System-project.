package com.financeapp.backend.dto;

import java.util.List;

public class FinancialInsightResponse {

    private String topCategory;
    private Double topCategoryAmount;

    private Double totalIncome;
    private Double totalExpense;

    private Double savings;

    private String highestExpenseTitle;
    private Double highestExpenseAmount;

    private Double monthlyBudget;
    private List<String> insights;

    public FinancialInsightResponse(String topCategory,
                                    Double topCategoryAmount,
                                    Double totalIncome,
                                    Double totalExpense,
                                    Double savings,
                                    String highestExpenseTitle,
                                    Double highestExpenseAmount,
                                    Double monthlyBudget,
                                    List<String> insights) {

        this.topCategory = topCategory;
        this.topCategoryAmount = topCategoryAmount;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.savings = savings;
        this.highestExpenseTitle = highestExpenseTitle;
        this.highestExpenseAmount = highestExpenseAmount;
        this.monthlyBudget = monthlyBudget;
        this.insights = insights;
    }

    public String getTopCategory() { return topCategory; }

    public Double getTopCategoryAmount() { return topCategoryAmount; }

    public Double getTotalIncome() { return totalIncome; }

    public Double getTotalExpense() { return totalExpense; }

    public Double getSavings() { return savings; }

    public String getHighestExpenseTitle() { return highestExpenseTitle; }

    public Double getHighestExpenseAmount() { return highestExpenseAmount; }

    public Double getMonthlyBudget() { return monthlyBudget; }

    public List<String> getInsights() { return insights; }
}