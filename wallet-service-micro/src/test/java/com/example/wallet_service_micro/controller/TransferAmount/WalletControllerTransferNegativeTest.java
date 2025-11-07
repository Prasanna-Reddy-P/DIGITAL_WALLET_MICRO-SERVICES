package com.example.wallet_service_micro.controller.TransferAmount;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.transferMoney.TransferRequest;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTransferNegativeTest {

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
    }

    // ------------------------------------------------------------------
    // Recipient not found → UserNotFoundException
    // ------------------------------------------------------------------
    @Test
    void testTransferRecipientNotFoundThrows() {
        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(999L);
        req.setAmount(100.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(999L), eq(100.0), anyString(), eq("Primary"), eq(token)))
                .thenThrow(new com.example.wallet_service_micro.exception.user.UserNotFoundException("Recipient not found"));

        assertThrows(com.example.wallet_service_micro.exception.user.UserNotFoundException.class, () ->
                walletController.transfer(token, req)
        );
    }

    // ------------------------------------------------------------------
    // Sender wallet frozen → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testTransferSenderWalletFrozenThrows() {
        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(50.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(50.0), anyString(), eq("Primary"), eq(token)))
                .thenThrow(new IllegalArgumentException("Wallet is frozen"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transfer(token, req)
        );
        assertEquals("Wallet is frozen", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Insufficient balance → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testTransferInsufficientBalanceThrows() {
        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(5000.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(5000.0), anyString(), eq("Primary"), eq(token)))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transfer(token, req)
        );
        assertEquals("Insufficient balance", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Negative transfer amount → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testTransferNegativeAmountThrows() {
        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(-100.0);
        req.setSenderWalletName("Primary");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(-100.0), anyString(), eq("Primary"), eq(token)))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transfer(token, req)
        );
        assertEquals("Amount must be positive", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Daily limit exceeded → wallet should freeze
    // ------------------------------------------------------------------
    @Test
    void testTransferDailyLimitExceededFreezesWallet() {
        String token = "Bearer xyz";
        TransferRequest req = new TransferRequest();
        req.setReceiverId(20L);
        req.setAmount(2000.0);
        req.setSenderWalletName("Primary");

        TransferResponse resp = new TransferResponse();
        resp.setAmountTransferred(2000.0);
        resp.setFrozen(true);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(20L), eq(2000.0), anyString(), eq("Primary"), eq(token)))
                .thenReturn(resp);

        TransferResponse result = walletController.transfer(token, req).getBody();
        assertNotNull(result);
        assertTrue(result.getFrozen(), "Wallet should be frozen due to daily limit exceeded");
    }
}
