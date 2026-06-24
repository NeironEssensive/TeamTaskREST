package com.example.team_task.service;

import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUser_AuthenticatedUser_ReturnsUserResponse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getCurrentUser();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsUserNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void getCurrentUsername_AuthenticatedUser_ReturnsUsername() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john_doe");

        String username = userService.getCurrentUsername();

        assertThat(username).isEqualTo("john_doe");
    }

    @Test
    void getAllUsers_MultipleUsers_ReturnsAllUserResponses() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setName("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(User.Role.ADMIN);

        when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("testuser");
        assertThat(responses.get(1).getName()).isEqualTo("admin");
        assertThat(responses.get(1).getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getAllUsers_EmptyDatabase_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).isEmpty();
    }

    @Test
    void findByName_ExistingUser_ReturnsUserResponse() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));

        UserResponse response = userService.findByName("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByName_NonExistentUser_ThrowsUserNotFoundException() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByName("ghost"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void findById_ExistingUser_ReturnsUserEntity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User user = userService.findById(1L);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("testuser");
    }

    @Test
    void findById_NonExistentUser_ThrowsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteUser_ExistingUser_DeletesById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void getCurrentUserEntity_AuthenticatedUser_ReturnsEntity() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User user = userService.getCurrentUserEntity();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void mapToResponse_ValidUser_ReturnsCorrectUserResponse() {
        UserResponse response = userService.mapToResponse(testUser);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    void mapToResponse_AdminUser_ReturnsAdminRole() {
        User admin = new User();
        admin.setId(2L);
        admin.setName("admin");
        admin.setEmail("admin@example.com");
        admin.setRole(User.Role.ADMIN);

        UserResponse response = userService.mapToResponse(admin);

        assertThat(response.getRole()).isEqualTo("ADMIN");
    }
}