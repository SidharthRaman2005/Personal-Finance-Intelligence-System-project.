package com.financeapp.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "investments")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InvestmentType investmentType;

    private String investmentName;

    private Double investedAmount;

    private Double currentValue;

    private LocalDate purchaseDate;

    @Column(length = 2000)
    private String notes;

    private String riskLevel;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Investment() {
    }

    public Investment(InvestmentType investmentType,
                      String investmentName,
                      Double investedAmount,
                      Double currentValue,
                      LocalDate purchaseDate,
                      String notes,
                      String riskLevel,
                      User user) {
        this.investmentType = investmentType;
        this.investmentName = investmentName;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
        this.purchaseDate = purchaseDate;
        this.notes = notes;
        this.riskLevel = riskLevel;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public InvestmentType getInvestmentType() {
        return investmentType;
    }

    public void setInvestmentType(InvestmentType investmentType) {
        this.investmentType = investmentType;
    }

    public String getInvestmentName() {
        return investmentName;
    }

    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }

    public Double getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(Double investedAmount) {
        this.investedAmount = investedAmount;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
