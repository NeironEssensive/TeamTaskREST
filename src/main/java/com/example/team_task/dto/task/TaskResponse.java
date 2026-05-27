package com.example.team_task.dto.task;

import java.time.LocalDateTime;

import com.example.team_task.entity.Task.Priority;
import com.example.team_task.entity.Task.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
