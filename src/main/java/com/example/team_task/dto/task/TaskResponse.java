package com.example.team_task.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response DTO containing task information")
public class TaskResponse {
    
    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;
    
    @Schema(description = "Title of the task", example = "Implement user authentication")
    private String title;
    
    @Schema(description = "Detailed description of the task", example = "Implement JWT-based authentication")
    private String description;
    
    @Schema(description = "Current status of the task", example = "IN_PROGRESS", allowableValues = {"PENDING", "IN_PROGRESS", "DONE"})
    private String status;
    
    @Schema(description = "Priority level of the task", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String priority;
    
    @Schema(description = "ID of the user who owns this task", example = "1")
    private Long userId;
    
    @Schema(description = "Timestamp when the task was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the task was last updated", example = "2024-01-15T14:45:00")
    private LocalDateTime updatedAt;
}