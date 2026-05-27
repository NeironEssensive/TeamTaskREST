package com.example.team_task.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.TaskNotFoundException;
import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.service.TaskService;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private TaskService taskService;
    private UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@RequestBody Map<String, Object> taskData) {
        String title = (String) taskData.get("title");
        String description = (String) taskData.get("description");
        UserResponse current = userService.getCurrentUser();
        User user = userService.findById(current.getId());
        Task task = new Task(title, description, user);
        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @GetMapping("/myTasks")
    public ResponseEntity<List<TaskResponse>> myTasks() {
        return ResponseEntity.ok(taskService.myTasks());
    }

    @GetMapping("/admin/allTasks")
    public ResponseEntity<List<TaskResponse>> allTasks() {
        UserResponse currentUser = userService.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN"))
            throw new AccessDeniedException();
        return ResponseEntity.ok(taskService.allTasks());
    }

    @GetMapping("/admin/task/{id}")
    public ResponseEntity<TaskResponse> adminTaskById(@PathVariable Long id) {
        UserResponse currentUser = userService.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN"))
            throw new AccessDeniedException();
        return ResponseEntity.ok(taskService.findTaskById(id));
    }

    @GetMapping("/myTasks/{id}")
    public ResponseEntity<TaskResponse> taskById(@PathVariable Long id) {
        UserResponse user = userService.getCurrentUser();
        TaskResponse task = taskService.findTaskById(id);
        if (task.getUserId() != user.getId())
            throw new AccessDeniedException();
        return ResponseEntity.ok(taskService.findTaskById(id));
    }

    @PutMapping("/myTasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody Map<String, Object> taskData) {
        UserResponse user = userService.getCurrentUser();
        TaskResponse task = taskService.findTaskById(id);
        if (task.getUserId() != user.getId())
            throw new AccessDeniedException();
        TaskResponse updatedTask = taskService.updateTask(id, taskData);
        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("admin/task/{id}")
    public ResponseEntity<TaskResponse> updateAnyTask(@PathVariable Long id, @RequestBody Map<String, Object> taskData){
        UserResponse user = userService.getCurrentUser();
        if(!user.getRole().equals("ADMIN")) throw new AccessDeniedException();
        TaskResponse updatedTask = taskService.updateTask(id, taskData);
        return ResponseEntity.ok(updatedTask);
    }
}
