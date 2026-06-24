package com.example.team_task.repository;

import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);

        otherUser = new User();
        otherUser.setName("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(User.Role.USER);

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(otherUser);
    }

    @Test
    void findByUserId_UserHasTasks_ReturnsUserTasks() {
        Task task1 = new Task("Task 1", "Description 1", testUser);
        Task task2 = new Task("Task 2", "Description 2", testUser);

        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void findByUserId_UserHasNoTasks_ReturnsEmptyList() {
        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertThat(tasks).isEmpty();
    }

    @Test
    void findByUserId_OnlyReturnsTasksForSpecificUser() {
        Task userTask = new Task("User Task", "User Description", testUser);
        Task otherTask = new Task("Other Task", "Other Description", otherUser);

        entityManager.persistAndFlush(userTask);
        entityManager.persistAndFlush(otherTask);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("User Task");
        assertThat(tasks.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByUserId_TaskHasCorrectStatus() {
        Task task = new Task("Status Task", "Description", testUser);
        entityManager.persistAndFlush(task);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getStatus()).isEqualTo(Task.Status.PENDING);
        assertThat(tasks.get(0).getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }

    @Test
    void save_NewTask_SetsCreatedAtAndUpdatedAt() {
        Task task = new Task("New Task", "New Description", testUser);
        Task savedTask = taskRepository.save(task);

        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();
    }
}