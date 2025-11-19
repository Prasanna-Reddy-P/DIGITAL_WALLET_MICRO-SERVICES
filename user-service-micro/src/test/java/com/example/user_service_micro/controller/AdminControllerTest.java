package com.example.user_service_micro.controller;

import com.example.user_service_micro.controller.admin.AdminController;
import com.example.user_service_micro.dto.pagination.PaginationRequestDTO;
import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.dto.user.UserIdRequest;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import com.example.user_service_micro.exception.auth.ForbiddenException;
import com.example.user_service_micro.exception.auth.UnauthorizedException;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRole("USER");
    }

    // ---------------- getAllUsers -------------------

    private PaginationRequestDTO createPaginationDTO() {
        PaginationRequestDTO dto = new PaginationRequestDTO();
        dto.setPage(0);
        dto.setSize(10);
        return dto;
    }

    @Test
    void getAllUsers_Unauthorized() {
        when(userService.getUserFromToken("token")).thenReturn(null);

        PaginationRequestDTO dto = createPaginationDTO();

        assertThrows(UnauthorizedException.class,
                () -> adminController.getAllUsers("token", dto));
    }

    @Test
    void getAllUsers_Forbidden_NotAdmin() {
        when(userService.getUserFromToken("token")).thenReturn(normalUser);

        PaginationRequestDTO dto = createPaginationDTO();

        assertThrows(ForbiddenException.class,
                () -> adminController.getAllUsers("token", dto));
    }

    @Test
    void getAllUsers_Success() {
        when(userService.getUserFromToken("token")).thenReturn(adminUser);

        PaginationRequestDTO dto = createPaginationDTO();

        UserDTO userDto = new UserDTO();
        userDto.setName("Prasanna");

        Page<UserDTO> mockPage = new PageImpl<>(List.of(userDto));
        when(userService.getAllUsers(0, 10)).thenReturn(mockPage);

        ResponseEntity<Page<UserDTO>> response =
                adminController.getAllUsers("token", dto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
    }

    // ---------------- getUserById -------------------

    @Test
    void getUserById_Unauthorized() {
        when(userService.getUserFromToken("token")).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> adminController.getUserById("token", 5L));
    }

    @Test
    void getUserById_Forbidden_NotAdmin() {
        when(userService.getUserFromToken("token")).thenReturn(normalUser);

        assertThrows(ForbiddenException.class,
                () -> adminController.getUserById("token", 5L));
    }

    @Test
    void getUserById_Success() {
        when(userService.getUserFromToken("token")).thenReturn(adminUser);

        UserInfoResponse responseObj = new UserInfoResponse();
        responseObj.setName("Prasanna");
        when(userService.getUserById(5L)).thenReturn(responseObj);

        ResponseEntity<UserInfoResponse> response =
                adminController.getUserById("token", 5L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Prasanna", response.getBody().getName());
    }

    // ---------------- blacklistUser -------------------

    @Test
    void blacklistUser_Unauthorized() {
        when(userService.getUserFromToken("token")).thenReturn(null);

        UserIdRequest req = new UserIdRequest(10L);

        assertThrows(UnauthorizedException.class,
                () -> adminController.blacklistUser("token", req));
    }

    @Test
    void blacklistUser_Forbidden_NotAdmin() {
        when(userService.getUserFromToken("token")).thenReturn(normalUser);

        UserIdRequest req = new UserIdRequest(10L);

        assertThrows(ForbiddenException.class,
                () -> adminController.blacklistUser("token", req));
    }

    @Test
    void blacklistUser_Success() {
        when(userService.getUserFromToken("token")).thenReturn(adminUser);

        UserIdRequest req = new UserIdRequest(10L);

        ResponseEntity<String> response =
                adminController.blacklistUser("token", req);

        verify(userService, times(1)).blacklistUser(10L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User blacklisted successfully", response.getBody());
    }

    // ---------------- unblacklistUser -------------------

    @Test
    void unblacklistUser_Unauthorized() {
        when(userService.getUserFromToken("token")).thenReturn(null);

        UserIdRequest req = new UserIdRequest(10L);

        assertThrows(UnauthorizedException.class,
                () -> adminController.unblacklistUser("token", req));
    }

    @Test
    void unblacklistUser_Forbidden_NotAdmin() {
        when(userService.getUserFromToken("token")).thenReturn(normalUser);

        UserIdRequest req = new UserIdRequest(10L);

        assertThrows(ForbiddenException.class,
                () -> adminController.unblacklistUser("token", req));
    }

    @Test
    void unblacklistUser_Success() {
        when(userService.getUserFromToken("token")).thenReturn(adminUser);

        UserIdRequest req = new UserIdRequest(10L);

        ResponseEntity<String> response =
                adminController.unblacklistUser("token", req);

        verify(userService, times(1)).unblacklistUser(10L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User unblacklisted successfully", response.getBody());
    }
}
