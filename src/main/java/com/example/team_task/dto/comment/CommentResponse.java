package com.example.team_task.dto.comment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private Long userId;
    private Long taskId;
    private String userName;
    private LocalDateTime createdAt;
}

