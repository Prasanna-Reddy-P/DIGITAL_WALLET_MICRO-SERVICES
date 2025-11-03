package com.example.user_service_micro.service;

import com.example.user_service_micro.config.JwtUtil;
import com.example.user_service_micro.model.User;
import com.example.user_service_micro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // Extract user from JWT token
    public User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        String email = jwtUtil.getEmailFromToken(token);
        return userRepository.findByEmail(email).orElse(null);
    }

    // ✅ Fetch all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Fetch a user by ID
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
