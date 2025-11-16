package com.example.user_service_micro.controller.authentication;

import com.example.user_service_micro.config.jwt.JwtUtil;
import com.example.user_service_micro.dto.credentials.LoginRequest;
import com.example.user_service_micro.dto.credentials.LoginResponse;
import com.example.user_service_micro.dto.credentials.SignupRequest;
import com.example.user_service_micro.dto.credentials.SignupResponse;
import com.example.user_service_micro.exception.auth.InvalidCredentialsException;
import com.example.user_service_micro.exception.user.UserAlreadyExistsException;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Handles user signup, admin signup, login, and token validation")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RestClient restClient;

    @Autowired
    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.restClient = RestClient.create();
    }

    // -----------------------------------------------------
    // USER SIGNUP
    // -----------------------------------------------------
    @Operation(
            summary = "User Signup",
            description = "Registers a new user and creates a wallet in wallet-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Parameter(description = "User signup details")
            @Valid @RequestBody SignupRequest signupRequest
    ) {
        if (signupRequest.getAge() < 18) {
            return ResponseEntity.badRequest().body("User must be at least 18 years old");
        }

        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists!");
        }

        User newUser = new User();
        newUser.setName(signupRequest.getName());
        newUser.setEmail(signupRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        newUser.setAge(signupRequest.getAge());
        newUser.setRole("USER");

        User savedUser = userRepository.save(newUser);

        // Notify wallet service (non-blocking)
        try {
            restClient.post()
                    .uri("http://localhost:8086/api/wallet/create?userId=" + savedUser.getId())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("⚠️ Wallet-service not reachable. Wallet will be created later.");
        }

        String token = jwtUtil.generateToken(savedUser.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("token", token);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------
    // ADMIN SIGNUP
    // -----------------------------------------------------
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
            @Valid @RequestBody SignupRequest signupRequest,
            @Parameter(description = "Secret admin authorization key")
            @RequestHeader("X-ADMIN-SECRET") String adminSecret
    ) {
        final String SECRET_KEY = "SuperSecretAdminKey123";

        if (!SECRET_KEY.equals(adminSecret)) {
            return ResponseEntity.status(403).body("Forbidden: Invalid admin secret");
        }

        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists!");
        }

        User admin = new User();
        admin.setName(signupRequest.getName());
        admin.setEmail(signupRequest.getEmail());
        admin.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        admin.setRole("ADMIN");

        User savedAdmin = userRepository.save(admin);
        String token = jwtUtil.generateToken(savedAdmin.getEmail());

        SignupResponse response = new SignupResponse(
                "Admin registered successfully!",
                savedAdmin.getName(),
                savedAdmin.getEmail(),
                token
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------
    // LOGIN
    // -----------------------------------------------------
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
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getBlacklisted()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "USER_BLACKLISTED");
            error.put("message", "User is blacklisted and cannot login");
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse(
                "Login successful!",
                token,
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------
    // VALIDATE TOKEN
    // -----------------------------------------------------
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
            @Parameter(description = "Auth header in the format: Bearer <token>")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        String email = jwtUtil.getEmailFromToken(token);
        return ResponseEntity.ok(Map.of("email", email));
    }
}
