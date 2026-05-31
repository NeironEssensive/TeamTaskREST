package com.example.team_task.dto.error;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Hidden 
public class GlobalExceptionHandling {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "User not Found", ex.getMessage(), request);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ApiResponse(
        responseCode = "423",
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.LOCKED, "Access denied", ex.getMessage(), request);
    }
    
    @ExceptionHandler(TaskNotFoundException.class)
    @ApiResponse(
        responseCode = "404",
        description = "Task not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handeTaskNotFound(TaskNotFoundException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.NOT_FOUND, "Task not found", ex.getMessage(), request);
    }
    
    @ExceptionHandler(CommentNotFoundException.class)
    @ApiResponse(
        responseCode = "404",
        description = "Comment not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.NOT_FOUND, "Comment not found", ex.getMessage(), request);
    }
    
    @ExceptionHandler(UserAlreadyExistException.class)
    @ApiResponse(
        responseCode = "226",
        description = "User already exists",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.IM_USED, "User already exists", ex.getMessage(), request);
    }
    
    @ExceptionHandler(ValidationException.class)
    @ApiResponse(
        responseCode = "409",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, "wrong data", ex.getMessage(), request);
    }
    
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequestsException(TooManyRequestsException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "too many requests", ex.getMessage(), request);
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