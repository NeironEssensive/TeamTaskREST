package com.example.team_task.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.team_task.config.SecurityConfig;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;

import jakarta.validation.ValidationException;

@Service
public class UserService {
    private UserRepository userRepository;
    private SecurityConfig securityConfig;
    public UserService(UserRepository userRepository, SecurityConfig securityConfig){
        this.userRepository = userRepository;
        this.securityConfig = securityConfig;
    }
    
    
}
