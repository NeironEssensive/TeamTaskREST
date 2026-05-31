package com.example.team_task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team_task.dto.auth.LoginRequest;
import com.example.team_task.dto.auth.LoginResponse;
import com.example.team_task.dto.auth.LogoutRequest;
import com.example.team_task.dto.auth.RefreshRequest;
import com.example.team_task.dto.auth.RegisterRequest;
import com.example.team_task.dto.error.ErrorResponse;
import com.example.team_task.dto.error.TooManyRequestsException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.service.AuthService;
import com.example.team_task.service.JwtService;
import com.example.team_task.service.RateLimitService;
import com.example.team_task.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {
    private final AuthService authService;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private RateLimitService rateLimitService;

    public AuthController(AuthService authService, JwtService jwtService, PasswordEncoder passwordEncoder,
            UserService userService, RateLimitService rateLimitService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with USER role. Username must be unique and email must be valid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "226", description = "User already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = """
                    {
                        "timeStamp": "2024-01-15T10:30:00",
                        "status": 226,
                        "error": "User already exists",
                        "message": "User with username : john_doe already exists",
                        "path": "/auth/register"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> register(
            @Parameter(description = "Registration details", required = true) @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.saveUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user with username and password, returns JWT token on success")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class), examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiJ9..."
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid credentials", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = """
                    {
                        "timeStamp": "2024-01-15T10:30:00",
                        "status": 409,
                        "error": "wrong data",
                        "message": "Login or password are wrong",
                        "path": "/auth/login"
                    }
                    """)))
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials", required = true, examples = {
                    @ExampleObject(name = "Login request", value = """
                            {
                                "name": "john_doe",
                                "password": "securePass123"
                            }
                            """)
            }) @RequestBody LoginRequest request) {
         return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}