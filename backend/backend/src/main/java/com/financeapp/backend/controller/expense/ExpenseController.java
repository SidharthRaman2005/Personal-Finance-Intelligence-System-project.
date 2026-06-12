package com.financeapp.backend.controller.expense;

import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.expense.ExpenseService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // ✅ ADD
    @PostMapping("/add")
    public ApiResponse<Expense> addExpense(Authentication authentication,
                                           @RequestBody Expense expense) {

        String username = authentication.getName();

        Expense saved = expenseService.addExpense(
                username,
                expense.getTitle(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );

        return new ApiResponse<>(true, "Expense added", saved);
    }

    // ✅ GET USER EXPENSES
    @GetMapping("/user")
    public ApiResponse<List<Expense>> getUserExpenses(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(true,
                "Expenses fetched",
                expenseService.getUserExpenses(username));
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteExpense(@PathVariable Long id) {

        expenseService.deleteExpense(id);
        return new ApiResponse<>(true, "Deleted successfully", null);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ApiResponse<Expense> updateExpense(@PathVariable Long id,
                                              @RequestBody Expense expense) {

        Expense updated = expenseService.updateExpense(
                id,
                expense.getTitle(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );

        return new ApiResponse<>(true, "Updated successfully", updated);
    }

    // ✅ DASHBOARD
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(true,
                "Dashboard fetched",
                expenseService.getDashboardData(username));
    }

    // ✅ MONTHLY SUMMARY
    @GetMapping("/summary/monthly")
    public ApiResponse<List<Expense>> monthlySummary(Authentication authentication,
                                                     @RequestParam int month,
                                                     @RequestParam int year) {

        String username = authentication.getName();

        return new ApiResponse<>(true,
                "Monthly summary",
                expenseService.getMonthlyExpenses(username, month, year));
    }

    // ✅ CATEGORY BREAKDOWN
    @GetMapping("/summary/category")
    public ApiResponse<Map<String, Double>> categoryBreakdown(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(true,
                "Category breakdown",
                expenseService.getCategoryBreakdown(username));
    }
}