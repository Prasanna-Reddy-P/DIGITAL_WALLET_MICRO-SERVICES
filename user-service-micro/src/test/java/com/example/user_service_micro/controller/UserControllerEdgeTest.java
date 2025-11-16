package com.example.user_service_micro.controller;

import com.example.user_service_micro.config.jwt.JwtUtil;
import com.example.user_service_micro.controller.user.UserController;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.http.ResponseEntity;

class UserControllerEdgeTest {

    private static final Logger log = LoggerFactory.getLogger(UserControllerEdgeTest.class);

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        log.info("Setting up test data...");

        user = new User();
        user.setId(1L);
        user.setName("Prasanna");
        user.setEmail("prasanna@example.com");

        userDTO = new UserDTO();
        userDTO.setName("Prasanna");
        userDTO.setEmail("prasanna@example.com");

        log.info("Setup complete.");
    }

    /* ===========================
       TEST: getCurrentUser
       =========================== */
    @Test
    void getCurrentUser_success() {
        log.info("Running: getCurrentUser_success");

        String authHeader = "Bearer token";

        when(jwtUtil.getEmailFromToken("token"))
                .thenReturn(user.getEmail());
        log.info("Mocked jwtUtil.getEmailFromToken");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        log.info("Mocked userRepository.findByEmail");

        when(userMapper.toUsersDTO(user))
                .thenReturn(userDTO);
        log.info("Mocked userMapper.toUsersDTO");

        ResponseEntity<UserDTO> response =
                userController.getCurrentUser(authHeader);

        log.info("Controller response: {}", response);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Prasanna", response.getBody().getName());
        assertEquals("prasanna@example.com", response.getBody().getEmail());

        log.info("Test getCurrentUser_success PASSED");
    }

    @Test
    void getCurrentUser_userNotFound() {
        log.info("Running: getCurrentUser_userNotFound");

        String authHeader = "Bearer token";

        when(jwtUtil.getEmailFromToken("token"))
                .thenReturn("x@x.com");
        log.info("Mocked jwtUtil.getEmailFromToken with unknown email");

        when(userRepository.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());
        log.info("Mocked userRepository.findByEmail to return empty");

        assertThrows(UsernameNotFoundException.class,
                () -> userController.getCurrentUser(authHeader));

        log.info("Test getCurrentUser_userNotFound PASSED");
    }

    /* ===========================
       TEST: getUserById
       =========================== */
    @Test
    void getUserById_success() {
        log.info("Running: getUserById_success");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        log.info("Mocked userRepository.findById");

        when(userMapper.toUsersDTO(user))
                .thenReturn(userDTO);
        log.info("Mocked userMapper.toUsersDTO");

        ResponseEntity<UserDTO> response =
                userController.getUserById(1L);

        log.info("Controller response: {}", response);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Prasanna", response.getBody().getName());

        log.info("Test getUserById_success PASSED");
    }

    @Test
    void getUserById_notFound() {
        log.info("Running: getUserById_notFound");

        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());
        log.info("Mocked userRepository.findById to return empty");

        assertThrows(UsernameNotFoundException.class,
                () -> userController.getUserById(2L));

        log.info("Test getUserById_notFound PASSED");
    }
}
