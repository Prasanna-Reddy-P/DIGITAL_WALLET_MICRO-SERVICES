package com.example.user_service_micro.controller.admin;

import com.example.user_service_micro.dto.user.UserIdRequest;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import com.example.user_service_micro.service.user.UserService;
import com.example.user_service_micro.exception.auth.UnauthorizedException;
import com.example.user_service_micro.exception.auth.ForbiddenException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------
    // GET ALL USERS (ADMIN ONLY)
    // ---------------------------
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all users. Accessible only to ADMIN users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users fetched successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admins only")
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @Parameter(description = "Authorization header containing JWT token")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ---------------------------
    // GET USER BY ID
    // ---------------------------
    @Operation(
            summary = "Get user details by ID",
            description = "Fetches complete information of a user by ID. Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details retrieved",
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admins only"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(
            @Parameter(description = "JWT Bearer token")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,

            @Parameter(description = "User ID to fetch")
            @PathVariable Long userId) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // ---------------------------
    // BLACKLIST USER
    // ---------------------------
    @Operation(
            summary = "Blacklist a user",
            description = "Marks a user account as blacklisted. Only admins can perform this."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User blacklisted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admins only")
    })
    @PutMapping("/blacklist")
    public ResponseEntity<String> blacklistUser(
            @Parameter(description = "JWT Bearer token")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User ID to blacklist",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserIdRequest.class))
            )
            @RequestBody UserIdRequest request) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        userService.blacklistUser(request.getUserId());
        return ResponseEntity.ok("User blacklisted successfully");
    }

    // ---------------------------
    // UN-BLACKLIST USER
    // ---------------------------
    @Operation(
            summary = "Remove user from blacklist",
            description = "Unblacklists a user account. Admin-only access."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User unblacklisted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admins only")
    })
    @PutMapping("/unblacklist")
    public ResponseEntity<String> unblacklistUser(
            @Parameter(description = "JWT Bearer token")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User ID to unblacklist",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserIdRequest.class)))
            @RequestBody UserIdRequest request) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        userService.unblacklistUser(request.getUserId());
        return ResponseEntity.ok("User unblacklisted successfully");
    }
}
