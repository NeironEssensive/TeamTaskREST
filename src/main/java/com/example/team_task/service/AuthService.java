package com.example.team_task.service;

import org.springframework.lang.NonNull;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }
    @Transactional
    public UserResponse saveUser(@NonNull User user){
        return userService.mapToResponse(userRepository.save(user));
    }
    
    public User authenticate(String name, String password){
       User user = userRepository.findByName(name)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + name));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Login or password are wrong");
        }
        return user;
    }
}
