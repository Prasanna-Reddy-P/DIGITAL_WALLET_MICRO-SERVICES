package com.example.wallet_service_micro.controller.walletCreation;


import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletRequest;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletResponse;
import com.example.wallet_service_micro.service.factory.WalletManagementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerCreateWalletNegativeTest {

    @Mock
    private UserClient userClient;

    @Mock
    private WalletManagementService walletManagementService;

    @InjectMocks
    private WalletController walletController;

    private UserDTO mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockUser = new UserDTO();
        mockUser.setId(10L);
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");
        mockUser.setName("Test User");
    }

    // ------------------------------------------------------------------
    // Wallet name empty → validation error
    // ------------------------------------------------------------------
    @Test
    void testCreateWalletEmptyNameReturnsBadRequest() {
        String token = "Bearer xyz";
        CreateWalletRequest req = new CreateWalletRequest();
        req.setWalletName(""); // empty

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);

        ResponseEntity<CreateWalletResponse> response = walletController.createWallet(token, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Wallet name cannot be empty", response.getBody().getMessage());
    }


    // ------------------------------------------------------------------
    // Wallet already exists → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testCreateWalletAlreadyExistsThrows() {
        String token = "Bearer xyz";
        CreateWalletRequest req = new CreateWalletRequest();
        req.setWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletManagementService.createWallet(mockUser, "Primary"))
                .thenThrow(new IllegalArgumentException("Wallet already exists"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.createWallet(token, req)
        );

        assertEquals("Wallet already exists", ex.getMessage());
    }
}
