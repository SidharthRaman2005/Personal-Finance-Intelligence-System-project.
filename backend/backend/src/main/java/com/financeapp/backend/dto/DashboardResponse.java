package com.financeapp.backend.dto;

public class DashboardResponse {

    private double totalIncome;
    private double totalExpense;
    private double netBalance;
    private Double monthlyBudget;
    private Double remainingBudget;
    private Double percentageUsed;

    public DashboardResponse(double totalIncome,
                             double totalExpense,
                             double netBalance,
                             Double monthlyBudget,
                             Double remainingBudget,
                             Double percentageUsed) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.monthlyBudget = monthlyBudget;
        this.remainingBudget = remainingBudget;
        this.percentageUsed = percentageUsed;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public Double getRemainingBudget() {
        return remainingBudget;
    }

    public Double getPercentageUsed() {
        return percentageUsed;
    }
}