package com.example.team_task.controller;

import com.example.team_task.dto.comment.CommentResponse;
import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.CommentNotFoundException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Comment;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.service.CommentService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserService userService;

    @MockBean
    private TaskService taskService;

    private UserResponse userResponse;
    private UserResponse adminResponse;
    private User testUser;
    private Task testTask;
    private CommentResponse commentResponse;

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

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");

        testTask = new Task();
        testTask.setId(10L);
        testTask.setTitle("Test Task");

        commentResponse = CommentResponse.builder()
                .id(1L)
                .text("Great work")
                .userId(1L)
                .taskId(10L)
                .userName("testuser")
                .build();
    }

    @Test
    void createComment_ValidData_ReturnsOk() throws Exception {
        Map<String, Object> commentData = Map.of("text", "Great work");

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(userService.findById(1L)).thenReturn(testUser);
        when(taskService.entityFindTaskById(10L)).thenReturn(testTask);
        when(commentService.saveComment(any(Comment.class))).thenReturn(commentResponse);

        mockMvc.perform(post("/comments/create/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great work"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.taskId").value(10))
                .andExpect(jsonPath("$.userName").value("testuser"));
    }

    @Test
    void createComment_TaskNotFound_ReturnsNotFound() throws Exception {
        Map<String, Object> commentData = Map.of("text", "Great work");

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(userService.findById(1L)).thenReturn(testUser);
        when(taskService.entityFindTaskById(99L))
                .thenThrow(new CommentNotFoundException(99L));

        mockMvc.perform(post("/comments/create/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_OwnerAccess_ReturnsNoContent() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(commentService.findById(1L)).thenReturn(commentResponse);

        mockMvc.perform(delete("/comments/delete/1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L);
    }

    @Test
    void deleteTask_NotOwner_ReturnsLocked() throws Exception {
        CommentResponse otherComment = CommentResponse.builder()
                .id(5L)
                .userId(99L)
                .build();

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(commentService.findById(5L)).thenReturn(otherComment);

        mockMvc.perform(delete("/comments/delete/5"))
                .andExpect(status().isLocked());

        verify(commentService, never()).deleteComment(anyLong());
    }

    @Test
    void deleteTask_CommentNotFound_ReturnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(commentService.findById(99L)).thenThrow(new CommentNotFoundException(99L));

        mockMvc.perform(delete("/comments/delete/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAnyTask_AdminAccess_ReturnsNoContent() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminResponse);

        mockMvc.perform(delete("/comments/admin/delete/1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L);
    }

    @Test
    void deleteAnyTask_RegularUser_ReturnsLocked() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(delete("/comments/admin/delete/1"))
                .andExpect(status().isLocked());
    }
}