package com.financeapp.backend.dto;

import java.time.LocalDate;

public class SavingsGoalResponse {

    private Long id;
    private String goalName;
    private Double targetAmount;
    private Double currentAmount;
    private LocalDate targetDate;
    private Double progressPercentage;

    public SavingsGoalResponse(Long id,
                               String goalName,
                               Double targetAmount,
                               Double currentAmount,
                               LocalDate targetDate,
                               Double progressPercentage) {
        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.progressPercentage = progressPercentage;
    }

    public Long getId() {
        return id;
    }

    public String getGoalName() {
        return goalName;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }
}