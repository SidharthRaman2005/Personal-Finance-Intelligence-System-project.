package com.financeapp.backend.controller.budget;

import com.financeapp.backend.entity.Budget;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.budget.BudgetService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // ✅ Set or Update Budget
    @PostMapping("/set")
        public ApiResponse<Budget> setBudget(Authentication authentication,
                                                                                 @RequestParam int month,
                                         @RequestParam int year,
                                         @RequestParam Double amount) {

                String username = authentication.getName();

        Budget budget = budgetService.setBudget(username, month, year, amount);

        return new ApiResponse<>(
                true,
                "Budget saved successfully",
                budget
        );
    }

    // ✅ Get Budget
    @GetMapping("/get")
        public ApiResponse<Budget> getBudget(Authentication authentication,
                                                                                 @RequestParam int month,
                                         @RequestParam int year) {

                String username = authentication.getName();

        Budget budget = budgetService.getBudget(username, month, year);

        return new ApiResponse<>(
                true,
                "Budget fetched successfully",
                budget
        );
    }

    // 🚀 NEW: Budget vs Actual
    @GetMapping("/vs-actual")
    public ApiResponse<Map<String, Object>> getBudgetVsActual(
                        Authentication authentication,
            @RequestParam int month,
            @RequestParam int year) {

                String username = authentication.getName();

        Map<String, Object> result =
                budgetService.getBudgetVsActual(username, month, year);

        return new ApiResponse<>(
                true,
                "Budget vs Actual fetched successfully",
                result
        );
    }
}