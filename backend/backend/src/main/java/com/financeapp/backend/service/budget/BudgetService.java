package com.financeapp.backend.service.budget;

import com.financeapp.backend.entity.Budget;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.BudgetRepository;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         UserRepository userRepository,
                         ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    // ✅ Set or Update Monthly Budget
    public Budget setBudget(String username, int month, int year, Double amount) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Budget> existingBudget =
                budgetRepository.findByUserAndMonthAndYear(user, month, year);

        if (existingBudget.isPresent()) {
            Budget budget = existingBudget.get();
            budget.setAmount(amount);
            return budgetRepository.save(budget);
        } else {
            Budget budget = new Budget();
            budget.setUser(user);
            budget.setMonth(month);
            budget.setYear(year);
            budget.setAmount(amount);
            return budgetRepository.save(budget);
        }
    }

    // ✅ Get Budget For Month
    public Budget getBudget(String username, int month, int year) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return budgetRepository.findByUserAndMonthAndYear(user, month, year)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    // 🚀 NEW FEATURE: Budget vs Actual
    public Map<String, Object> getBudgetVsActual(String username, int month, int year) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findByUserAndMonthAndYear(user, month, year)
                .orElseThrow(() -> new RuntimeException("Budget not set for this month"));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        Double totalSpent =
                expenseRepository.getTotalExpenseByUserAndDateBetween(user, start, end);

        totalSpent = totalSpent != null ? totalSpent : 0.0;

        Double budgetAmount = budget.getAmount();
        Double remaining = budgetAmount - totalSpent;

        boolean overspent = totalSpent > budgetAmount;

        double percentageUsed = budgetAmount > 0
                ? (totalSpent / budgetAmount) * 100
                : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("budgetAmount", budgetAmount);
        response.put("totalSpent", totalSpent);
        response.put("remainingAmount", remaining);
        response.put("overspent", overspent);
        response.put("percentageUsed", percentageUsed);

        return response;
    }
}