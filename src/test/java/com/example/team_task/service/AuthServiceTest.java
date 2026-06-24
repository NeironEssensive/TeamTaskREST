package com.example.team_task.service;
import com.example.team_task.dto.auth.LoginRequest;
import com.example.team_task.dto.auth.LoginResponse;
import com.example.team_task.dto.auth.LogoutRequest;
import com.example.team_task.dto.auth.RefreshRequest;
import com.example.team_task.dto.auth.RefreshTokenData;
import com.example.team_task.dto.auth.RegisterRequest;
import com.example.team_task.dto.error.TooManyRequestsException;
import com.example.team_task.dto.error.UserAlreadyExistException;
import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

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

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
    }

    @Test
    void saveUser_ValidRequest_CreatesUserSuccessfully() {
        when(userRepository.existsByName(registerRequest.getName())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
        when(userService.mapToResponse(testUser)).thenReturn(expectedResponse);

        UserResponse actualResponse = authService.saveUser(registerRequest);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(1L);
        assertThat(actualResponse.getName()).isEqualTo("testuser");
        assertThat(actualResponse.getEmail()).isEqualTo("test@example.com");
        assertThat(actualResponse.getRole()).isEqualTo("USER");

        verify(userRepository).existsByName("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userService).mapToResponse(testUser);
    }

    @Test
    void saveUser_UsernameAlreadyExists_ThrowsUserAlreadyExistException() {
        when(userRepository.existsByName(registerRequest.getName())).thenReturn(true);

        assertThatThrownBy(() -> authService.saveUser(registerRequest))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessageContaining("testuser");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveUser_NewUser_HasUserRole() {
        when(userRepository.existsByName(registerRequest.getName())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userService.mapToResponse(any(User.class))).thenReturn(UserResponse.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .role("USER")
                .build());

        authService.saveUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void saveUser_EncodesPasswordBeforeSaving() {
        when(userRepository.existsByName(registerRequest.getName())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userService.mapToResponse(any(User.class))).thenReturn(UserResponse.builder().build());

        authService.saveUser(registerRequest);

        verify(passwordEncoder).encode("password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashedPassword123");
    }

    @Test
    void authenticate_ValidCredentials_ReturnsUser() {
        when(rateLimitService.isRegisterRateLimited()).thenReturn(false);
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = authService.authenticate("testuser", "password123");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("testuser");
    }

    @Test
    void authenticate_RateLimited_ThrowsTooManyRequestsException() {
        when(rateLimitService.isRegisterRateLimited()).thenReturn(true);

        assertThatThrownBy(() -> authService.authenticate("testuser", "password123"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Too many login attempts");

        verify(userRepository, never()).findByName(anyString());
    }

    @Test
    void authenticate_UserNotFound_ThrowsUserNotFoundException() {
        when(rateLimitService.isRegisterRateLimited()).thenReturn(false);
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("nonexistent", "password123"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void authenticate_WrongPassword_ThrowsValidationException() {
        when(rateLimitService.isRegisterRateLimited()).thenReturn(false);
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate("testuser", "wrongPassword"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Login or password are wrong");
    }

    @Test
    void login_ValidCredentials_ReturnsTokens() {
        when(rateLimitService.isLoginRateLimited()).thenReturn(false);
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("testuser", "USER")).thenReturn("access-token-value");
        when(refreshTokenService.createRefreshToken(1L, "testuser", "USER"))
                .thenReturn("refresh-token-value");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-value");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-value");
    }

    @Test
    void login_RateLimited_ThrowsTooManyRequestsException() {
        when(rateLimitService.isLoginRateLimited()).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Too many login attempts");

        verify(userRepository, never()).findByName(anyString());
    }

    @Test
    void login_UserNotFound_ThrowsUserNotFoundException() {
        when(rateLimitService.isLoginRateLimited()).thenReturn(false);
        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("testuser");
    }

    @Test
    void login_WrongPassword_ThrowsValidationException() {
        when(rateLimitService.isLoginRateLimited()).thenReturn(false);
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_SuccessfulLogin_ResetsRateLimit() {
        when(rateLimitService.isLoginRateLimited()).thenReturn(false);
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(), anyString(), anyString()))
                .thenReturn("refresh-token");

        authService.login(loginRequest);

        verify(rateLimitService).resetLoginAttempts();
    }

    @Test
    void refresh_ValidRefreshToken_ReturnsNewTokenPair() {
        RefreshRequest refreshRequest = new RefreshRequest("old-refresh-token");
        RefreshTokenData tokenData = RefreshTokenData.builder()
                .userId(1L)
                .username("testuser")
                .role("USER")
                .build();

        when(refreshTokenService.validateRefreshToken("old-refresh-token")).thenReturn(tokenData);
        when(jwtService.generateToken("testuser", "USER")).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(1L, "testuser", "USER"))
                .thenReturn("new-refresh-token");

        LoginResponse response = authService.refresh(refreshRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refresh_ValidRefreshToken_DeletesOldToken() {
        RefreshRequest refreshRequest = new RefreshRequest("old-refresh-token");
        RefreshTokenData tokenData = RefreshTokenData.builder()
                .userId(1L)
                .username("testuser")
                .role("USER")
                .build();

        when(refreshTokenService.validateRefreshToken("old-refresh-token")).thenReturn(tokenData);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(any(), anyString(), anyString()))
                .thenReturn("new-refresh-token");

        authService.refresh(refreshRequest);

        verify(refreshTokenService).deleteRefreshToken("old-refresh-token");
    }

    @Test
    void logout_ValidRequest_DeletesRefreshToken() {
        LogoutRequest logoutRequest = new LogoutRequest("token-to-delete");

        authService.logout(logoutRequest);

        verify(refreshTokenService).deleteRefreshToken("token-to-delete");
    }
}