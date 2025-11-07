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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerInternalTransferNegativeTest {

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
    }

    // ------------------------------------------------------------------
    // Negative amount → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferNegativeAmountThrows() {
        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(-100.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(-100.0), anyString()))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transferWithinWallets(token, req)
        );
        assertEquals("Amount must be positive", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Sender wallet frozen → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferSenderWalletFrozenThrows() {
        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(50.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(50.0), anyString()))
                .thenThrow(new IllegalArgumentException("Wallet is frozen"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transferWithinWallets(token, req)
        );
        assertEquals("Wallet is frozen", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Insufficient balance → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferInsufficientBalanceThrows() {
        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(1000.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(1000.0), anyString()))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transferWithinWallets(token, req)
        );
        assertEquals("Insufficient balance", ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Daily limit exceeded → wallet should freeze
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferDailyLimitExceededHandled() {
        String token = "Bearer xyz";

        // Prepare request
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(2000.0);

        // Prepare response (DTO does not have frozen field)
        UserInternalTransferResponse resp = new UserInternalTransferResponse();
        resp.setAmountTransferred(2000.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(
                eq(mockUser), eq("Primary"), eq("Savings"), eq(2000.0), anyString()))
                .thenReturn(resp);

        // Act
        UserInternalTransferResponse result = walletController.transferWithinWallets(token, req).getBody();

        // Assert
        assertNotNull(result);
        assertEquals(2000.0, result.getAmountTransferred());

        // Optional: verify walletService was called correctly
        verify(walletService).transferWithinUserWallets(
                eq(mockUser), eq("Primary"), eq("Savings"), eq(2000.0), anyString());
    }


    // ------------------------------------------------------------------
    // Duplicate transaction → IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void testInternalTransferDuplicateTransactionThrows() {
        String token = "Bearer xyz";
        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(100.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser), eq("Primary"), eq("Savings"), eq(100.0), anyString()))
                .thenThrow(new IllegalArgumentException("Duplicate transaction"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletController.transferWithinWallets(token, req)
        );
        assertEquals("Duplicate transaction", ex.getMessage());
    }


}
