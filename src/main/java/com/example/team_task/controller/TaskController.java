package com.example.team_task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.ErrorResponse;
import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.service.TaskService;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Task management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {
    private TaskService taskService;
    private UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @PostMapping("/create")
    @Operation(
        summary = "Create a new task",
        description = "Creates a new task for the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(
                description = "Task data",
                required = true,
                examples = {
                    @ExampleObject(
                        name = "Create task",
                        value = """
                            {
                                "title": "Implement authentication",
                                "description": "Add JWT authentication to the API"
                            }
                            """
                    )
                }
            )
            @RequestBody Map<String, Object> taskData) {
        String title = (String) taskData.get("title");
        String description = (String) taskData.get("description");
        UserResponse current = userService.getCurrentUser();
        User user = userService.findById(current.getId());
        Task task = new Task(title, description, user);
        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @GetMapping("/myTasks")
    @Operation(
        summary = "Get current user's tasks",
        description = "Returns all tasks belonging to the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tasks retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse[].class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<TaskResponse>> myTasks() {
        return ResponseEntity.ok(taskService.myTasks());
    }

    @GetMapping("/admin/allTasks")
    @Operation(
        summary = "Get all tasks (Admin only)",
        description = "Returns all tasks in the system. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "All tasks retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse[].class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<TaskResponse>> allTasks() {
        UserResponse currentUser = userService.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN"))
            throw new AccessDeniedException();
        return ResponseEntity.ok(taskService.allTasks());
    }

    @GetMapping("/admin/task/{id}")
    @Operation(
        summary = "Get task by ID (Admin only)",
        description = "Returns any task by its ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TaskResponse> adminTaskById(
            @Parameter(description = "ID of the task to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        UserResponse currentUser = userService.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN"))
            throw new AccessDeniedException();
        return ResponseEntity.ok(taskService.findTaskById(id));
    }

    @GetMapping("/myTasks/{id}")
    @Operation(
        summary = "Get user's task by ID",
        description = "Returns a specific task belonging to the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied - Task doesn't belong to user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TaskResponse> taskById(
            @Parameter(description = "ID of the task to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        UserResponse user = userService.getCurrentUser();
        TaskResponse task = taskService.findTaskById(id);
        if (task.getUserId() != user.getId())
            throw new AccessDeniedException();
        return ResponseEntity.ok(taskService.findTaskById(id));
    }

    @PutMapping("/myTasks/{id}")
    @Operation(
        summary = "Update user's task",
        description = "Updates a specific task belonging to the authenticated user. You can update title, description, status, or priority."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Invalid status or priority value",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "ID of the task to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(
                description = "Fields to update (title, description, status, priority)",
                required = true,
                examples = {
                    @ExampleObject(
                        name = "Update task",
                        value = """
                            {
                                "title": "Updated title",
                                "status": "IN_PROGRESS",
                                "priority": "HIGH"
                            }
                            """
                    )
                }
            )
            @RequestBody Map<String, Object> taskData) {
        UserResponse user = userService.getCurrentUser();
        TaskResponse task = taskService.findTaskById(id);
        if (task.getUserId() != user.getId())
            throw new AccessDeniedException();
        TaskResponse updatedTask = taskService.updateTask(id, taskData);
        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("admin/task/{id}")
    @Operation(
        summary = "Update any task (Admin only)",
        description = "Updates any task in the system by its ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TaskResponse> updateAnyTask(
            @Parameter(description = "ID of the task to update", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, Object> taskData){
        UserResponse user = userService.getCurrentUser();
        if(!user.getRole().equals("ADMIN")) throw new AccessDeniedException();
        TaskResponse updatedTask = taskService.updateTask(id, taskData);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/myTasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete user's task",
        description = "Deletes a specific task belonging to the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Task deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public void deleteTask(
            @Parameter(description = "ID of the task to delete", required = true, example = "1")
            @PathVariable Long id){
        UserResponse user = userService.getCurrentUser();
        TaskResponse task = taskService.findTaskById(id);
        if (task.getUserId() != user.getId())
            throw new AccessDeniedException();
        taskService.deleteTask(id);
    }

    @DeleteMapping("/admin/task/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete any task (Admin only)",
        description = "Deletes any task in the system by its ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Task deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public void deleteAnyTask(
            @Parameter(description = "ID of the task to delete", required = true, example = "1")
            @PathVariable Long id){
        UserResponse user = userService.getCurrentUser();
        if(!user.getRole().equals("ADMIN")) throw new AccessDeniedException();
        taskService.deleteTask(id);
    }
}