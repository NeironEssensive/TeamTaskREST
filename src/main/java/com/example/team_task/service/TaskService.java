package com.example.team_task.service;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.repository.TaskRepository;

@Service
public class TaskService {
    private TaskRepository taskRepository;
    private UserService userService;
    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Transactional
    public TaskResponse saveTask(@NonNull Task task) {
        return mapToResponse(taskRepository.save(task));
    }

    public List<TaskResponse> allTasks() {
        return taskRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public List<TaskResponse> myTasks(){
        UserResponse current = userService.getCurrentUser();
        return taskRepository.findByUserId(current.getId())
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

   

    public TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().toString())
                .priority(task.getPriority().toString())
                .userId(task.getUser().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

}
