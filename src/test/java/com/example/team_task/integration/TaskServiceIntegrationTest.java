package com.example.team_task.integration;

import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.TaskRepository;
import com.example.team_task.repository.UserRepository;
import com.example.team_task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);
        testUser = userRepository.save(testUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUser.getName(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void saveTask_ValidTask_PersistsToDatabase() {
        Task task = new Task("Integration Task", "Integration Description", testUser);

        TaskResponse response = taskService.saveTask(task);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Integration Task");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPriority()).isEqualTo("MEDIUM");
    }

    @Test
    void myTasks_UserHasTasks_ReturnsUserTasks() {
        Task task1 = createAndSaveTask("Task 1");
        Task task2 = createAndSaveTask("Task 2");

        List<TaskResponse> tasks = taskService.myTasks();

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(TaskResponse::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void updateTask_ValidUpdate_UpdatesInDatabase() {
        Task task = createAndSaveTask("Original Title");
        Map<String, Object> updates = Map.of("title", "Updated Title", "priority", "HIGH");

        TaskResponse updated = taskService.updateTask(task.getId(), updates);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getPriority()).isEqualTo("HIGH");
    }

    @Test
    void deleteTask_ExistingTask_RemovesFromDatabase() {
        Task task = createAndSaveTask("Task to delete");
        Long taskId = task.getId();

        taskService.deleteTask(taskId);

        assertThat(taskRepository.findById(taskId)).isEmpty();
    }

    @Test
    void allTasks_MultipleUsers_ReturnsAllTasks() {
        createAndSaveTask("User Task 1");
        createAndSaveTask("User Task 2");

        List<TaskResponse> tasks = taskService.allTasks();

        assertThat(tasks).hasSize(2);
    }

    private Task createAndSaveTask(String title) {
        Task task = new Task(title, "Description for " + title, testUser);
        return taskRepository.save(task);
    }
}