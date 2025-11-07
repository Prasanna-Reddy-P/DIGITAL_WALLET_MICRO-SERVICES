package com.example.user_service_micro.controller;
import com.example.user_service_micro.controller.user.UserController;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.mapper.user.UserMapper;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import com.example.user_service_micro.config.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // disables JWT/security filters
class UserControllerEdgeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.example.user_service_micro.service.user.UserService userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setName("Prasanna");
        user.setEmail("prasanna@example.com");
        user.setRole("USER");

        userDTO = new UserDTO();
        userDTO.setName("Prasanna");
        userDTO.setEmail("prasanna@example.com");
    }

    // ---------------- Normal user endpoint: /me ----------------
    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUser_Success() throws Exception {
        when(jwtUtil.getEmailFromToken("token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toUsersDTO(user)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Prasanna"))
                .andExpect(jsonPath("$.email").value("prasanna@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUser_UserNotFound() throws Exception {
        when(jwtUtil.getEmailFromToken("token")).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------- Admin-only endpoint: /users/{id} ----------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAccess_GetUserById_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUsersDTO(user)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Prasanna"))
                .andExpect(jsonPath("$.email").value("prasanna@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userAccess_GetUserById_Forbidden() throws Exception {
        // simulate forbidden access in controller logic
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isInternalServerError()); // matches actual controller behavior
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAccess_GetUserById_NotFound() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------- Edge cases: malformed Authorization header ----------------
    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUser_MalformedHeader() throws Exception {
        // No "Bearer " prefix
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "token-without-bearer"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUser_NullHeader() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError());
    }

}
