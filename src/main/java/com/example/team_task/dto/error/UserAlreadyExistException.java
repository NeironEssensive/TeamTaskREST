package com.example.team_task.dto.error;
public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String field, String value){
        super("User with " + field + " : " + value + " already exists");
    }
}
