package com.financeapp.backend.dto;

import com.financeapp.backend.entity.Frequency;
import java.time.LocalDate;

public class RecurringExpenseRequest {

    private String title;
    private Double amount;
    private String category;
    private LocalDate startDate;
    private LocalDate nextExecutionDate;
    private Frequency frequency;
    private String username;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public void setNextExecutionDate(LocalDate nextExecutionDate) { this.nextExecutionDate = nextExecutionDate; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}