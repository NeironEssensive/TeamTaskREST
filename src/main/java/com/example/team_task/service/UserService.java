package com.example.team_task.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;


import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;



@Service
public class UserService {
    private UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public Optional<User> getCurrentUser(){
        String username = getCurrentUsername();
        User user = userRepository.findByName(username).orElseThrow(() -> new UserNotFoundException(username));
        return Optional.ofNullable(user);
    }
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        
        return authentication.getName();
    }


    
    
}
