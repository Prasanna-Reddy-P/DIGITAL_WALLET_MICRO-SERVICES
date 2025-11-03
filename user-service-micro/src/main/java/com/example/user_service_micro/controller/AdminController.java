package com.example.user_service_micro.controller;

import com.example.user_service_micro.model.User;
import com.example.user_service_micro.dto.UserInfoResponse;
import com.example.user_service_micro.service.UserService;
import com.example.user_service_micro.exception.UnauthorizedException;
import com.example.user_service_micro.exception.ForbiddenException;
import com.example.user_service_micro.exception.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // -------------------- GET ALL USERS --------------------
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        logger.info("Received admin request: GET /api/admin/users");

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        List<User> users = userService.getAllUsers();
        logger.info("Admin fetched {} users successfully", users.size());

        return ResponseEntity.ok(users);
    }

    // -------------------- GET USER BY ID --------------------
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        logger.info("Received admin request: GET /api/admin/users/{}", userId);

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        User user = userService.getUserById(userId);
        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        UserInfoResponse response = new UserInfoResponse(
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        logger.info("Admin fetched user {} successfully", userId);
        return ResponseEntity.ok(response);
    }
}
