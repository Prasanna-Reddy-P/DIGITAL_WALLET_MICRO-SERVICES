package com.example.user_service_micro.controller.user;

import com.example.user_service_micro.config.jwt.JwtUtil;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import com.example.user_service_micro.service.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDTO dto = userMapper.toUsersDTO(user);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        UserDTO dto = userMapper.toUsersDTO(user);
        return ResponseEntity.ok(dto);

    }

}
