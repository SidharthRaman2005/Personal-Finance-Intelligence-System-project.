package com.financeapp.backend.dto.analysis;

import java.util.List;

public class HiddenExpenseDetectorResponse {

    private final double threshold;
    private final double totalMonthlyExpense;
    private final double microTransactionTotal;
    private final int microTransactionCount;
    private final double microTransactionSharePercent;
    private final String thresholdLabel;
    private final String warningLevel;
    private final String insight;
    private final String savingsOpportunityInsight;
    private final List<CategorySpend> topCategories;

    public HiddenExpenseDetectorResponse(double threshold,
                                         double totalMonthlyExpense,
                                         double microTransactionTotal,
                                         int microTransactionCount,
                                         double microTransactionSharePercent,
                                         String thresholdLabel,
                                         String warningLevel,
                                         String insight,
                                         String savingsOpportunityInsight,
                                         List<CategorySpend> topCategories) {
        this.threshold = threshold;
        this.totalMonthlyExpense = totalMonthlyExpense;
        this.microTransactionTotal = microTransactionTotal;
        this.microTransactionCount = microTransactionCount;
        this.microTransactionSharePercent = microTransactionSharePercent;
        this.thresholdLabel = thresholdLabel;
        this.warningLevel = warningLevel;
        this.insight = insight;
        this.savingsOpportunityInsight = savingsOpportunityInsight;
        this.topCategories = topCategories;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getTotalMonthlyExpense() {
        return totalMonthlyExpense;
    }

    public double getMicroTransactionTotal() {
        return microTransactionTotal;
    }

    public int getMicroTransactionCount() {
        return microTransactionCount;
    }

    public double getMicroTransactionSharePercent() {
        return microTransactionSharePercent;
    }

    public String getThresholdLabel() {
        return thresholdLabel;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    public String getInsight() {
        return insight;
    }

    public String getSavingsOpportunityInsight() {
        return savingsOpportunityInsight;
    }

    public List<CategorySpend> getTopCategories() {
        return topCategories;
    }

    public static class CategorySpend {
        private final String category;
        private final double amount;
        private final double percent;

        public CategorySpend(String category, double amount, double percent) {
            this.category = category;
            this.amount = amount;
            this.percent = percent;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public double getPercent() {
            return percent;
        }
    }
}
