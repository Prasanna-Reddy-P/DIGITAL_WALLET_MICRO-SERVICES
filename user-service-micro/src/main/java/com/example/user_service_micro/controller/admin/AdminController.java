package com.example.user_service_micro.controller.admin;

import com.example.user_service_micro.dto.pagination.PaginationRequestDTO;
import com.example.user_service_micro.dto.user.UserIdRequest;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import com.example.user_service_micro.service.user.UserService;
import com.example.user_service_micro.exception.auth.UnauthorizedException;
import com.example.user_service_micro.exception.auth.ForbiddenException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // --------------------------------------------------------------------
    // GET ALL USERS WITH PAGINATION — VIA JSON BODY (POST)
    // --------------------------------------------------------------------
    @Operation(
            summary = "Get paginated list of users (Admin only)",
            description = "Fetches users using pagination sent inside request body."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users fetched successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Admins only")
    })
    @PostMapping("/list")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody PaginationRequestDTO request
    ) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        Page<UserDTO> users = userService.getAllUsers(request.getPage(), request.getSize());
        return ResponseEntity.ok(users);
    }


    // --------------------------------------------------------------------
    // GET USER BY ID
    // --------------------------------------------------------------------
    @Operation(
            summary = "Get user details by ID",
            description = "Admin-only access"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details retrieved",
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId
    ) {
        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        return ResponseEntity.ok(userService.getUserById(userId));
    }


    // --------------------------------------------------------------------
    // BLACKLIST USER
    // --------------------------------------------------------------------
    @Operation(summary = "Blacklist a user", description = "Admin-only")
    @PutMapping("/blacklist")
    public ResponseEntity<String> blacklistUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request
    ) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        userService.blacklistUser(request.getUserId());
        return ResponseEntity.ok("User blacklisted successfully");
    }


    // --------------------------------------------------------------------
    // UN-BLACKLIST USER
    // --------------------------------------------------------------------
    @Operation(summary = "Unblacklist a user", description = "Admin-only")
    @PutMapping("/unblacklist")
    public ResponseEntity<String> unblacklistUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request
    ) {

        User admin = userService.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        userService.unblacklistUser(request.getUserId());
        return ResponseEntity.ok("User unblacklisted successfully");
    }
}
