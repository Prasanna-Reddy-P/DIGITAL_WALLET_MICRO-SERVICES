package com.example.wallet_service_micro.service.InternalTransfer;

import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.transactions.WalletTransactionService;
import com.example.wallet_service_micro.service.validator.WalletValidator;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.config.properties.WalletProperties;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceInternalTransferTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletProperties walletProperties;
    @Mock private WalletMapper walletMapper;
    @Mock private WalletValidator walletValidator;
    @Mock private WalletTransactionService txnService;
    @Mock private WalletManagementService walletManagementService;

    @InjectMocks private WalletService walletService;

    private UserDTO user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserDTO();
        user.setId(10L);
        user.setEmail("test@example.com");
        when(walletProperties.getDailyLimit()).thenReturn(5000.0);
    }

    @Test
    void testInternalTransferSuccess() {
        Wallet senderWallet = new Wallet(10L, "Primary");
        senderWallet.setBalance(300.0);
        Wallet receiverWallet = new Wallet(10L, "Savings");
        receiverWallet.setBalance(200.0);

        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);

        UserInternalTransferResponse mockedResponse = mock(UserInternalTransferResponse.class);
        when(walletMapper.toInternalTransferResponse(senderWallet)).thenReturn(mockedResponse);

        UserInternalTransferResponse result =
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 100.0, "TXN1");

        assertNotNull(result);
        verify(walletValidator).validateAmount(100.0, "Internal Transfer");
        verify(txnService).recordTransferTransactions(user, user, 100.0, "TXN1", "Primary", "Savings");
    }

    @Test
    void testFrozenWalletThrows() {
        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setFrozen(true);
        Wallet receiverWallet = new Wallet(user.getId(), "Savings");
        receiverWallet.setBalance(100.0);

        when(txnService.isDuplicate("TXN2")).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        doThrow(new IllegalArgumentException("Wallet is frozen"))
                .when(walletValidator).validateFrozen(senderWallet);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 50.0, "TXN2")
        );
    }

    @Test
    void testDuplicateTransactionThrows() {
        when(txnService.isDuplicate("TXN_DUP")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 50.0, "TXN_DUP")
        );
    }

    @Test
    void testInsufficientBalanceThrows() {
        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setBalance(30.0);
        Wallet receiverWallet = new Wallet(user.getId(), "Savings");
        receiverWallet.setBalance(100.0);

        when(txnService.isDuplicate("TXN3")).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(walletValidator).validateBalance(senderWallet, 50.0);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 50.0, "TXN3")
        );
    }

    @Test
    void testDailyLimitExceededFreezesWallet() {
        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setBalance(500.0);
        senderWallet.setDailySpent(walletProperties.getDailyLimit() - 10);
        Wallet receiverWallet = new Wallet(user.getId(), "Savings");
        receiverWallet.setBalance(100.0);

        when(txnService.isDuplicate("TXN4")).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        when(walletMapper.toInternalTransferResponse(senderWallet))
                .thenReturn(mock(UserInternalTransferResponse.class));

        walletService.transferWithinUserWallets(user, "Primary", "Savings", 20.0, "TXN4");

        assertTrue(senderWallet.getFrozen());
    }

    @Test
    void testInternalTransferNegativeAmountThrows() {
        doThrow(new IllegalArgumentException("Amount must be positive"))
                .when(walletValidator).validateAmount(-50.0, "Internal Transfer");

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", -50.0, "TXN_NEG")
        );

        verify(walletValidator).validateAmount(-50.0, "Internal Transfer");
        verify(walletManagementService, never()).getExistingWallet(any(), anyString());
    }

    @Test
    void testInternalTransferDailyLimitExceededThrows() {
        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setBalance(500.0);
        senderWallet.setDailySpent(walletProperties.getDailyLimit() - 10);
        Wallet receiverWallet = new Wallet(user.getId(), "Savings");
        receiverWallet.setBalance(100.0);

        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        when(walletMapper.toInternalTransferResponse(senderWallet)).thenReturn(new UserInternalTransferResponse());

        UserInternalTransferResponse response =
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 20.0, "TXN_DL");

        assertNotNull(response);
        assertTrue(senderWallet.getFrozen());
        assertEquals(500.0 - 20.0, senderWallet.getBalance());
        assertEquals(100.0 + 20.0, receiverWallet.getBalance());
    }
}
