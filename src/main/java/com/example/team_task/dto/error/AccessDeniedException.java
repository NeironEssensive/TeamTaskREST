package com.example.team_task.dto.error;

public class AccessDeniedException extends RuntimeException{
    public AccessDeniedException(){
        super("Access denied");
    }
    public AccessDeniedException(String message){
        super(message);
    }
}
