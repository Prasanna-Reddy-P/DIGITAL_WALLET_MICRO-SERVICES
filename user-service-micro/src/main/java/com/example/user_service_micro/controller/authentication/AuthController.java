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
        this.restClient = RestClient.create(); // For inter-service communication
    }

    // ------------------- USER SIGNUP -------------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        /*
        @RequestBody - whatever the request we are sending via JSON body, convert it to a
        java object type SignupRequest.
         */
        if (signupRequest.getAge() < 18) {
            return ResponseEntity.badRequest().body("User must be at least 18 years old");
        }

        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists!");
        }

        // Create and populate new User entity
        User newUser = new User();
        newUser.setName(signupRequest.getName());
        newUser.setEmail(signupRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        newUser.setAge(signupRequest.getAge());
        newUser.setRole("USER");

        // Save user
        User savedUser = userRepository.save(newUser);

        // Notify wallet-service
        try {
            restClient.post()
                    .uri("http://localhost:8086/api/wallet/create?userId=" + savedUser.getId())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("⚠️ Wallet-service not reachable. Wallet will be created later.");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }


    // ------------------- ADMIN SIGNUP -------------------
    @PostMapping("/signup-admin")
    public ResponseEntity<?> signupAdmin(@Valid @RequestBody SignupRequest signupRequest,
                                         @RequestHeader("X-ADMIN-SECRET") String adminSecret) {
        final String SECRET_KEY = "SuperSecretAdminKey123";

        if (!SECRET_KEY.equals(adminSecret)) {
            return ResponseEntity.status(403).body("Forbidden: Invalid admin secret");
        }

        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists!");
        }

        // Create admin entity
        User admin = new User();
        admin.setName(signupRequest.getName());
        admin.setEmail(signupRequest.getEmail());
        admin.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        admin.setRole("ADMIN");

        User savedAdmin = userRepository.save(admin);

        // Generate token
        String token = jwtUtil.generateToken(savedAdmin.getEmail());

        // Build typed response DTO
        SignupResponse response = new SignupResponse(
                "Admin registered successfully!",
                savedAdmin.getName(),
                savedAdmin.getEmail(),
                token
        );

        return ResponseEntity.ok(response);
    }

    // login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // ✅ BLACKLIST CHECK
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

        // Generate JWT
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



    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

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
