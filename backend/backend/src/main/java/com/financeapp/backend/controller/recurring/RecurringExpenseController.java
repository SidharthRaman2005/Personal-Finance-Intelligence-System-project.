package com.financeapp.backend.controller.recurring;

import com.financeapp.backend.dto.RecurringExpenseRequest;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.recurring.RecurringExpenseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring")
public class RecurringExpenseController {

    private final RecurringExpenseService service;

    public RecurringExpenseController(RecurringExpenseService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ApiResponse<String> addRecurring(@RequestBody RecurringExpenseRequest request) {

        service.addRecurringExpense(
                request.getUsername(),
                request.getTitle(),
                request.getAmount(),
                request.getCategory(),
                request.getStartDate(),
                request.getNextExecutionDate(),
                request.getFrequency()
        );

        return new ApiResponse<>(true, "Recurring expense added", null);
    }

    @PostMapping("/generate")
    public ApiResponse<String> generateRecurring() {

        service.generateRecurringExpenses();

        return new ApiResponse<>(true, "Recurring expenses generated", null);
    }
}