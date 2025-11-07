package com.example.wallet_service_micro.service.TransferAmount;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.transactions.WalletTransactionService;
import com.example.wallet_service_micro.service.validator.WalletValidator;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.config.properties.WalletProperties;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTransferAmountTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletProperties walletProperties;
    @Mock private WalletMapper walletMapper;
    @Mock private WalletValidator walletValidator;
    @Mock private WalletTransactionService txnService;
    @Mock private WalletManagementService walletManagementService;
    @Mock private UserClient userClient;

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
    void testTransferAmountSuccess() {
        UserDTO recipient = new UserDTO();
        recipient.setId(20L);
        recipient.setEmail("r@r.com");

        Wallet senderWallet = new Wallet(10L, "Primary");
        senderWallet.setBalance(500.0);
        Wallet receiverWallet = new Wallet(20L, "Default");
        receiverWallet.setBalance(200.0);

        when(userClient.getUserById(eq(20L), anyString())).thenReturn(recipient);
        when(txnService.isDuplicate(anyString())).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(senderWallet);
        when(walletManagementService.getExistingWallet(recipient, "Default")).thenReturn(receiverWallet);

        TransferResponse mockedResp = mock(TransferResponse.class);
        when(walletMapper.toTransferResponse(senderWallet)).thenReturn(mockedResp);

        TransferResponse result = walletService.transferAmount(
                user, 20L, 50.0, "TX123", "Primary", "AUTH"
        );

        assertNotNull(result);
        verify(walletValidator).validateAmount(50.0, "Transfer");
        verify(txnService).recordTransferTransactions(user, recipient, 50.0, "TX123", "Primary", "Default");
    }

    @Test
    void testTransferRecipientNotFound() {
        when(userClient.getUserById(anyLong(), anyString())).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                walletService.transferAmount(user, 999L, 50.0, "T1", "Primary", "AUTH")
        );
    }
}

