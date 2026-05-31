package com.example.team_task.service;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.team_task.entity.User.Role;
import com.example.team_task.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final RateLimitService rateLimitService;

    @Transactional
    public UserResponse saveUser(RegisterRequest request) {
        if (userRepository.existsByName(request.getName())) {
            throw new UserAlreadyExistException("username", request.getName());
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);
        return userService.mapToResponse(savedUser);
    }

    public User authenticate(String name, String password) {
        if (rateLimitService.isRegisterRateLimited()) {
            throw new TooManyRequestsException("Too many login attempts. Try again later.");
        }
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + name));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Login or password are wrong");
        }
        return user;
    }

    public LoginResponse login(LoginRequest request) {
        if (rateLimitService.isLoginRateLimited()) {
            throw new TooManyRequestsException("Too many login attempts. Try again later.");
        }
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new UserNotFoundException(request.getName()));
       
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid credentials");
        }
        rateLimitService.resetLoginAttempts();

        String accessToken = jwtService.generateToken(user.getName(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(
                user.getId(), user.getName(), user.getRole().name());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponse refresh(RefreshRequest request) {
        RefreshTokenData data = refreshTokenService.validateRefreshToken(
                request.getRefreshToken());

        String newAccessToken = jwtService.generateToken(data.getUsername(), data.getRole());
        refreshTokenService.deleteRefreshToken(request.getRefreshToken());
        String newRefreshToken = refreshTokenService.createRefreshToken(
                data.getUserId(), data.getUsername(), data.getRole());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.deleteRefreshToken(request.getRefreshToken());
    }

}
