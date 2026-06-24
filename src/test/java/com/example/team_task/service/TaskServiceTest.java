package com.example.team_task.service;

import com.example.team_task.dto.error.TaskNotFoundException;
import com.example.team_task.dto.error.ValidationException;
import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private User adminUser;
    private Task testTask;
    private UserResponse currentUserResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setName("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(User.Role.ADMIN);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(Task.Status.PENDING);
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setUser(testUser);

        currentUserResponse = UserResponse.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
    }

    @Test
    void saveTask_ValidTask_SavesAndPublishesEvent() {
        Task taskToSave = new Task("New Task", "Description", testUser);
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(userService.findById(1L)).thenReturn(testUser);
        when(taskRepository.save(taskToSave)).thenReturn(testTask);

        TaskResponse response = taskService.saveTask(taskToSave);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).save(taskToSave);
        verify(eventPublisherService).publishTaskCreated(any(Task.class), eq(testUser));
    }

    @Test
    void allTasks_MultipleTasks_ReturnsAllTaskResponses() {
        Task secondTask = new Task();
        secondTask.setId(2L);
        secondTask.setTitle("Second Task");
        secondTask.setDescription("Second Description");
        secondTask.setStatus(Task.Status.IN_PROGRESS);
        secondTask.setPriority(Task.Priority.HIGH);
        secondTask.setUser(adminUser);

        when(taskRepository.findAll()).thenReturn(List.of(testTask, secondTask));

        List<TaskResponse> responses = taskService.allTasks();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Task");
        assertThat(responses.get(1).getTitle()).isEqualTo("Second Task");
        assertThat(responses.get(1).getPriority()).isEqualTo("HIGH");
    }

    @Test
    void allTasks_EmptyDatabase_ReturnsEmptyList() {
        when(taskRepository.findAll()).thenReturn(List.of());

        List<TaskResponse> responses = taskService.allTasks();

        assertThat(responses).isEmpty();
    }

    @Test
    void myTasks_UserHasTasks_ReturnsUserTasks() {
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(taskRepository.findByUserId(1L)).thenReturn(List.of(testTask));

        List<TaskResponse> responses = taskService.myTasks();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(1L);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Task");
    }

    @Test
    void myTasks_UserHasNoTasks_ReturnsEmptyList() {
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(taskRepository.findByUserId(1L)).thenReturn(List.of());

        List<TaskResponse> responses = taskService.myTasks();

        assertThat(responses).isEmpty();
    }

    @Test
    void findTaskById_ExistingTask_ReturnsTaskResponse() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskResponse response = taskService.findTaskById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPriority()).isEqualTo("MEDIUM");
    }

    @Test
    void findTaskById_NonExistentTask_ThrowsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void entityFindTaskById_ExistingTask_ReturnsTaskEntity() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task task = taskService.entityFindTaskById(1L);

        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(1L);
    }

    @Test
    void entityFindTaskById_NonExistentTask_ThrowsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.entityFindTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateTask_ValidFields_UpdatesAndPublishesEvent() {
        Map<String, Object> updates = Map.of(
                "title", "Updated Title",
                "status", "IN_PROGRESS",
                "priority", "HIGH"
        );

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("Updated Title");
        savedTask.setDescription("Test Description");
        savedTask.setStatus(Task.Status.IN_PROGRESS);
        savedTask.setPriority(Task.Priority.HIGH);
        savedTask.setUser(testUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(userService.findById(1L)).thenReturn(testUser);

        TaskResponse response = taskService.updateTask(1L, updates);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getPriority()).isEqualTo("HIGH");
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisherService).publishTaskUpdated(any(Task.class), any(Task.class), eq(testUser));
    }

    @Test
    void updateTask_InvalidStatus_ThrowsValidationException() {
        Map<String, Object> updates = Map.of("status", "INVALID_STATUS");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.updateTask(1L, updates))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Wrong status");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_InvalidPriority_ThrowsValidationException() {
        Map<String, Object> updates = Map.of("priority", "CRITICAL");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.updateTask(1L, updates))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Wrond priority");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_NoChanges_DoesNotPublishEventWhenEmpty() {
        Map<String, Object> updates = Map.of();

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("Test Task");
        savedTask.setDescription("Test Description");
        savedTask.setStatus(Task.Status.PENDING);
        savedTask.setPriority(Task.Priority.MEDIUM);
        savedTask.setUser(testUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(userService.findById(1L)).thenReturn(testUser);

        taskService.updateTask(1L, updates);

        verify(eventPublisherService).publishTaskUpdated(any(Task.class), any(Task.class), eq(testUser));
    }

    @Test
    void updateTask_OnlyTitleChanged_UpdatesTitle() {
        Map<String, Object> updates = Map.of("title", "New Title Only");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("New Title Only");
        savedTask.setDescription("Test Description");
        savedTask.setStatus(Task.Status.PENDING);
        savedTask.setPriority(Task.Priority.MEDIUM);
        savedTask.setUser(testUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(userService.findById(1L)).thenReturn(testUser);

        TaskResponse response = taskService.updateTask(1L, updates);

        assertThat(response.getTitle()).isEqualTo("New Title Only");
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void updateTask_OnlyDescriptionChanged_UpdatesDescription() {
        Map<String, Object> updates = Map.of("description", "New Description");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("Test Task");
        savedTask.setDescription("New Description");
        savedTask.setStatus(Task.Status.PENDING);
        savedTask.setPriority(Task.Priority.MEDIUM);
        savedTask.setUser(testUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(userService.findById(1L)).thenReturn(testUser);

        TaskResponse response = taskService.updateTask(1L, updates);

        assertThat(response.getDescription()).isEqualTo("New Description");
    }

    @Test
    void deleteTask_ExistingTask_DeletesAndPublishesEvent() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(userService.findById(1L)).thenReturn(testUser);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
        verify(eventPublisherService).publishTaskDeleted(testTask, testUser);
    }

    @Test
    void deleteTask_NonExistentTask_ThrowsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).deleteById(99L);
        verify(eventPublisherService, never()).publishTaskDeleted(any(), any());
    }

    @Test
    void mapToResponse_ValidTask_ReturnsCorrectTaskResponse() {
        TaskResponse response = taskService.mapToResponse(testTask);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPriority()).isEqualTo("MEDIUM");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void mapToResponse_TaskWithDifferentStatuses_MapsCorrectly() {
        testTask.setStatus(Task.Status.DONE);
        testTask.setPriority(Task.Priority.LOW);

        TaskResponse response = taskService.mapToResponse(testTask);

        assertThat(response.getStatus()).isEqualTo("DONE");
        assertThat(response.getPriority()).isEqualTo("LOW");
    }
}