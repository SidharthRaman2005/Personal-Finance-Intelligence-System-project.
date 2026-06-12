package com.financeapp.backend.controller.goal;

import com.financeapp.backend.dto.SavingsGoalResponse;
import com.financeapp.backend.dto.goal.GoalRequest;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.goal.SavingsGoalService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService goalService;

    public SavingsGoalController(SavingsGoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ApiResponse<SavingsGoalResponse> createGoal(Authentication authentication,
                                                       @RequestBody GoalRequest request) {
        return new ApiResponse<>(
                true,
                "Goal created",
                goalService.createGoal(authentication.getName(), request)
        );
    }

    @GetMapping
    public ApiResponse<List<SavingsGoalResponse>> getGoals(Authentication authentication) {
        return new ApiResponse<>(
                true,
                "Goals fetched",
                goalService.getGoals(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<SavingsGoalResponse> getGoal(Authentication authentication,
                                                    @PathVariable Long id) {
        return new ApiResponse<>(
                true,
                "Goal fetched",
                goalService.getGoal(authentication.getName(), id)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<SavingsGoalResponse> updateGoal(Authentication authentication,
                                                       @PathVariable Long id,
                                                       @RequestBody GoalRequest request) {
        return new ApiResponse<>(
                true,
                "Goal updated",
                goalService.updateGoal(authentication.getName(), id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteGoal(Authentication authentication,
                                          @PathVariable Long id) {
        goalService.deleteGoal(authentication.getName(), id);
        return new ApiResponse<>(true, "Goal deleted", null);
    }

    @PostMapping("/{id}/add-money")
    public ApiResponse<SavingsGoalResponse> addMoney(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @RequestParam Double amount) {
        return new ApiResponse<>(
                true,
                "Goal updated",
                goalService.addMoneyToGoal(authentication.getName(), id, amount)
        );
    }
}