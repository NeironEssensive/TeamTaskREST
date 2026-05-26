package com.example.team_task.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.auth.RegisterRequest;
import com.example.team_task.dto.error.UserAlreadyExistException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;
import com.example.team_task.service.AuthService;
import com.example.team_task.service.JwtService;
import com.example.team_task.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;


    public AuthController(AuthService authService, JwtService jwtService, PasswordEncoder passwordEncoder,
            UserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.saveUser(request));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginData) {
        String name = loginData.get("name");
        String password = loginData.get("password");
        User user = authService.authenticate(name, password);
        String token = jwtService.generateToken(name, user.getRole().toString());
        return Map.of("token", token);
    }
}
