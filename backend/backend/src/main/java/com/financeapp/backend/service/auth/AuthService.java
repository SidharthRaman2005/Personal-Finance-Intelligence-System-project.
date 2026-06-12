package com.financeapp.backend.service.auth;

import com.financeapp.backend.dto.auth.LoginResponse;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // ===============================
    // SIGNUP (Direct create user)
    // ===============================
    public void startSignup(String username, String email, String password) {

        if (isBlank(username) || isBlank(email) || isBlank(password)) {
            throw new RuntimeException("Username, email, and password are required");
        }

        String normalizedUsername = username.trim();
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User(normalizedUsername, normalizedEmail, passwordEncoder.encode(password));
        userRepository.save(user);
    }

    // ===============================
    // LOGIN
    // ===============================
    public boolean login(String username, String password) {
        return authenticateUser(username, password).isPresent();
    }

    public LoginResponse loginAndGenerateToken(String username, String password) {

        Optional<User> authenticatedUser = authenticateUser(username, password);
        if (authenticatedUser.isEmpty()) {
            return null;
        }

        User user = authenticatedUser.get();
        String token = jwtService.generateToken(user.getUsername());

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getUsername()
        );
    }

    private Optional<User> authenticateUser(String username, String password) {

        if (isBlank(username) || isBlank(password)) {
            return Optional.empty();
        }

        return userRepository.findByUsernameIgnoreCase(username.trim())
                .filter(user -> matchesAndMigratePassword(user, password));
    }

    private boolean matchesAndMigratePassword(User user, String rawPassword) {
        String storedPassword = user.getPassword();

        if (storedPassword == null) {
            return false;
        }

        try {
            if (passwordEncoder.matches(rawPassword, storedPassword)) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
            // Existing plain-text password rows are migrated below on successful login.
        }

        if (storedPassword.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
