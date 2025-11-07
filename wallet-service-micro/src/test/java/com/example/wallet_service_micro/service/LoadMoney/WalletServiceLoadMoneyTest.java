package com.example.wallet_service_micro.service.LoadMoney;

import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import com.example.wallet_service_micro.service.transactions.WalletTransactionService;
import com.example.wallet_service_micro.service.validator.WalletValidator;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.config.properties.WalletProperties;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceLoadMoneyTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletProperties walletProperties;
    @Mock private WalletMapper walletMapper;
    @Mock private WalletValidator walletValidator;
    @Mock private WalletTransactionService txnService;
    @Mock private WalletManagementService walletManagementService;

    @InjectMocks private WalletService walletService;

    private UserDTO user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserDTO();
        user.setId(10L);
        user.setEmail("test@example.com");

        wallet = new Wallet(10L, "Primary");
        wallet.setBalance(500.0);
        wallet.setDailySpent(0.0);

        when(walletProperties.getDailyLimit()).thenReturn(5000.0);
    }

    @Test
    void testLoadMoneySuccess() {
        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(100.0);
        req.setWalletName("Primary");
        String txnId = "TX123";

        when(txnService.isDuplicate(txnId)).thenReturn(false);
        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(wallet);
        LoadMoneyResponse mockedResponse = mock(LoadMoneyResponse.class);
        when(walletMapper.toLoadMoneyResponse(wallet)).thenReturn(mockedResponse);

        LoadMoneyResponse result = walletService.loadMoney(user, req, txnId, "Primary");

        assertNotNull(result);
        verify(walletValidator).validateAmount(100.0, "Load");
        verify(txnService).recordLoadTransaction(user, 100.0, txnId, "Primary");
    }

    @Test
    void testLoadMoneyThrowsUserNotFound() {
        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(100.0);

        assertThrows(UserNotFoundException.class, () ->
                walletService.loadMoney(null, req, "TXN", "Primary")
        );
    }

    @Test
    void testLoadMoneyNegativeAmountThrows() {
        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(-50.0);

        doThrow(new IllegalArgumentException("Amount must be positive"))
                .when(walletValidator).validateAmount(-50.0, "Load");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletService.loadMoney(user, req, "TXN_NEG", "Primary")
        );

        assertEquals("Amount must be positive", ex.getMessage());
        verify(walletValidator).validateAmount(-50.0, "Load");
        verify(walletManagementService, never()).getExistingWallet(any(), anyString());
    }

    @Test
    void testLoadMoneyDailyLimitExceededThrows() {
        wallet.setDailySpent(walletProperties.getDailyLimit() - 100);
        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(200.0);

        when(walletManagementService.getExistingWallet(user, "Primary")).thenReturn(wallet);
        doThrow(new IllegalArgumentException("Daily limit exceeded"))
                .when(walletValidator).validateDailyLimit(wallet, 200.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletService.loadMoney(user, req, "TXN_LIMIT", "Primary")
        );

        assertEquals("Daily limit exceeded", ex.getMessage());
        verify(walletValidator).validateDailyLimit(wallet, 200.0);
        verify(walletRepository, never()).saveAndFlush(any());
    }
}
