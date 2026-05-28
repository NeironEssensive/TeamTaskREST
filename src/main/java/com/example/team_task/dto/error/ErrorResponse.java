package com.example.team_task.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard error response for API errors")
public class ErrorResponse {
    
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T10:30:00")
    private LocalDateTime timeStamp;
    
    @Schema(description = "HTTP status code", example = "404")
    private int status;
    
    @Schema(description = "Error type", example = "User not Found")
    private String error;
    
    @Schema(description = "Detailed error message", example = "User with id 99 not found")
    private String message;
    
    @Schema(description = "Request path that caused the error", example = "/api/users/99")
    private String path;
}