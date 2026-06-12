package com.financeapp.backend.service.auth;

import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtService);
        lenient().when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        lenient().when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldRejectWhenEmailAlreadyRegistered() {
        when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.startSignup("newuser", "taken@example.com", "password123"));

        assertTrue(ex.getMessage().contains("Email already registered"));
    }

    @Test
    void shouldRejectWhenUsernameAlreadyTaken() {
        when(userRepository.existsByUsernameIgnoreCase("existinguser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.startSignup("existinguser", "new@example.com", "password123"));

        assertTrue(ex.getMessage().contains("Username already taken"));
    }

    @Test
    void shouldHashPasswordBeforeSave() {
        String email = "hash@example.com";
        String rawPassword = "password123";

        authService.startSignup("hashuser", email, rawPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertNotEquals(rawPassword, saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));
    }
}
