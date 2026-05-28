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
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.error.ErrorResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.service.UserService;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the profile information of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Current user profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<UserResponse> aboutMe() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/admin/all")
    @Operation(
        summary = "Get all users (Admin only)",
        description = "Returns a list of all registered users. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of all users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse[].class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied - User is not an admin",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "timeStamp": "2024-01-15T10:30:00",
                        "status": 423,
                        "error": "Access denied",
                        "message": "Access denied",
                        "path": "/users/admin/all"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<List<UserResponse>> allUsers() {
        UserResponse currentUser = userService.getCurrentUser();
        if (currentUser.getRole().equals("USER"))
            throw new com.example.team_task.dto.error.AccessDeniedException("Access denied");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete a user (Admin only)",
        description = "Deletes a user by their ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Access denied - User is not an admin",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public void deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "5")
            @PathVariable Long id) {
        UserResponse current = userService.getCurrentUser();
        if (!current.getRole().equals("ADMIN"))
            throw new com.example.team_task.dto.error.AccessDeniedException();
        userService.deleteUser(id);
    }
}