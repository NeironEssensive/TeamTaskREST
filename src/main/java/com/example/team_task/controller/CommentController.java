package com.example.team_task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.comment.CommentResponse;
import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.ErrorResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Comment;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.service.CommentService;
import com.example.team_task.service.TaskService;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/comments")
@Tag(name = "Comments", description = "Comment management operations")
@SecurityRequirement(name = "Bearer Authentication")
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
    @Operation(
        summary = "Create a comment on a task",
        description = "Creates a new comment on the specified task by the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comment created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "ID of the task to comment on", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(
                description = "Comment data",
                required = true,
                examples = {
                    @ExampleObject(
                        name = "Create comment",
                        value = """
                            {
                                "text": "Great progress on this task!"
                            }
                            """
                    )
                }
            )
            @RequestBody Map<String, Object> commentData){
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
    @Operation(
        summary = "Delete own comment",
        description = "Deletes a comment created by the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Comment deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Comment not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied - Comment doesn't belong to user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public void deleteTask(
            @Parameter(description = "ID of the comment to delete", required = true, example = "1")
            @PathVariable Long id){
        UserResponse user = userService.getCurrentUser();
        CommentResponse comment = commentService.findById(id);
        if(user.getId() != comment.getUserId()) 
            throw new AccessDeniedException();
        commentService.deleteComment(id);
    }

    @DeleteMapping("/admin/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete any comment (Admin only)",
        description = "Deletes any comment in the system by its ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Comment deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Comment not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public void deleteAnyTask(
            @Parameter(description = "ID of the comment to delete", required = true, example = "1")
            @PathVariable Long id){
        UserResponse user = userService.getCurrentUser();
        if(!user.getRole().equals("ADMIN")) 
            throw new AccessDeniedException();
        commentService.deleteComment(id);
    }
}