package com.example.wallet_service_micro.repository.transaction;

import com.example.wallet_service_micro.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionRepositoryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByUserId() {
        Long userId = 10L;
        Pageable pageable = PageRequest.of(0, 5);

        Page<Transaction> mockPage = new PageImpl<>(List.of(new Transaction()));

        when(transactionRepository.findByUserId(userId, pageable))
                .thenReturn(mockPage);

        Page<Transaction> result = transactionRepository.findByUserId(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(transactionRepository, times(1))
                .findByUserId(userId, pageable);
    }

    @Test
    void testFindByUserIdAndWalletId() {
        Long userId = 1L;
        Long walletId = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Transaction> mockPage = new PageImpl<>(List.of(new Transaction()));

        when(transactionRepository.findByUserIdAndWalletId(userId, walletId, pageable))
                .thenReturn(mockPage);

        Page<Transaction> result =
                transactionRepository.findByUserIdAndWalletId(userId, walletId, pageable);

        assertThat(result).isNotNull();
        verify(transactionRepository).findByUserIdAndWalletId(userId, walletId, pageable);
    }

    @Test
    void testFindByUserAndTimestampBetween() {
        Long userId = 5L;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = List.of(new Transaction());

        when(transactionRepository.findByUserAndTimestampBetween(userId, start, end))
                .thenReturn(transactions);

        List<Transaction> result =
                transactionRepository.findByUserAndTimestampBetween(userId, start, end);

        assertThat(result).hasSize(1);
        verify(transactionRepository).findByUserAndTimestampBetween(userId, start, end);
    }

    @Test
    void testFindByUserAndWalletAndTimestampBetween() {
        Long userId = 5L;
        Long walletId = 9L;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now();

        when(transactionRepository.findByUserAndWalletAndTimestampBetween(
                userId, walletId, start, end
        )).thenReturn(List.of(new Transaction()));

        List<Transaction> result =
                transactionRepository.findByUserAndWalletAndTimestampBetween(userId, walletId, start, end);

        assertThat(result).hasSize(1);
        verify(transactionRepository).findByUserAndWalletAndTimestampBetween(userId, walletId, start, end);
    }

    @Test
    void testExistsByTransactionId() {
        String transactionId = "TX1234";

        when(transactionRepository.existsByTransactionId(transactionId))
                .thenReturn(true);

        boolean exists = transactionRepository.existsByTransactionId(transactionId);

        assertThat(exists).isTrue();
        verify(transactionRepository).existsByTransactionId(transactionId);
    }

    @Test
    void testFindByUserIdAndWalletName() {
        Long userId = 1L;
        String walletName = "MainWallet";
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionRepository.findByUserIdAndWalletName(userId, walletName, pageable))
                .thenReturn(new PageImpl<>(List.of(new Transaction())));

        Page<Transaction> result =
                transactionRepository.findByUserIdAndWalletName(userId, walletName, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserIdAndWalletName(userId, walletName, pageable);
    }

    @Test
    void testFindTransactionsByUserAndWallet() {
        Long userId = 1L;
        String walletName = "Test Wallet";
        Pageable pageable = PageRequest.of(0, 20);

        when(transactionRepository.findTransactionsByUserAndWallet(userId, walletName, pageable))
                .thenReturn(new PageImpl<>(List.of(new Transaction())));

        Page<Transaction> result =
                transactionRepository.findTransactionsByUserAndWallet(userId, walletName, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findTransactionsByUserAndWallet(userId, walletName, pageable);
    }
}
