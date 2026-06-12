package com.financeapp.backend.controller.auth;

import com.financeapp.backend.dto.auth.*;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.auth.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Signup
    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody SignupRequest request) {
        try {
            authService.startSignup(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );

            return new ApiResponse<>(
                    true,
                    "Signup successful. Please login.",
                    null
            );
        } catch (RuntimeException ex) {
            return new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    null
            );
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {

        LoginResponse loginResponse = authService.loginAndGenerateToken(
                request.getUsername(),
                request.getPassword()
        );

        if (loginResponse != null) {
            return new ApiResponse<>(
                    true,
                    "Login successful 🚀",
                    loginResponse
            );
        }

        return new ApiResponse<>(
                false,
                "Invalid credentials",
                null
        );
    }
}
