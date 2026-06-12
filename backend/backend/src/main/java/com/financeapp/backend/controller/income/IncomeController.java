package com.financeapp.backend.controller.income;

import com.financeapp.backend.entity.Income;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.income.IncomeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/income")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    // ✅ ADD INCOME
    @PostMapping("/add")
    public ApiResponse<Income> addIncome(Authentication authentication,
                                         @RequestBody Income income) {

        String username = authentication.getName();

        Income saved = incomeService.addIncome(
                username,
                income.getSource(),
                income.getAmount(),
                income.getDate()
        );

        return new ApiResponse<>(true, "Income added", saved);
    }

    // ✅ GET USER INCOME
    @GetMapping("/user")
    public ApiResponse<List<Income>> getUserIncome(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Income fetched",
                incomeService.getUserIncome(username)
        );
    }

    // ✅ TOTAL INCOME
    @GetMapping("/summary/total")
    public ApiResponse<Double> getTotalIncome(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Total income fetched",
                incomeService.getTotalIncome(username)
        );
    }

    // ✅ MONTHLY INCOME
    @GetMapping("/summary/monthly")
    public ApiResponse<Double> getMonthlyIncome(Authentication authentication,
                                                @RequestParam int month,
                                                @RequestParam int year) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Monthly income fetched",
                incomeService.getMonthlyIncome(username, month, year)
        );
    }

    // ✅ INCOME BY DATE RANGE
    @GetMapping("/summary/range")
    public ApiResponse<Double> getIncomeByRange(Authentication authentication,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Income by date range fetched",
                incomeService.getIncomeByDateRange(
                        username,
                        LocalDate.parse(startDate),
                        LocalDate.parse(endDate)
                )
        );
    }

    // ✅ SOURCE BREAKDOWN (Pie Chart Ready)
    @GetMapping("/summary/source")
    public ApiResponse<Map<String, Double>> getSourceBreakdown(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Source breakdown fetched",
                incomeService.getSourceBreakdown(username)
        );
    }

    // ✅ UPDATE INCOME
    @PutMapping("/{id}")
    public ApiResponse<Income> updateIncome(@PathVariable Long id,
                                            @RequestBody Income income) {

        Income updated = incomeService.updateIncome(
                id,
                income.getSource(),
                income.getAmount(),
                income.getDate()
        );

        return new ApiResponse<>(true, "Income updated", updated);
    }

    // ✅ DELETE INCOME
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteIncome(@PathVariable Long id) {

        incomeService.deleteIncome(id);

        return new ApiResponse<>(true, "Income deleted", null);
    }
}