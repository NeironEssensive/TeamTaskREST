package com.example.team_task.service;

import com.example.team_task.dto.comment.CommentResponse;
import com.example.team_task.dto.error.CommentNotFoundException;
import com.example.team_task.entity.Comment;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Task testTask;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setText("Test comment text");
        testComment.setTask(testTask);
        testComment.setUser(testUser);
    }

    @Test
    void saveComment_ValidComment_SavesAndReturnsResponse() {
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentResponse response = commentService.saveComment(testComment);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getText()).isEqualTo("Test comment text");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getTaskId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("testuser");
        verify(commentRepository).save(testComment);
    }

    @Test
    void saveComment_WithDifferentUser_ReturnsCorrectMapping() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setName("anotheruser");

        Comment comment = new Comment();
        comment.setId(2L);
        comment.setText("Another comment");
        comment.setTask(testTask);
        comment.setUser(anotherUser);

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.saveComment(comment);

        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getUserName()).isEqualTo("anotheruser");
    }

    @Test
    void findById_ExistingComment_ReturnsCommentResponse() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        CommentResponse response = commentService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getText()).isEqualTo("Test comment text");
    }

    @Test
    void findById_NonExistentComment_ThrowsCommentNotFoundException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findById(99L))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteComment_ExistingComment_DeletesById() {
        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_AnyId_CallsRepositoryDelete() {
        commentService.deleteComment(42L);

        verify(commentRepository).deleteById(42L);
    }

    @Test
    void mapToResponse_ValidComment_ReturnsCorrectResponse() {
        CommentResponse response = commentService.mapToResponse(testComment);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getText()).isEqualTo("Test comment text");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getTaskId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("testuser");
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    void mapToResponse_CommentWithDifferentTask_MapsCorrectly() {
        Task anotherTask = new Task();
        anotherTask.setId(5L);
        testComment.setTask(anotherTask);

        CommentResponse response = commentService.mapToResponse(testComment);

        assertThat(response.getTaskId()).isEqualTo(5L);
    }
}