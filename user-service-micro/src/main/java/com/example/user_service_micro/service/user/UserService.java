package com.example.user_service_micro.service.user;

import com.example.user_service_micro.config.jwt.JwtUtil;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import com.example.user_service_micro.exception.user.UserNotFoundException;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
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

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public UserInfoResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID " + userId));

        return new UserInfoResponse(user.getName(), user.getEmail(), user.getRole());
    }

    public void blacklistUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID " + userId));

        user.setBlacklisted(true);   // ✅ Set new field
        userRepository.save(user);   // ✅ Persist update
    }


}
