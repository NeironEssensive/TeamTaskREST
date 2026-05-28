package com.example.team_task.dto.comment;

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
@Schema(description = "Response DTO containing comment information")
public class CommentResponse {
    
    @Schema(description = "Unique identifier of the comment", example = "1")
    private Long id;
    
    @Schema(description = "Content of the comment", example = "Great progress on this task!")
    private String text;
    
    @Schema(description = "ID of the user who wrote this comment", example = "1")
    private Long userId;
    
    @Schema(description = "ID of the task this comment belongs to", example = "5")
    private Long taskId;
    
    @Schema(description = "Username of the comment author", example = "john_doe")
    private String userName;
    
    @Schema(description = "Timestamp when the comment was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}