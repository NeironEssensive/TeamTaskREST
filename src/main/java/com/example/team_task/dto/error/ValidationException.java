package com.example.team_task.dto.error;

public class ValidationException extends RuntimeException{
    public ValidationException(String message){
        super("data not correct");
    }
}
