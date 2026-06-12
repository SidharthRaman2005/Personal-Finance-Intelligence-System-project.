package com.financeapp.backend.repository;

import com.financeapp.backend.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

    // List<RecurringExpense> findByNextExecutionDate(LocalDate date);
    List<RecurringExpense> findByNextExecutionDateLessThanEqual(LocalDate date);
}