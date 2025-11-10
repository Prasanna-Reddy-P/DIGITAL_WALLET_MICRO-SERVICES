package com.example.wallet_service_micro.controller.TransferAmount;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.transferMoney.TransferRequest;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTransferNegativeTest {

    private static final Logger log = LoggerFactory.getLogger(WalletControllerTransferNegativeTest.class);

    @Mock private UserClient userClient;
    @Mock private WalletService walletService;
    @Mock private WalletManagementService walletManagementService;

    @InjectMocks private WalletController walletController;

    private UserDTO mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockUser = new UserDTO();
        mockUser.setId(10L);
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");
        mockUser.setName("Test User");

        log.info("Setup completed: Mock user initialized with email {}", mockUser.getEmail());
    }

    @Test
    void testTransferRecipientNotFoundThrows() {
        log.info("Running test: Recipient not found → UserNotFoundException");

        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(999L);
        req.setAmount(100.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(999L), eq(100.0),
                anyString(), eq("Primary"), eq(token)))
                .thenThrow(new UserNotFoundException("Recipient not found"));

        assertThrows(UserNotFoundException.class, () -> walletController.transfer(token, req));
        log.info("Test passed: Recipient not found correctly threw UserNotFoundException");
    }

    @Test
    void testTransferSenderWalletFrozenThrows() {
        log.info("Running test: Sender wallet frozen → IllegalArgumentException");

        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(50.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(50.0),
                anyString(), eq("Primary"), eq(token)))
                .thenThrow(new IllegalArgumentException("Wallet is frozen"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transfer(token, req));
        assertEquals("Wallet is frozen", ex.getMessage());

        log.info("Test passed: Frozen wallet correctly threw IllegalArgumentException");
    }

    @Test
    void testTransferInsufficientBalanceThrows() {
        log.info("Running test: Insufficient balance → IllegalArgumentException");

        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(5000.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(5000.0),
                anyString(), eq("Primary"), eq(token)))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transfer(token, req));
        assertEquals("Insufficient balance", ex.getMessage());

        log.info("Test passed: Insufficient balance correctly threw IllegalArgumentException");
    }

    @Test
    void testTransferNegativeAmountThrows() {
        log.info("Running test: Negative transfer amount → IllegalArgumentException");

        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(-100.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(-100.0),
                anyString(), eq("Primary"), eq(token)))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletController.transfer(token, req));
        assertEquals("Amount must be positive", ex.getMessage());

        log.info("Test passed: Negative amount correctly threw IllegalArgumentException");
    }

    @Test
    void testTransferDailyLimitExceededFreezesWallet() {
        log.info("Running test: Daily limit exceeded → Wallet should freeze");

        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(2000.0);
        req.setSenderWalletName("Primary");

        TransferResponse resp = new TransferResponse();
        resp.setAmountTransferred(2000.0);
        resp.setFrozen(true);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(2000.0),
                anyString(), eq("Primary"), eq(token)))
                .thenReturn(resp);

        TransferResponse result = walletController.transfer(token, req).getBody();
        assertNotNull(result);
        assertTrue(result.getFrozen(), "Wallet should be frozen due to daily limit exceeded");

        log.info("Test passed: Daily limit exceeded froze wallet successfully, transferred {}", result.getAmountTransferred());
    }
}
