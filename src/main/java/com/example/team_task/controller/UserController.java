package com.example.team_task.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.entity.User;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }
    @GetMapping("/me")
    public User aboutMe(){
        User user = userService.getCurrentUser().orElseThrow(() -> new UserNotFoundException("Current"));
        return user;
    }
    

}
