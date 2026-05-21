package com.example.team_task.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.UserAlreadyExistException;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;
import com.example.team_task.service.AuthService;
import com.example.team_task.service.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    public AuthController(AuthService authService, JwtService jwtService, PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        Optional<User> validUser = userRepository.findByName(user.getName());

        if (validUser.isPresent()) throw new UserAlreadyExistException("username", user.getName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return authService.saveUser(user);
        
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
