package com.example.team_task.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public TaskController(TaskService taskService, UserService userService){
        this.taskService = taskService;
        this.userService =  userService;
    }

    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@RequestBody Map<String, Object> taskData){
        String title = (String) taskData.get("title");
        String description = (String) taskData.get("description");
        UserResponse current = userService.getCurrentUser();
        User user = userService.findById(current.getId());
        Task task = new Task(title, description, user);
        return ResponseEntity.ok(taskService.saveTask(task));     
    }
}
