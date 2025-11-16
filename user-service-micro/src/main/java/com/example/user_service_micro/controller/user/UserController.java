package com.example.user_service_micro.controller.user;

import com.example.user_service_micro.config.jwt.JwtUtil;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import com.example.user_service_micro.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "User Controller", description = "Endpoints related to user details and profile")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    // ----------------------------------------------------------
    // GET CURRENT LOGGED-IN USER
    // ----------------------------------------------------------
    @Operation(
            summary = "Get current logged-in user",
            description = "Returns details of the authenticated user using the JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched user"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "404", description = "User not found in database")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @Parameter(description = "JWT token in the format: Bearer <token>")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDTO dto = userMapper.toUsersDTO(user);
        return ResponseEntity.ok(dto);
    }

    // ----------------------------------------------------------
    // GET USER BY ID (ADMIN ONLY)
    // ----------------------------------------------------------
    @Operation(
            summary = "Get user by ID (Admin only)",
            description = "Returns user details for the given ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found for the given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve", example = "5")
            @PathVariable Long id
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        UserDTO dto = userMapper.toUsersDTO(user);
        return ResponseEntity.ok(dto);
    }
}
