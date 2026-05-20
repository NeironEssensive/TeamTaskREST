package com.example.team_task.controller;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.entity.User;
import com.example.team_task.service.AuthService;
import com.example.team_task.service.JwtService;

import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    public AuthController(AuthService authService, JwtService jwtService, PasswordEncoder passwordEncoder){
        this.authService = authService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/register")
    public User register(@RequestBody User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return authService.saveUser(user);
    }
    
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginData){
        String name = loginData.get("name");
        String password = loginData.get("password");
        User user = authService.authenticate(name, password);
        String token = jwtService.generateToken(name, user.getRole().toString());
        return Map.of("token", token);
    }
}
