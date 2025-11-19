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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceEdgeTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceEdgeTest.class);

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
        log.info("Initializing mocks and setting up test user data");
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("Prasanna");
        user.setEmail("prasanna@example.com");
        user.setPassword("password123");
        user.setRole("USER");

        log.debug("Test user initialized: {}", user);
    }

    // ---------------- Token extraction ----------------
    @Test
    void getUserFromToken_NullHeader() {
        log.info("Running test: getUserFromToken_NullHeader");
        User result = userService.getUserFromToken(null);
        log.debug("Result when header is null: {}", result);
        assertNull(result);
    }

    @Test
    void getUserFromToken_InvalidPrefix() {
        log.info("Running test: getUserFromToken_InvalidPrefix");
        User result = userService.getUserFromToken("Token abc.def.ghi");
        log.debug("Result with invalid prefix: {}", result);
        assertNull(result);
    }

    @Test
    void getUserFromToken_InvalidJwt() {
        log.info("Running test: getUserFromToken_InvalidJwt");
        String header = "Bearer invalid.jwt";
        when(jwtUtil.validateToken("invalid.jwt")).thenReturn(false);
        log.debug("Mocked invalid token validation for: invalid.jwt");
        User result = userService.getUserFromToken(header);
        log.debug("Result with invalid JWT: {}", result);
        assertNull(result);
    }

    @Test
    void getUserFromToken_UserNotFound() {
        log.info("Running test: getUserFromToken_UserNotFound");
        String header = "Bearer valid.jwt";
        when(jwtUtil.validateToken("valid.jwt")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("valid.jwt")).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        log.debug("Mocked valid JWT but user not found in DB");

        User result = userService.getUserFromToken(header);
        log.debug("Result when user not found: {}", result);
        assertNull(result);
    }

    @Test
    void getUserFromToken_Success() {
        log.info("Running test: getUserFromToken_Success");
        String header = "Bearer valid.jwt";
        when(jwtUtil.validateToken("valid.jwt")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("valid.jwt")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        log.debug("Mocked valid token and existing user lookup");

        User result = userService.getUserFromToken(header);
        log.debug("User returned: {}", result);
        assertNotNull(result);
        assertEquals("Prasanna", result.getName());
    }

    // ---------------- getAllUsers ----------------
    // ---------------- getAllUsers ----------------
    @Test
    void getAllUsers_EmptyList() {
        log.info("Running test: getAllUsers_EmptyList");

        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        Page<UserDTO> result = userService.getAllUsers(0, 10);

        log.debug("Resulting DTO page: {}", result.getContent());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getAllUsers_Success() {
        log.info("Running test: getAllUsers_Success");

        UserDTO dto = new UserDTO();
        dto.setName(user.getName());

        Page<User> mockPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        when(userMapper.toDTO(user)).thenReturn(dto);

        Page<UserDTO> result = userService.getAllUsers(0, 10);

        log.debug("Mapped user DTO page: {}", result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals("Prasanna", result.getContent().get(0).getName());
    }


    // ---------------- getUserById ----------------
    @Test
    void getUserById_UserNotFound() {
        log.info("Running test: getUserById_UserNotFound");
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        log.debug("Mocked DB: no user found for ID 2");

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    void getUserById_Success() {
        log.info("Running test: getUserById_Success");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        log.debug("Mocked DB: user found for ID 1");

        var response = userService.getUserById(1L);
        log.debug("UserInfoResponse returned: {}", response);
        assertEquals("Prasanna", response.getName());
        assertEquals("USER", response.getRole());
    }
}