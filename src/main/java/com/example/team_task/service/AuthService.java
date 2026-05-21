package com.example.team_task.service;

import org.springframework.lang.NonNull;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;

import jakarta.validation.ValidationException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(@NonNull User user){
        return userRepository.save(user);
    }

    public User authenticate(String name, String password){
       User user = userRepository.findByName(name)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + name));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Invalid password");
        }
        return user;
    }
}
