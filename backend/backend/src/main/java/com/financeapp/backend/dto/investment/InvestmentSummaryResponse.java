package com.financeapp.backend.dto.investment;

import java.util.List;
import java.util.Map;

public class InvestmentSummaryResponse {

    private Double totalInvested;
    private Double totalCurrentValue;
    private Double totalProfitLoss;
    private Map<String, Double> allocationPercentageByType;
    private Double diversificationScore;
    private String highestInvestmentCategory;
    private List<String> insights;

    public InvestmentSummaryResponse(Double totalInvested,
                                     Double totalCurrentValue,
                                     Double totalProfitLoss,
                                     Map<String, Double> allocationPercentageByType,
                                     Double diversificationScore,
                                     String highestInvestmentCategory,
                                     List<String> insights) {
        this.totalInvested = totalInvested;
        this.totalCurrentValue = totalCurrentValue;
        this.totalProfitLoss = totalProfitLoss;
        this.allocationPercentageByType = allocationPercentageByType;
        this.diversificationScore = diversificationScore;
        this.highestInvestmentCategory = highestInvestmentCategory;
        this.insights = insights;
    }

    public Double getTotalInvested() {
        return totalInvested;
    }

    public Double getTotalCurrentValue() {
        return totalCurrentValue;
    }

    public Double getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public Map<String, Double> getAllocationPercentageByType() {
        return allocationPercentageByType;
    }

    public Double getDiversificationScore() {
        return diversificationScore;
    }

    public String getHighestInvestmentCategory() {
        return highestInvestmentCategory;
    }

    public List<String> getInsights() {
        return insights;
    }
}
