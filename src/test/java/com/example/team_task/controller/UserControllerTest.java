package com.example.team_task.controller;

import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse userResponse;
    private UserResponse adminResponse;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void aboutMe_AuthenticatedUser_ReturnsCurrentUser() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void aboutMe_AdminUser_ReturnsAdminData() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void allUsers_AdminUser_ReturnsAllUsers() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(userService.getAllUsers()).thenReturn(List.of(userResponse, adminResponse));

        mockMvc.perform(get("/users/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("testuser"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("admin"));
    }

    @Test
    void allUsers_AdminUser_EmptyList_ReturnsEmptyArray() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void allUsers_RegularUser_ReturnsLocked() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/users/admin/all"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void deleteUser_AdminUser_ReturnsNoContent() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);

        mockMvc.perform(delete("/users/admin/5"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(5L);
    }

    @Test
    void deleteUser_RegularUser_ReturnsLocked() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(delete("/users/admin/5"))
                .andExpect(status().isLocked());
    }

    @Test
    void deleteUser_UserNotFound_ReturnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);
        when(userService.findByName("admin")).thenReturn(adminResponse);
        when(userService.findById(2L)).thenThrow(new UserNotFoundException(2L));

        mockMvc.perform(delete("/users/admin/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not Found"));
    }
}