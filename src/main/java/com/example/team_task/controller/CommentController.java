package com.example.team_task.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.team_task.dto.comment.CommentResponse;
import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Comment;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.service.CommentService;
import com.example.team_task.service.TaskService;
import com.example.team_task.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
@RestController
@RequestMapping("/comments")
public class CommentController {
    private CommentService commentService;
    private UserService userService;
    private TaskService taskService;
    public CommentController(CommentService commentService, UserService userService, TaskService taskService){
        this.commentService = commentService;
        this.userService = userService;
        this.taskService = taskService;
    }
    @PostMapping("/create/{id}")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long id, @RequestBody Map<String, Object> commentData){
        String text = (String) commentData.get("text");
        UserResponse user = userService.getCurrentUser();
        User trueUser = userService.findById(user.getId());
        Task task = taskService.entityFindTaskById(id);
        Comment comment = new Comment(text, task, trueUser);
        commentService.saveComment(comment);
        return ResponseEntity.ok(commentService.mapToResponse(comment));
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id){
        UserResponse user = userService.getCurrentUser();
        CommentResponse comment = commentService.findById(id);
        if(user.getId() != comment.getUserId()) throw new AccessDeniedException();
        commentService.deleteComment(id);
    }

    @DeleteMapping("/admin/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnyTask(@PathVariable Long id){
        UserResponse user = userService.getCurrentUser();
        if(!user.getRole().equals("ADMIN")) throw new AccessDeniedException();
        commentService.deleteComment(id);
    }
}
