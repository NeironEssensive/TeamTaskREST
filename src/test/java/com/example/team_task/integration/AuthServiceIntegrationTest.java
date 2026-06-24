package com.example.team_task.integration;

import com.example.team_task.dto.auth.LoginRequest;
import com.example.team_task.dto.auth.LoginResponse;
import com.example.team_task.dto.auth.RegisterRequest;
import com.example.team_task.dto.error.UserAlreadyExistException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void saveUser_ValidRequest_CreatesAndReturnsUser() {
        RegisterRequest request = RegisterRequest.builder()
                .name("newuser")
                .email("newuser@example.com")
                .password("password123")
                .build();

        UserResponse response = authService.saveUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void saveUser_DuplicateUsername_ThrowsException() {
        RegisterRequest firstRequest = RegisterRequest.builder()
                .name("duplicateuser")
                .email("first@example.com")
                .password("password123")
                .build();

        authService.saveUser(firstRequest);

        RegisterRequest secondRequest = RegisterRequest.builder()
                .name("duplicateuser")
                .email("second@example.com")
                .password("password123")
                .build();

        assertThatThrownBy(() -> authService.saveUser(secondRequest))
                .isInstanceOf(UserAlreadyExistException.class);
    }

    @Test
    void login_ValidCredentials_ReturnsTokens() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("loginuser")
                .email("loginuser@example.com")
                .password("password123")
                .build();

        authService.saveUser(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .name("loginuser")
                .password("password123")
                .build();

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotEmpty();
    }
}