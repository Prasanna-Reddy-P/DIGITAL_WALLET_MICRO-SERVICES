package com.example.user_service_micro.service;

import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import com.example.user_service_micro.exception.user.UserNotFoundException;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import com.example.user_service_micro.service.user.UserService;
import com.example.user_service_micro.config.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceEdgeTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("Prasanna");
        user.setEmail("prasanna@example.com");
        user.setPassword("password123");
        user.setRole("USER");
    }

    // ---------------- Token extraction ----------------
    @Test
    void getUserFromToken_NullHeader() {
        User result = userService.getUserFromToken(null);
        assertNull(result);
    }

    @Test
    void getUserFromToken_InvalidPrefix() {
        User result = userService.getUserFromToken("Token abc.def.ghi");
        assertNull(result);
    }

    @Test
    void getUserFromToken_InvalidJwt() {
        String header = "Bearer invalid.jwt";
        when(jwtUtil.validateToken("invalid.jwt")).thenReturn(false);
        User result = userService.getUserFromToken(header);
        assertNull(result);
    }

    @Test
    void getUserFromToken_UserNotFound() {
        String header = "Bearer valid.jwt";
        when(jwtUtil.validateToken("valid.jwt")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("valid.jwt")).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        User result = userService.getUserFromToken(header);
        assertNull(result);
    }

    @Test
    void getUserFromToken_Success() {
        String header = "Bearer valid.jwt";
        when(jwtUtil.validateToken("valid.jwt")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("valid.jwt")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.getUserFromToken(header);
        assertNotNull(result);
        assertEquals("Prasanna", result.getName());
    }

    // ---------------- getAllUsers ----------------
    @Test
    void getAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDTO> result = userService.getAllUsers();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_Success() {
        UserDTO dto = new UserDTO();
        dto.setName(user.getName());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        List<UserDTO> result = userService.getAllUsers();
        assertEquals(1, result.size());
        assertEquals("Prasanna", result.get(0).getName());
    }

    // ---------------- getUserById ----------------
    @Test
    void getUserById_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        var response = userService.getUserById(1L);
        assertEquals("Prasanna", response.getName());
        assertEquals("USER", response.getRole());
    }
}
