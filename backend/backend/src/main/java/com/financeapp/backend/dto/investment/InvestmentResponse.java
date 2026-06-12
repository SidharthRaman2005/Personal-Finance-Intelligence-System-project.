package com.financeapp.backend.dto.investment;

import java.time.LocalDate;

public class InvestmentResponse {

    private Long id;
    private String investmentType;
    private String investmentName;
    private Double investedAmount;
    private Double currentValue;
    private LocalDate purchaseDate;
    private String notes;
    private String riskLevel;

    public InvestmentResponse(Long id,
                              String investmentType,
                              String investmentName,
                              Double investedAmount,
                              Double currentValue,
                              LocalDate purchaseDate,
                              String notes,
                              String riskLevel) {
        this.id = id;
        this.investmentType = investmentType;
        this.investmentName = investmentName;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
        this.purchaseDate = purchaseDate;
        this.notes = notes;
        this.riskLevel = riskLevel;
    }

    public Long getId() {
        return id;
    }

    public String getInvestmentType() {
        return investmentType;
    }

    public String getInvestmentName() {
        return investmentName;
    }

    public Double getInvestedAmount() {
        return investedAmount;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public String getNotes() {
        return notes;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}
