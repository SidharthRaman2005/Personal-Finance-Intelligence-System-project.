package com.financeapp.backend.service.expense;

import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // ✅ ADD EXPENSE
    public Expense addExpense(String username,
                              String title,
                              String description,
                              Double amount,
                              String category,
                              LocalDate date) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    // ✅ GET USER EXPENSES
    public List<Expense> getUserExpenses(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return expenseRepository.findByUser(user);
    }

    // ✅ DELETE
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    // ✅ UPDATE
    public Expense updateExpense(Long id,
                                 String title,
                                 String description,
                                 Double amount,
                                 String category,
                                 LocalDate date) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setTitle(title);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);

        return expenseRepository.save(expense);
    }

    // ✅ DASHBOARD
    public Map<String, Object> getDashboardData(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Double totalExpense = expenseRepository.getTotalExpenseByUser(user);
        Long expenseCount = expenseRepository.countByUser(user);

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        Double thisMonthExpense =
                expenseRepository.getTotalExpenseByUserAndDateBetween(user, start, end);

        List<String> categories = expenseRepository.findTopCategory(user);
        String topCategory = categories.isEmpty() ? null : categories.get(0);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalExpense", totalExpense != null ? totalExpense : 0);
        dashboard.put("expenseCount", expenseCount);
        dashboard.put("thisMonthExpense", thisMonthExpense != null ? thisMonthExpense : 0);
        dashboard.put("topCategory", topCategory);

        return dashboard;
    }

    // ✅ MONTHLY SUMMARY
    public List<Expense> getMonthlyExpenses(String username, int month, int year) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return expenseRepository.findMonthlyExpenses(user, month, year);
    }

    // ✅ CATEGORY BREAKDOWN
    public Map<String, Double> getCategoryBreakdown(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> results = expenseRepository.getCategoryTotals(user);

        Map<String, Double> categoryTotals = new HashMap<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            Double total = (Double) row[1];
            categoryTotals.put(category, total);
        }

        return categoryTotals;
    }
}