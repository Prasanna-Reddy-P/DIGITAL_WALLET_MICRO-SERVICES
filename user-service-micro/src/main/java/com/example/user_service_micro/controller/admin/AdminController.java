package com.example.user_service_micro.controller.admin;

import com.example.user_service_micro.dto.UserIdRequest;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import com.example.user_service_micro.service.user.UserService;
import com.example.user_service_micro.exception.auth.UnauthorizedException;
import com.example.user_service_micro.exception.auth.ForbiddenException;
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

    @GetMapping // represents as a handler for HTTP GET requests.
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                        @PathVariable Long userId) {
        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        UserInfoResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/blacklist")
    public ResponseEntity<String> blacklistUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        userService.blacklistUser(request.getUserId());
        return ResponseEntity.ok("User blacklisted successfully");
    }

    @PutMapping("/unblacklist")
    public ResponseEntity<String> unblacklistUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        userService.unblacklistUser(request.getUserId());
        return ResponseEntity.ok("User unblacklisted successfully");
    }

}

/*
What is ResponseEntity?

ResponseEntity is a Spring class that represents the entire HTTP response, including:

HTTP status code (200, 400, 401, 500…)
Headers (Content-Type, Authorization, Location, etc.)
Body (JSON, objects, text, etc.)
It is the most powerful way to control an HTTP response in Spring MVC and Spring Web.
 */
