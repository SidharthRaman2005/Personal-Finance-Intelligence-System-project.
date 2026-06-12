package com.financeapp.backend.bank.model;

import java.time.LocalDate;

public class BankTransaction {

    private LocalDate date;
    private String description;
    private Double amount;
    private String type; // CREDIT / DEBIT

    public BankTransaction(LocalDate date, String description, Double amount, String type) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }
}