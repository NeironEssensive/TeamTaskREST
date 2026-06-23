package com.example.team_task.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team_task.dto.error.TaskNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.Task.Priority;
import com.example.team_task.entity.Task.Status;
import com.example.team_task.entity.User;
import com.example.team_task.repository.TaskRepository;
import com.example.team_task.repository.UserRepository;

@Service
public class TaskService {
    private TaskRepository taskRepository;
    private UserService userService;
    private EventPublisherService eventPublisherService;

    public TaskService(TaskRepository taskRepository, UserService userService, EventPublisherService eventPublisherService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.eventPublisherService = eventPublisherService;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse saveTask(@NonNull Task task) {
        Task saved = taskRepository.save(task);
        UserResponse currentUser = userService.getCurrentUser();
        eventPublisherService.publishTaskCreated(saved, userService.findById(currentUser.getId()));
        return mapToResponse(saved);
    }

    @Cacheable(value = "tasks", key = "'allTasks'")
    public List<TaskResponse> allTasks() {
        return taskRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Cacheable(value = "tasks", key = "'myTasks:' + @userService.getCurrentUsername()")
    public List<TaskResponse> myTasks() {
        UserResponse current = userService.getCurrentUser();
        return taskRepository.findByUserId(current.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Cacheable(value = "task", key = "#id")
    public TaskResponse findTaskById(Long id) {
        return mapToResponse(taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id)));
    }

    public Task entityFindTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true) 
    }, put = {
            @CachePut(value = "task", key = "#id") 
    })
    public TaskResponse updateTask(Long id, Map<String, Object> taskData) {
         Task task = entityFindTaskById(id);
    
    String oldTitle = task.getTitle();
    String oldDescription = task.getDescription();
    Status oldStatus = task.getStatus();
    Priority oldPriority = task.getPriority();  
    updateFields(task, taskData);
    Task saved = taskRepository.save(task);
    UserResponse currentUser = userService.getCurrentUser();
    eventPublisherService.publishTaskUpdated(task, saved, userService.findById(currentUser.getId()));
    TaskResponse response = mapToResponse(saved);
    response.setUpdatedAt(LocalDateTime.now());
    return response;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "task", key = "#id"),
            @CacheEvict(value = "tasks", allEntries = true)
    })
    public void deleteTask(Long id) {
        Task task = entityFindTaskById(id);
        UserResponse currentUser = userService.getCurrentUser();
        taskRepository.deleteById(id);
        eventPublisherService.publishTaskDeleted(task, userService.findById(currentUser.getId()));
    }

    private void updateFields(Task task, Map<String, Object> taskData) {
        Optional.ofNullable((String) taskData.get("title"))
                .ifPresent(task::setTitle);

        Optional.ofNullable((String) taskData.get("description"))
                .ifPresent(task::setDescription);

        Optional.ofNullable((String) taskData.get("status"))
                .ifPresent(status -> {
                    try {
                        task.setStatus(Status.valueOf(status.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException(
                                "Wrong status: '" + status +
                                        "'. Allowed status: " +
                                        Arrays.toString(Status.values()));
                    }
                });

        Optional.ofNullable((String) taskData.get("priority"))
                .ifPresent(priority -> {
                    try {
                        task.setPriority(Priority.valueOf(priority.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException(
                                "Wrond priority: '" + priority +
                                        "'. Allowed priority: " +
                                        Arrays.toString(Priority.values()));
                    }
                });
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