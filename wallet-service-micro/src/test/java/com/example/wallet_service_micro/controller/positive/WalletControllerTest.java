package com.example.wallet_service_micro.controller.positive;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.controller.wallet.WalletController;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferRequest;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferResponse;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.transferMoney.TransferRequest;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletRequest;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.wallet.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTest {

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
    // ✅ Test: Get Balance
    // ------------------------------------------------------------------
    @Test
    void testGetBalance() {
        String token = "Bearer xyz";
        String walletName = "Primary";

        Wallet wallet = new Wallet(10L, walletName);
        WalletBalanceResponse response = new WalletBalanceResponse();
        response.setBalance(500.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletManagementService.getExistingWallet(mockUser, walletName)).thenReturn(wallet);
        when(walletService.toWalletBalanceResponse(wallet)).thenReturn(response);

        ResponseEntity<WalletBalanceResponse> result =
                walletController.getBalance(token, walletName);

        assertEquals(500.0, result.getBody().getBalance());
        verify(walletManagementService).getExistingWallet(mockUser, walletName);
    }

    // ------------------------------------------------------------------
    // ✅ Test: Get Transactions
    // ------------------------------------------------------------------
    @Test
    void testGetTransactions() {
        String token = "Bearer xyz";
        String walletName = "Primary";

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletManagementService.getExistingWallet(mockUser, walletName))
                .thenReturn(new Wallet(mockUser.getId(), walletName));

        TransactionDTO tx = new TransactionDTO();
        tx.setAmount(200.0);

        Page<TransactionDTO> page = new PageImpl<>(List.of(tx));

        when(walletService.getTransactionsByWallet(mockUser, walletName, 0, 10))
                .thenReturn(page);

        ResponseEntity<Page<TransactionDTO>> result =
                walletController.getTransactions(token, walletName, 0, 10);

        assertEquals(1, result.getBody().getTotalElements());
    }

    // ------------------------------------------------------------------
    // ✅ Test: Load Money
    // ------------------------------------------------------------------
    @Test
    void testLoadMoney() {
        String token = "Bearer xyz";

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(100.0);
        req.setWalletName("Primary");

        LoadMoneyResponse resp = mock(LoadMoneyResponse.class);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.loadMoney(eq(mockUser), eq(req), anyString(), eq("Primary")))
                .thenReturn(resp);

        ResponseEntity<LoadMoneyResponse> result =
                walletController.loadMoney(token, req);

        assertEquals(resp, result.getBody());
    }


    // ------------------------------------------------------------------
    // ✅ Test: Transfer money to another user
    // ------------------------------------------------------------------
    @Test
    void testTransfer() {
        String token = "Bearer xyz";

        TransferRequest req = new TransferRequest();
        req.setReceiverId(99L);
        req.setAmount(250.0);
        req.setSenderWalletName("Primary");

        TransferResponse resp = new TransferResponse();
        resp.setAmountTransferred(250.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferAmount(eq(mockUser), eq(99L), eq(250.0),
                anyString(), eq("Primary"), eq(token)))
                .thenReturn(resp);

        ResponseEntity<TransferResponse> result =
                walletController.transfer(token, req);

        assertEquals(250.0, result.getBody().getAmountTransferred());
    }

    // ------------------------------------------------------------------
    // ✅ Test: Get all wallets
    // ------------------------------------------------------------------
    @Test
    void testGetAllWallets() {
        String token = "Bearer xyz";

        WalletBalanceResponse w1 = new WalletBalanceResponse();
        WalletBalanceResponse w2 = new WalletBalanceResponse();

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.getAllWalletsForUserDTO(mockUser))
                .thenReturn(List.of(w1, w2));

        ResponseEntity<List<WalletBalanceResponse>> result =
                walletController.getAllWallets(token);

        assertEquals(2, result.getBody().size());
    }

    // ------------------------------------------------------------------
    // ✅ Test: Internal Transfer
    // ------------------------------------------------------------------
    @Test
    void testInternalTransfer() {
        String token = "Bearer xyz";

        UserInternalTransferRequest req = new UserInternalTransferRequest();
        req.setSenderWalletName("Primary");
        req.setReceiverWalletName("Savings");
        req.setAmount(150.0);

        UserInternalTransferResponse resp = new UserInternalTransferResponse();
        resp.setAmountTransferred(150.0);

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletService.transferWithinUserWallets(eq(mockUser),
                eq("Primary"), eq("Savings"), eq(150.0), anyString()))
                .thenReturn(resp);

        ResponseEntity<UserInternalTransferResponse> result =
                walletController.transferWithinWallets(token, req);

        assertEquals(150.0, result.getBody().getAmountTransferred());
    }

    // ------------------------------------------------------------------
    // ✅ Test: Create Wallet
    // ------------------------------------------------------------------
    @Test
    void testCreateWallet() {
        String token = "Bearer xyz";

        CreateWalletRequest req = new CreateWalletRequest();
        req.setWalletName("Travel");

        CreateWalletResponse resp = new CreateWalletResponse();
        resp.setWalletName("Travel");

        when(userClient.getUserFromToken(token)).thenReturn(mockUser);
        when(walletManagementService.createWallet(mockUser, "Travel"))
                .thenReturn(resp);

        ResponseEntity<CreateWalletResponse> result =
                walletController.createWallet(token, req);

        assertEquals("Travel", result.getBody().getWalletName());
    }
}
