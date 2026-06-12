package com.financeapp.backend.dto.report;

public class ReportTrendPoint {

    private final String label;
    private final Double income;
    private final Double expense;
    private final Double savings;

    public ReportTrendPoint(String label, Double income, Double expense, Double savings) {
        this.label = label;
        this.income = income;
        this.expense = expense;
        this.savings = savings;
    }

    public String getLabel() {
        return label;
    }

    public Double getIncome() {
        return income;
    }

    public Double getExpense() {
        return expense;
    }

    public Double getSavings() {
        return savings;
    }
}
