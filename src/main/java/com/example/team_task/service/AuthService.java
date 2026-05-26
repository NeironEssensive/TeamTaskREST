package com.example.team_task.service;

import org.springframework.lang.NonNull;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team_task.dto.auth.RegisterRequest;
import com.example.team_task.dto.error.UserAlreadyExistException;
import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.entity.User.Role;
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
    public UserResponse saveUser(RegisterRequest request){
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
    
    public User authenticate(String name, String password){
       User user = userRepository.findByName(name)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + name));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Login or password are wrong");
        }
        return user;
    }
}
