package com.financeapp.backend.dto;

public class FinancialHealthResponse {

    private int score;
    private String status;
    private double savingsRate;
    private String message;
    private ScoreBreakdown breakdown;

    public FinancialHealthResponse(int score,
                                   String status,
                                   double savingsRate,
                                   String message,
                                   ScoreBreakdown breakdown) {
        this.score = score;
        this.status = status;
        this.savingsRate = savingsRate;
        this.message = message;
        this.breakdown = breakdown;
    }

    public int getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public String getMessage() {
        return message;
    }

    public ScoreBreakdown getBreakdown() {
        return breakdown;
    }

    public static class ScoreBreakdown {
        private int budgetControlPoints;
        private String budgetControlStatus;
        private int savingsPoints;
        private String savingsStatus;
        private int stabilityPoints;
        private String stabilityStatus;

        public ScoreBreakdown(int budgetControlPoints,
                              String budgetControlStatus,
                              int savingsPoints,
                              String savingsStatus,
                              int stabilityPoints,
                              String stabilityStatus) {
            this.budgetControlPoints = budgetControlPoints;
            this.budgetControlStatus = budgetControlStatus;
            this.savingsPoints = savingsPoints;
            this.savingsStatus = savingsStatus;
            this.stabilityPoints = stabilityPoints;
            this.stabilityStatus = stabilityStatus;
        }

        public int getBudgetControlPoints() {
            return budgetControlPoints;
        }

        public String getBudgetControlStatus() {
            return budgetControlStatus;
        }

        public int getSavingsPoints() {
            return savingsPoints;
        }

        public String getSavingsStatus() {
            return savingsStatus;
        }

        public int getStabilityPoints() {
            return stabilityPoints;
        }

        public String getStabilityStatus() {
            return stabilityStatus;
        }
    }
}