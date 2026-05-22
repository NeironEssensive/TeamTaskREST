package com.example.team_task.dto.error;

public class CommentNotFoundException extends RuntimeException{
    public CommentNotFoundException(Long id){
        super("Comment with id " + id + " not found");
    }
}
