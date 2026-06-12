package com.financeapp.backend.repository;

import com.financeapp.backend.entity.Budget;
import com.financeapp.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Get budget for specific month & year
    Optional<Budget> findByUserAndMonthAndYear(User user, int month, int year);
}