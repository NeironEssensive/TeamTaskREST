package com.example.team_task.controller;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.TaskNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.service.TaskService;
import com.example.team_task.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    private TaskResponse taskResponse;
    private UserResponse userResponse;
    private UserResponse adminResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status("PENDING")
                .priority("MEDIUM")
                .userId(1L)
                .createdAt(LocalDateTime.of(2026, 6, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 15, 10, 0))
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        adminResponse = UserResponse.builder()
                .id(2L)
                .name("admin")
                .email("admin@example.com")
                .role("ADMIN")
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
    }

    @Test
    void createTask_ValidData_ReturnsOk() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(userService.findById(1L)).thenReturn(testUser);
        when(taskService.saveTask(any(Task.class))).thenReturn(taskResponse);

        Map<String, Object> taskData = Map.of(
                "title", "Test Task",
                "description", "Test Description"
        );

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void createTask_TaskNotFound_ReturnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(userService.findById(1L)).thenReturn(testUser);
        when(taskService.saveTask(any(Task.class)))
                .thenThrow(new TaskNotFoundException(99L));

        Map<String, Object> taskData = Map.of("title", "Test Task");

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void myTasks_UserHasTasks_ReturnsTaskList() throws Exception {
        when(taskService.myTasks()).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/tasks/myTasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void myTasks_UserHasNoTasks_ReturnsEmptyList() throws Exception {
        when(taskService.myTasks()).thenReturn(List.of());

        mockMvc.perform(get("/tasks/myTasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void allTasks_AdminUser_ReturnsAllTasks() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(taskService.allTasks()).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/tasks/admin/allTasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void allTasks_RegularUser_ReturnsLocked() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/tasks/admin/allTasks"))
                .andExpect(status().isLocked());
    }

    @Test
    void adminTaskById_AdminUser_ReturnsTask() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(taskService.findTaskById(1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/tasks/admin/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void adminTaskById_RegularUser_ReturnsLocked() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/tasks/admin/task/1"))
                .andExpect(status().isLocked());
    }

    @Test
    void adminTaskById_TaskNotFound_ReturnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(taskService.findTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/tasks/admin/task/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void taskById_OwnerAccess_ReturnsTask() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/tasks/myTasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void taskById_NotOwner_ReturnsLocked() throws Exception {
        TaskResponse otherTask = TaskResponse.builder()
                .id(5L)
                .title("Other Task")
                .userId(99L)
                .build();

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(5L)).thenReturn(otherTask);

        mockMvc.perform(get("/tasks/myTasks/5"))
                .andExpect(status().isLocked());
    }

    @Test
    void taskById_TaskNotFound_ReturnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/tasks/myTasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_OwnerAccess_ReturnsUpdatedTask() throws Exception {
        Map<String, Object> updates = Map.of("title", "Updated Title");

        TaskResponse updatedTask = TaskResponse.builder()
                .id(1L)
                .title("Updated Title")
                .description("Test Description")
                .status("PENDING")
                .priority("MEDIUM")
                .userId(1L)
                .build();

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(1L)).thenReturn(taskResponse);
        when(taskService.updateTask(eq(1L), any(Map.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/tasks/myTasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateTask_NotOwner_ReturnsLocked() throws Exception {
        TaskResponse otherTask = TaskResponse.builder()
                .id(5L)
                .userId(99L)
                .build();

        Map<String, Object> updates = Map.of("title", "Updated");

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(5L)).thenReturn(otherTask);

        mockMvc.perform(put("/tasks/myTasks/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isLocked());
    }

    @Test
    void updateTask_InvalidStatus_ReturnsConflict() throws Exception {
        Map<String, Object> updates = Map.of("status", "INVALID");

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(1L)).thenReturn(taskResponse);
        when(taskService.updateTask(eq(1L), any(Map.class)))
                .thenThrow(new ValidationException("Wrong status: INVALID"));

        mockMvc.perform(put("/tasks/myTasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateAnyTask_AdminAccess_ReturnsUpdatedTask() throws Exception {
        Map<String, Object> updates = Map.of("priority", "HIGH");

        TaskResponse updatedTask = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .priority("HIGH")
                .userId(1L)
                .build();

        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(taskService.updateTask(eq(1L), any(Map.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/tasks/admin/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void updateAnyTask_RegularUser_ReturnsLocked() throws Exception {
        Map<String, Object> updates = Map.of("priority", "HIGH");

        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(put("/tasks/admin/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isLocked());
    }

    @Test
    void deleteTask_OwnerAccess_ReturnsNoContent() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(1L)).thenReturn(taskResponse);

        mockMvc.perform(delete("/tasks/myTasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteTask_NotOwner_ReturnsLocked() throws Exception {
        TaskResponse otherTask = TaskResponse.builder()
                .id(5L)
                .userId(99L)
                .build();

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(5L)).thenReturn(otherTask);

        mockMvc.perform(delete("/tasks/myTasks/5"))
                .andExpect(status().isLocked());
    }

    @Test
    void deleteTask_TaskNotFound_ReturnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(taskService.findTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(delete("/tasks/myTasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAnyTask_AdminAccess_ReturnsNoContent() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);

        mockMvc.perform(delete("/tasks/admin/task/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteAnyTask_RegularUser_ReturnsLocked() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(delete("/tasks/admin/task/1"))
                .andExpect(status().isLocked());
    }
}