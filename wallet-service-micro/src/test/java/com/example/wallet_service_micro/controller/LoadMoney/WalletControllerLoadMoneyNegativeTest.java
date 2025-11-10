package com.example.wallet_service_micro.controller.LoadMoney;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerLoadMoneyNegativeTest {

    private static final Logger log = LoggerFactory.getLogger(WalletControllerLoadMoneyNegativeTest.class);

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
        log.info("Initializing mocks and test user data");
        MockitoAnnotations.openMocks(this);

        mockUser = new UserDTO();
        mockUser.setId(10L);
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");
        mockUser.setName("Test User");

        log.debug("Test user initialized: {}", mockUser);
    }

    @Test
    void testLoadMoneyNegativeAmountThrows() {
        log.info("Running test: testLoadMoneyNegativeAmountThrows");
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

        log.debug("Exception thrown: {}", ex.getMessage());
        assertEquals("Amount must be positive", ex.getMessage());
        verify(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));
    }

    @Test
    void testLoadMoneyDailyLimitExceededThrows() {
        log.info("Running test: testLoadMoneyDailyLimitExceededThrows");
        String token = "Bearer xyz";

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(10000.0);
        req.setWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        doThrow(new IllegalArgumentException("Daily limit exceeded"))
                .when(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.loadMoney(token, req)
        );

        log.debug("Exception thrown: {}", ex.getMessage());
        assertEquals("Daily limit exceeded", ex.getMessage());
        verify(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));
    }

    @Test
    void testLoadMoneyDuplicateTransactionThrows() {
        log.info("Running test: testLoadMoneyDuplicateTransactionThrows");
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

        log.debug("Exception thrown: {}", ex.getMessage());
        assertEquals("Duplicate transaction", ex.getMessage());
        verify(walletService).loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary"));
    }
}
