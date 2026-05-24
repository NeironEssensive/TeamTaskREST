package com.example.team_task.controller;

import java.util.List;
import java.util.Optional;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> aboutMe(){
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/admin/all") 
    public ResponseEntity<List<UserResponse>> allUsers(){
        UserResponse currentUser = userService.getCurrentUser();
        if(currentUser.getRole().equals("USER")) throw new AccessDeniedException("Access denied");
        return ResponseEntity.ok(userService.getAllUsers());
    }
    

}
