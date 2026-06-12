package com.financeapp.backend.service.goal;

import com.financeapp.backend.dto.SavingsGoalResponse;
import com.financeapp.backend.dto.goal.GoalRequest;
import com.financeapp.backend.entity.SavingsGoal;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.SavingsGoalRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository goalRepository,
                              UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    public SavingsGoalResponse createGoal(String username, GoalRequest request) {
        if (request == null) {
            throw new RuntimeException("Goal details are required");
        }

        String goalName = safeTrim(request.getGoalName());
        Double targetAmount = request.getTargetAmount();
        Double currentAmount = request.getCurrentAmount();

        if (goalName == null) {
            throw new RuntimeException("Goal name is required");
        }
        if (targetAmount == null || targetAmount <= 0) {
            throw new RuntimeException("Target amount must be greater than 0");
        }
        if (currentAmount != null && currentAmount < 0) {
            throw new RuntimeException("Current amount cannot be negative");
        }

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(goalName);
        goal.setTargetAmount(targetAmount);
        goal.setCurrentAmount(currentAmount == null ? 0.0 : currentAmount);
        goal.setTargetDate(request.getTargetDate());
        goal.setUser(user);

        return toResponse(goalRepository.save(goal));
    }

    public List<SavingsGoalResponse> getGoals(String username) {

        List<SavingsGoal> goals = goalRepository.findByUserUsername(username);

        return goals.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SavingsGoalResponse getGoal(String username, Long goalId) {
        SavingsGoal goal = goalRepository.findByIdAndUserUsername(goalId, username)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        return toResponse(goal);
    }

    public SavingsGoalResponse updateGoal(String username, Long goalId, GoalRequest request) {
        if (request == null) {
            throw new RuntimeException("Goal details are required");
        }

        SavingsGoal goal = goalRepository.findByIdAndUserUsername(goalId, username)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        String goalName = safeTrim(request.getGoalName());
        if (goalName != null) {
            goal.setGoalName(goalName);
        }

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount() <= 0) {
                throw new RuntimeException("Target amount must be greater than 0");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getCurrentAmount() != null) {
            if (request.getCurrentAmount() < 0) {
                throw new RuntimeException("Current amount cannot be negative");
            }
            goal.setCurrentAmount(request.getCurrentAmount());
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        return toResponse(goalRepository.save(goal));
    }

    public void deleteGoal(String username, Long goalId) {
        SavingsGoal goal = goalRepository.findByIdAndUserUsername(goalId, username)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goalRepository.delete(goal);
    }

    public SavingsGoalResponse addMoneyToGoal(String username, Long goalId, Double amount) {

        if (amount == null || amount <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        SavingsGoal goal = goalRepository.findByIdAndUserUsername(goalId, username)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        double currentSavedAmount = goal.getCurrentAmount() == null ? 0.0 : goal.getCurrentAmount();
        goal.setCurrentAmount(currentSavedAmount + amount);

        SavingsGoal updatedGoal = goalRepository.save(goal);
        return toResponse(updatedGoal);
    }

    private SavingsGoalResponse toResponse(SavingsGoal goal) {
        double target = goal.getTargetAmount() == null ? 0.0 : goal.getTargetAmount();
        double saved = goal.getCurrentAmount() == null ? 0.0 : goal.getCurrentAmount();
        double progress = target > 0 ? (saved / target) * 100 : 0.0;

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetDate(),
                progress
        );
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}