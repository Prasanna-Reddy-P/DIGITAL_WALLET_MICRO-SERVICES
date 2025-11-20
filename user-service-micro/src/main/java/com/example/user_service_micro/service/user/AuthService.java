package com.example.user_service_micro.service.user;
import com.example.user_service_micro.config.jwt.JwtUtil;
import com.example.user_service_micro.dto.credentials.LoginRequest;
import com.example.user_service_micro.dto.credentials.LoginResponse;
import com.example.user_service_micro.dto.credentials.SignupRequest;
import com.example.user_service_micro.dto.credentials.SignupResponse;
import com.example.user_service_micro.exception.auth.*;
import com.example.user_service_micro.exception.user.*;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import com.example.user_service_micro.config.client.WalletClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WalletClient walletClient;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String ADMIN_SECRET = "SuperSecretAdminKey123";

    public AuthService(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            WalletClient walletClient,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.walletClient = walletClient;
        this.passwordEncoder = passwordEncoder;
    }

    // ======================================================================
    // USER SIGNUP
    // ======================================================================
    public SignupResponse signup(SignupRequest request) {

        log.info("Signup attempt for email: {}", request.getEmail());

        validateSignupRequest(request);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAge(request.getAge());
        user.setRole("USER");

        User saved = userRepository.save(user);

        log.info("User created successfully: id={}, email={}", saved.getId(), saved.getEmail());
        log.info("Calling wallet microservice to create wallet...");

        walletClient.createWallet(saved.getId());

        log.info("Wallet creation triggered for user {}", saved.getId());

        return new SignupResponse(
                "User registered successfully!",
                saved.getName(),
                saved.getEmail(),
                jwtUtil.generateToken(saved.getEmail())
        );
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request.getAge() < 18) {
            log.warn("Signup failed — age too low: {}", request.getAge());
            throw new IllegalArgumentException("User must be at least 18 years old");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Signup failed — email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists!");
        }
    }

    // ======================================================================
    // ADMIN SIGNUP
    // ======================================================================
    public SignupResponse signupAdmin(SignupRequest request, String secret) {

        log.info("Admin signup attempt for email: {}", request.getEmail());

        if (!ADMIN_SECRET.equals(secret)) {
            log.error("Admin signup failed — invalid secret");
            throw new ForbiddenException("Invalid admin secret");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Admin signup failed — email already exists");
            throw new UserAlreadyExistsException("Email already exists!");
        }

        User admin = new User();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole("ADMIN");

        User saved = userRepository.save(admin);

        log.info("Admin created successfully: {}", saved.getEmail());

        return new SignupResponse(
                "Admin registered successfully!",
                saved.getName(),
                saved.getEmail(),
                jwtUtil.generateToken(saved.getEmail())
        );
    }

    // ======================================================================
    // LOGIN
    // ======================================================================
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for email {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (Boolean.TRUE.equals(user.getBlacklisted())) {
            log.warn("Blacklisted user {} attempted to login", user.getEmail());
            throw new ForbiddenException("User is blacklisted and cannot login");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("User {} logged in successfully", request.getEmail());

        return new LoginResponse(
                "Login successful!",
                jwtUtil.generateToken(user.getEmail()),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // ======================================================================
    // VALIDATE TOKEN
    // ======================================================================
    public String validateToken(String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Token validation failed — missing header");
            throw new UnauthorizedException("Missing Authorization header");
        }

        String token = header.substring(7);

        if (!jwtUtil.validateToken(token)) {
            log.warn("Token validation failed — invalid token");
            throw new UnauthorizedException("Invalid token");
        }

        String email = jwtUtil.getEmailFromToken(token);
        log.info("Token valid for email {}", email);

        return email;
    }
}
