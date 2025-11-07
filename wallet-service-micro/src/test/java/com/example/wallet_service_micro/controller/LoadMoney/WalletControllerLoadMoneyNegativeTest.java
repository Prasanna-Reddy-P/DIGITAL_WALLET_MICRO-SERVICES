package com.example.wallet_service_micro.controller.LoadMoney;
import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.wallet.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerLoadMoneyNegativeTest {

    @Mock
    private UserClient userClient;

    @Mock
    private WalletService walletService;

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
    // ❌ Negative Amount
    // ------------------------------------------------------------------
    @Test
    void testLoadMoneyNegativeAmountThrows() {
        String token = "Bearer xyz";

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(-100.0);
        req.setWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        doThrow(new IllegalArgumentException("Amount must be positive"))
                .when(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.loadMoney(token, req)
        );

        assertEquals("Amount must be positive", ex.getMessage());
        verify(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));
    }

    // ------------------------------------------------------------------
    // ❌ Daily Limit Exceeded
    // ------------------------------------------------------------------
    @Test
    void testLoadMoneyDailyLimitExceededThrows() {
        String token = "Bearer xyz";

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(10000.0); // exceeds daily limit
        req.setWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        doThrow(new IllegalArgumentException("Daily limit exceeded"))
                .when(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.loadMoney(token, req)
        );

        assertEquals("Daily limit exceeded", ex.getMessage());
        verify(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));
    }

    // ------------------------------------------------------------------
    // ❌ Duplicate Transaction
    // ------------------------------------------------------------------
    @Test
    void testLoadMoneyDuplicateTransactionThrows() {
        String token = "Bearer xyz";

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(200.0);
        req.setWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        doThrow(new IllegalArgumentException("Duplicate transaction"))
                .when(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.loadMoney(token, req)
        );

        assertEquals("Duplicate transaction", ex.getMessage());
        verify(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));
    }
}

