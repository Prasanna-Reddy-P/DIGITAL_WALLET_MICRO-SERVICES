package com.example.wallet_service_micro.controller.InternalTransfer;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferRequest;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.service.wallet.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerInternalTransferNegativeTest {

    private static final Logger log =
            LoggerFactory.getLogger(WalletControllerInternalTransferNegativeTest.class);

    @Mock
    private UserClient userClient;

    @Mock
    private WalletService walletService;

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

        log.info("[Setup] Mock user initialized for internal transfer tests.");
    }

    // ------------------------------------------------------------------
    // Negative amount → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferNegativeAmountThrows() {
        log.info("[Test] Validating negative amount transfer scenario...");

        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(-100.0);

        log.info("[Input] Sender=Primary, Receiver=Savings, Amount={}", req.getAmount());

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(-100.0), anyString()))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transferWithinWallets(token, req));

        log.info("[Assert] Exception matched: {}", ex.getMessage());
        assertEquals("Amount must be positive", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Sender wallet frozen → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferSenderWalletFrozenThrows() {
        log.info("[Test] Validating frozen wallet scenario...");

        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(50.0);

        log.info("[Input] Sender=Primary (Frozen), Amount={}", req.getAmount());

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(50.0), anyString()))
                .thenThrow(new IllegalArgumentException("Wallet is frozen"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transferWithinWallets(token, req));

        log.info("[Assert] Exception matched: {}", ex.getMessage());
        assertEquals("Wallet is frozen", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Insufficient balance → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferInsufficientBalanceThrows() {
        log.info("[Test] Validating insufficient balance scenario...");

        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(1000.0);

        log.info("[Input] Sender=Primary, Amount={} (Insufficient expected)", req.getAmount());

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(1000.0), anyString()))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transferWithinWallets(token, req));

        log.info("[Assert] Exception matched: {}", ex.getMessage());
        assertEquals("Insufficient balance", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Daily limit exceeded → wallet should freeze
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferDailyLimitExceededHandled() {
        log.info("[Test] Validating daily limit exceeded scenario...");

        String token = "Bearer xyz";

        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(2000.0);

        log.info("[Input] Sender=Primary, Amount={} (Daily limit expected)", req.getAmount());

        UserInternalTransferResponse resp = new UserInternalTransferResponse();
        resp.setAmountTransferred(2000.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(2000.0), anyString()))
                .thenReturn(resp);

        UserInternalTransferResponse result =
                walletController.transferWithinWallets(token, req).getBody();

        log.info("[Assert] Successful response received for daily limit case.");
        assertNotNull(result);
        assertEquals(2000.0, result.getAmountTransferred());

        verify(walletService).transferWithinUserWallets(
                eq(mockUser), eq("Primary"), eq("Savings"), eq(2000.0), anyString());
    }

    // ------------------------------------------------------------------
    // Duplicate transaction → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferDuplicateTransactionThrows() {
        log.info("[Test] Validating duplicate transaction scenario...");

        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(100.0);

        log.info("[Input] Sender=Primary, Amount={} (Duplicate expected)", req.getAmount());

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(100.0), anyString()))
                .thenThrow(new IllegalArgumentException("Duplicate transaction"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transferWithinWallets(token, req));

        log.info("[Assert] Exception matched: {}", ex.getMessage());
        assertEquals("Duplicate transaction", ex.getMessage());
    }
}
