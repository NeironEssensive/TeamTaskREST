package com.example.team_task.dto.error;

public class UserNotFoundException extends RuntimeException{
    
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }

    public UserNotFoundException(String name) {
        super("User with name '" + name + "' not found");
    }
}
