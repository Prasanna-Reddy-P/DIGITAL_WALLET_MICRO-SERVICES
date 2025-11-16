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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceInternalTransferTest {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceInternalTransferTest.class);

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

        log.info("Setup complete: user={}, dailyLimit={}", user.getId(), walletProperties.getDailyLimit());
    }

    @Test
    void testInternalTransferSuccess() {
        log.info("Running testInternalTransferSuccess");

        Wallet senderWallet = new Wallet(10L, "Primary");
        senderWallet.setBalance(300.0);
        Wallet receiverWallet = new Wallet(10L, "Savings");
        receiverWallet.setBalance(200.0);

        log.info("Mocking wallets: sender={}, receiver={}", senderWallet, receiverWallet);

        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);

        UserInternalTransferResponse mockedResponse = mock(UserInternalTransferResponse.class);
        when(walletMapper.toInternalTransferResponse(senderWallet)).thenReturn(mockedResponse);

        UserInternalTransferResponse result =
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 100.0, "TXN1");

        log.info("Transfer completed");

        assertNotNull(result);
        verify(walletValidator).validateAmount(100.0, "Internal Transfer");
        verify(txnService).recordTransferTransactions(user, user, 100.0, "TXN1", "Primary", "Savings");

        log.info("Assertions passed for testInternalTransferSuccess");
    }

    @Test
    void testFrozenWalletThrows() {
        log.info("Running testFrozenWalletThrows");

        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setFrozen(true);
        Wallet receiverWallet = new Wallet(user.getId(), "Savings");

        log.info("Sender wallet frozen");

        when(txnService.isDuplicate("TXN2")).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        doThrow(new IllegalArgumentException("Wallet is frozen"))
                .when(walletValidator).validateFrozen(senderWallet);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 50.0, "TXN2")
        );

        log.info("Exception correctly thrown for frozen wallet");
    }

    @Test
    void testDuplicateTransactionThrows() {
        log.info("Running testDuplicateTransactionThrows");

        when(txnService.isDuplicate("TXN_DUP")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 50.0, "TXN_DUP")
        );

        log.info("Duplicate transaction test passed");
    }

    @Test
    void testInsufficientBalanceThrows() {
        log.info("Running testInsufficientBalanceThrows");

        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setBalance(30.0);

        Wallet receiverWallet = new Wallet(user.getId(), "Savings");

        log.info("Sender balance insufficient");

        when(txnService.isDuplicate("TXN3")).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(walletValidator).validateBalance(senderWallet, 50.0);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", 50.0, "TXN3")
        );

        log.info("Insufficient balance exception correctly thrown");
    }

    @Test
    void testDailyLimitExceededFreezesWallet() {
        log.info("Running testDailyLimitExceededFreezesWallet");

        Wallet senderWallet = new Wallet(user.getId(), "Primary");
        senderWallet.setBalance(500.0);
        senderWallet.setDailySpent(walletProperties.getDailyLimit() - 10);

        Wallet receiverWallet = new Wallet(user.getId(), "Savings");
        receiverWallet.setBalance(100.0);

        log.info("Daily limit nearly reached");

        when(txnService.isDuplicate("TXN4")).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(user, "Savings")).thenReturn(receiverWallet);
        when(walletMapper.toInternalTransferResponse(senderWallet)).thenReturn(mock(UserInternalTransferResponse.class));

        walletService.transferWithinUserWallets(user, "Primary", "Savings", 20.0, "TXN4");

        assertTrue(senderWallet.getFrozen());

        log.info("Daily limit exceeded → wallet frozen");
    }

    @Test
    void testInternalTransferNegativeAmountThrows() {
        log.info("Running testInternalTransferNegativeAmountThrows");

        doThrow(new IllegalArgumentException("Amount must be positive"))
                .when(walletValidator).validateAmount(-50.0, "Internal Transfer");

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferWithinUserWallets(user, "Primary", "Savings", -50.0, "TXN_NEG")
        );

        verify(walletValidator).validateAmount(-50.0, "Internal Transfer");
        verify(walletManagementService, never()).getExistingWallet(any(), anyString());

        log.info("Negative amount exception thrown successfully");
    }

}
