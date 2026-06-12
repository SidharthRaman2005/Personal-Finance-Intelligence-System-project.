package com.financeapp.backend.service.dashboard;

import com.financeapp.backend.dto.DashboardResponse;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.IncomeRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class DashboardService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public DashboardService(IncomeRepository incomeRepository,
                            ExpenseRepository expenseRepository,
                            UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getDashboard(String username) {

        // 🔥 FIX 1: Remove unwanted spaces
        final String trimmedUsername = username.trim();

        // 🔍 DEBUG (you can remove later)
        System.out.println("Username received: '" + trimmedUsername + "'");

        // ✅ Total Income
        double totalIncome = incomeRepository
                .findByUserUsername(trimmedUsername)
                .stream()
                .mapToDouble(i -> i.getAmount())
                .sum();

        // ✅ Get User safely (ignore case)
        User user = userRepository.findByUsernameIgnoreCase(trimmedUsername)
                .orElseThrow(() -> {
                    System.out.println("❌ User NOT FOUND for username: " + trimmedUsername);
                    return new RuntimeException("User not found");
                });

        System.out.println("✅ User FOUND: " + user.getUsername());

        // ✅ Total Expense
        Double totalExpense = expenseRepository.getTotalExpenseByUser(user);

        if (totalExpense == null) {
            totalExpense = 0.0;
        }

        // ✅ Net Balance
        double netBalance = totalIncome - totalExpense;

        // ✅ Budget metrics for current month
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        Double currentMonthExpense = expenseRepository
                .getTotalExpenseByUserAndDateBetween(user, start, end);

        if (currentMonthExpense == null) {
            currentMonthExpense = 0.0;
        }

        Double monthlyBudget = user.getMonthlyBudget();
        Double remainingBudget = null;
        Double percentageUsed = 0.0;

        if (monthlyBudget != null) {
            remainingBudget = monthlyBudget - currentMonthExpense;
            if (monthlyBudget > 0) {
                percentageUsed = (currentMonthExpense / monthlyBudget) * 100;
            }
        }

        return new DashboardResponse(
                totalIncome,
                totalExpense,
                netBalance,
                monthlyBudget,
                remainingBudget,
                percentageUsed
        );
    }
}