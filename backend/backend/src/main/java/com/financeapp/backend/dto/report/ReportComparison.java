package com.financeapp.backend.dto.report;

public class ReportComparison {

    private final Double currentValue;
    private final Double previousValue;
    private final Double percentChange;
    private final String direction;

    public ReportComparison(Double currentValue,
                            Double previousValue,
                            Double percentChange,
                            String direction) {
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.percentChange = percentChange;
        this.direction = direction;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public Double getPreviousValue() {
        return previousValue;
    }

    public Double getPercentChange() {
        return percentChange;
    }

    public String getDirection() {
        return direction;
    }
}
