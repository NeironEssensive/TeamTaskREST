package com.example.team_task.controller;

import com.example.team_task.dto.auth.LoginRequest;
import com.example.team_task.dto.auth.LoginResponse;
import com.example.team_task.dto.auth.LogoutRequest;
import com.example.team_task.dto.auth.RefreshRequest;
import com.example.team_task.dto.auth.RegisterRequest;
import com.example.team_task.dto.error.ErrorResponse;
import com.example.team_task.dto.error.TooManyRequestsException;
import com.example.team_task.dto.error.UserAlreadyExistException;
import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.service.AuthService;
import com.example.team_task.service.JwtService;
import com.example.team_task.service.RateLimitService;
import com.example.team_task.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    @MockBean
    private RateLimitService rateLimitService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .name("testuser")
                .password("password123")
                .build();
    }

    @Test
    void register_ValidRequest_ReturnsCreated() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.saveUser(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_UserAlreadyExists_ReturnsImUsed() throws Exception {
        when(authService.saveUser(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistException("username", "testuser"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isImUsed())
                .andExpect(jsonPath("$.status").value(226))
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    @Test
    void register_InvalidName_ReturnsBadRequest() throws Exception {
        registerRequest.setName("ab");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        registerRequest.setEmail("not-an-email");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortPassword_ReturnsBadRequest() throws Exception {
        registerRequest.setPassword("12345");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_BlankName_ReturnsBadRequest() throws Exception {
        registerRequest.setName("");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidCredentials_ReturnsOkWithTokens() throws Exception {
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"));
    }

    @Test
    void login_UserNotFound_ReturnsNotFound() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UserNotFoundException("testuser"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not Found"));
    }

    @Test
    void login_WrongPassword_ReturnsConflict() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ValidationException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("wrong data"));
    }

    @Test
    void login_TooManyRequests_ReturnsTooManyRequests() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new TooManyRequestsException("Too many attempts"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void refresh_ValidToken_ReturnsNewTokenPair() throws Exception {
        RefreshRequest refreshRequest = new RefreshRequest("old-refresh-token");
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void logout_ValidToken_ReturnsNoContent() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest("token-to-delete");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        verify(authService).logout(any(LogoutRequest.class));
    }
}