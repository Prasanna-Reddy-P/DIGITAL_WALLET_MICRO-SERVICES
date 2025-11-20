package com.example.user_service_micro.controller.authentication;
import com.example.user_service_micro.dto.credentials.LoginRequest;
import com.example.user_service_micro.dto.credentials.SignupRequest;
import com.example.user_service_micro.service.user.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Handles user signup, admin signup, login, and token validation")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // --------------------------------------------------------------------
    // USER SIGNUP
    // --------------------------------------------------------------------
    @Operation(
            summary = "User Signup",
            description = "Registers a new user and triggers wallet creation in wallet-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Parameter(description = "User signup details")
            @Valid @RequestBody SignupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    // --------------------------------------------------------------------
    // ADMIN SIGNUP
    // --------------------------------------------------------------------
    @Operation(
            summary = "Admin Signup",
            description = "Registers a new administrator using a secret admin key."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Admin registered successfully"),
            @ApiResponse(responseCode = "403", description = "Invalid admin secret"),
            @ApiResponse(responseCode = "400", description = "Email already exists")
    })
    @PostMapping("/signup-admin")
    public ResponseEntity<?> signupAdmin(
            @Valid @RequestBody SignupRequest request,
            @Parameter(description = "Secret admin authorization key")
            @RequestHeader("X-ADMIN-SECRET") String secret
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signupAdmin(request, secret));
    }

    // --------------------------------------------------------------------
    // LOGIN
    // --------------------------------------------------------------------
    @Operation(
            summary = "User Login",
            description = "Authenticates a user and returns a JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "403", description = "User blacklisted"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "User login credentials")
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    // --------------------------------------------------------------------
    // TOKEN VALIDATION
    // --------------------------------------------------------------------
    @Operation(
            summary = "Validate JWT Token",
            description = "Checks if a token is valid and returns the associated email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token valid"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Authorization header in the format: Bearer <token>")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String header
    ) {
        return ResponseEntity.ok(
                Map.of("email", authService.validateToken(header))
        );
    }
}