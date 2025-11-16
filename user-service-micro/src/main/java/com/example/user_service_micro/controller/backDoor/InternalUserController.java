package com.example.user_service_micro.controller.backDoor;

import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/internal/users")
@Tag(
        name = "Internal User Controller",
        description = "Internal-only endpoints for inter-service communication"
)
public class InternalUserController {

    @Autowired private UserRepository userRepository;
    @Autowired private UserMapper userMapper;

    @Operation(
            summary = "Internal: Get user by ID",
            description = """
                          This endpoint is only meant for internal microservice calls.
                          Access is controlled by the `internalService` request attribute set by a gateway or interceptor.
                          """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not an internal service"),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserByIdInternal(
            @Parameter(description = "User ID to fetch", example = "7")
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Boolean isInternal = (Boolean) request.getAttribute("internalService");
        if (isInternal == null || !isInternal) {
            return ResponseEntity.status(403).build();
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return ResponseEntity.ok(userMapper.toUsersDTO(user));
    }
}
