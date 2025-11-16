package com.example.wallet_service_micro.service.TransferAmount;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.transactions.WalletTransactionService;
import com.example.wallet_service_micro.service.validator.WalletValidator;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.config.properties.WalletProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTransferAmountTest {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceTransferAmountTest.class);

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletProperties walletProperties;
    @Mock private WalletMapper walletMapper;
    @Mock private WalletValidator walletValidator;
    @Mock private WalletTransactionService txnService;
    @Mock private WalletManagementService walletManagementService;
    @Mock private UserClient userClient;

    @InjectMocks private WalletService walletService;

    private UserDTO sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sender = new UserDTO();
        sender.setId(10L);
        sender.setEmail("test@example.com");
        when(walletProperties.getDailyLimit()).thenReturn(5000.0);

        log.info("✅ Test setup complete. Initialized sender with ID={} and email={}", sender.getId(), sender.getEmail());
    }

    @Test
    void testTransferAmountSuccess() {
        log.info("🚀 Starting test: testTransferAmountSuccess");

        // recipient
        UserDTO recipient = new UserDTO();
        recipient.setId(20L);
        recipient.setEmail("r@r.com");
        log.debug("Recipient initialized: id={}, email={}", recipient.getId(), recipient.getEmail());

        // wallets
        Wallet senderWallet = new Wallet(10L, "Primary");
        senderWallet.setBalance(500.0);

        Wallet receiverWallet = new Wallet(20L, "Default");
        receiverWallet.setBalance(200.0);

        // mocks
        when(userClient.getUserByIdInternal(eq(20L))).thenReturn(recipient);
        when(walletManagementService.getExistingWallet(sender, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(recipient, "Default")).thenReturn(receiverWallet);
        when(txnService.isDuplicate(anyString())).thenReturn(false);
        doNothing().when(walletValidator).validateAmount(anyDouble(), anyString());
        doNothing().when(txnService).recordTransferTransactions(any(), any(), anyDouble(), anyString(), anyString(), anyString());

        TransferResponse responseMock = new TransferResponse();
        when(walletMapper.toTransferResponse(senderWallet)).thenReturn(responseMock);

        log.info("Mock setup complete. Executing walletService.transferAmount()...");

        // run
        TransferResponse result = walletService.transferAmount(
                sender, 20L, 50.0, "TX123", "Primary", "AUTH"
        );

        // verify
        assertNotNull(result);
        verify(walletValidator).validateAmount(50.0, "Transfer");
        verify(txnService).recordTransferTransactions(sender, recipient, 50.0, "TX123", "Primary", "Default");
        verify(walletMapper).toTransferResponse(senderWallet);

        log.info("✅ Transfer successful. Amount: {}, Sender ID: {}, Receiver ID: {}",
                50.0, sender.getId(), recipient.getId());
    }

    @Test
    void testTransferRecipientNotFound() {
        log.info("🚀 Starting test: testTransferRecipientNotFound");

        when(userClient.getUserByIdInternal(anyLong())).thenReturn(null);

        log.debug("Mocked userClient to return null for any user ID.");

        assertThrows(UserNotFoundException.class, () -> {
            walletService.transferAmount(sender, 999L, 50.0, "TX999", "Primary", "AUTH");
        });

        log.info("✅ Expected exception thrown: UserNotFoundException for missing recipient.");
    }
}
