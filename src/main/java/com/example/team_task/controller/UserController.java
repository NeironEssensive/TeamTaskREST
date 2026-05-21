package com.example.team_task.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.entity.User;
import com.example.team_task.service.UserService;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {
    private UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }
    

}
