package com.example.user_service_micro.controller;

import com.example.user_service_micro.config.JwtUtil;
import com.example.user_service_micro.exception.UserAlreadyExistsException;
import com.example.user_service_micro.model.User;
import com.example.user_service_micro.repository.UserRepository;
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
    public ResponseEntity<?> signup(@Valid @RequestBody User user) {
        if (user.getAge() < 18) {
            return ResponseEntity.badRequest().body("User must be at least 18 years old");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists!");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        // Save user
        User savedUser = userRepository.save(user);

        // TODO: Notify wallet-service to create wallet for new user
        try {
            restClient.post()
                    .uri("http://localhost:8082/api/wallet/create?userId=" + savedUser.getId())
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
    public ResponseEntity<?> signupAdmin(@RequestBody User user,
                                         @RequestHeader("X-ADMIN-SECRET") String adminSecret) {
        final String SECRET_KEY = "SuperSecretAdminKey123"; // Use env variable in prod

        if (!SECRET_KEY.equals(adminSecret)) {
            return ResponseEntity.status(403).body("Forbidden: Invalid admin secret");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ADMIN");

        User savedAdmin = userRepository.save(user);

        String token = jwtUtil.generateToken(savedAdmin.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin registered successfully!");
        response.put("name", savedAdmin.getName());
        response.put("email", savedAdmin.getEmail());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    // ------------------- LOGIN -------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("token", token);
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

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
