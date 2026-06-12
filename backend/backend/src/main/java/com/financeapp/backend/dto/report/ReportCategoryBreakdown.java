package com.financeapp.backend.dto.report;

public class ReportCategoryBreakdown {

    private final String category;
    private final Double totalAmount;
    private final Double percentage;

    public ReportCategoryBreakdown(String category, Double totalAmount, Double percentage) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Double getPercentage() {
        return percentage;
    }
}
