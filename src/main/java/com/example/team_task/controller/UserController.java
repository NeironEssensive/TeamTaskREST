package com.example.team_task.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> aboutMe() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserResponse>> allUsers() {
        UserResponse currentUser = userService.getCurrentUser();
        if (currentUser.getRole().equals("USER"))
            throw new AccessDeniedException("Access denied");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        UserResponse current = userService.getCurrentUser();
        if (!current.getRole().equals("ADMIN"))
            throw new AccessDeniedException();
        userService.deleteUser(id);
    }

}
