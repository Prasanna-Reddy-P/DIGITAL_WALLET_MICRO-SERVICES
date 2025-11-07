package com.example.wallet_service_micro.service.retrieval;

import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.mapper.transaction.TransactionMapper;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.dto.user.UserDTO;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceWalletRetrievalTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletMapper walletMapper;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private WalletService walletService;

    private UserDTO user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserDTO();
        user.setId(10L);
        wallet = new Wallet(10L, "Primary");
        wallet.setBalance(500.0);
    }

    @Test
    void testGetWalletByUserIdAndWalletName() {
        when(walletRepository.findByUserIdAndWalletName(10L, "Primary"))
                .thenReturn(Optional.of(wallet));

        WalletBalanceResponse mocked = mock(WalletBalanceResponse.class);
        when(walletMapper.toBalanceResponse(wallet)).thenReturn(mocked);

        WalletBalanceResponse result = walletService.getWalletByUserIdAndWalletName(10L, "Primary");

        assertEquals(mocked, result);
    }

    @Test
    void testGetTransactionsByWallet() {
        TransactionDTO dto = new TransactionDTO();
        Page<Transaction> txPage = new PageImpl<>(List.of(new Transaction()));
        when(transactionRepository.findTransactionsByUserAndWallet(eq(10L), eq("Primary"), any(Pageable.class)))
                .thenReturn(txPage);
        when(transactionMapper.toDTO(any())).thenReturn(dto);

        Page<TransactionDTO> result = walletService.getTransactionsByWallet(user, "Primary", 0, 10);
        assertNotNull(result);
    }
}
