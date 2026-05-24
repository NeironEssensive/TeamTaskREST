package com.example.team_task.dto.error;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandling {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "User not Found", ex.getMessage(), request);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.LOCKED, "Access denied", ex.getMessage(), request);
    }
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handeTaskNotFound(TaskNotFoundException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.NOT_FOUND, "Task not found", ex.getMessage(), request);
    }
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.NOT_FOUND, "Comment not found", ex.getMessage(), request);
    }
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.IM_USED, "User already exists", ex.getMessage(), request);
    }
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, "Login or password are wrong", ex.getMessage(), request);
    }
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse
         .builder()
        .timeStamp(LocalDateTime.now())
        .status(status.value())
        .error(error)
        .message(message)
        .path(request.getRequestURI())
        .build();
        return new ResponseEntity<>(response, status);
    }
}
