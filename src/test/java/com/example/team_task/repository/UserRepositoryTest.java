package com.example.team_task.repository;

import com.example.team_task.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
    }

    @Test
    void findByName_ExistingUser_ReturnsUser() {
        entityManager.persistAndFlush(testUser);

        Optional<User> result = userRepository.findByName("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void findByName_NonExistentUser_ReturnsEmpty() {
        Optional<User> result = userRepository.findByName("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void findByName_DifferentCase_ReturnsEmpty() {
        entityManager.persistAndFlush(testUser);

        Optional<User> result = userRepository.findByName("TESTUSER");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByName_ExistingUser_ReturnsTrue() {
        entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByName("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_NonExistentUser_ReturnsFalse() {
        boolean exists = userRepository.existsByName("ghostuser");

        assertThat(exists).isFalse();
    }

    @Test
    void save_NewUser_PersistsWithGeneratedId() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isPositive();
    }

    @Test
    void save_NewUser_SetsCreatedAt() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void save_NewUser_SetsUpdatedAt() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_ExistingUser_ReturnsUser() {
        User persistedUser = entityManager.persistAndFlush(testUser);

        Optional<User> result = userRepository.findById(persistedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("testuser");
    }

    @Test
    void findById_NonExistentUser_ReturnsEmpty() {
        Optional<User> result = userRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_ExistingUser_RemovesFromDatabase() {
        User persistedUser = entityManager.persistAndFlush(testUser);
        Long userId = persistedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }
}