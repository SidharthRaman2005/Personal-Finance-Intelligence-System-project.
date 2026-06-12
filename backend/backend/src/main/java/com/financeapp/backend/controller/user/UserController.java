package com.financeapp.backend.controller.user;

import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.UserRepository;
import com.financeapp.backend.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/budget")
    public ApiResponse<String> setMonthlyBudget(Authentication authentication,
                                                @RequestParam Double budget) {

        String username = authentication.getName();

        User user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMonthlyBudget(budget);
        userRepository.save(user);

        return new ApiResponse<>(
                true,
                "Monthly budget saved successfully",
                null
        );
    }
}
