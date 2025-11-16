package com.example.user_service_micro.controller.backDoor;


import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserByIdInternal(@PathVariable Long id, HttpServletRequest request) {
        Boolean isInternal = (Boolean) request.getAttribute("internalService");
        if (isInternal == null || !isInternal) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(userMapper.toUsersDTO(user));
    }
}
