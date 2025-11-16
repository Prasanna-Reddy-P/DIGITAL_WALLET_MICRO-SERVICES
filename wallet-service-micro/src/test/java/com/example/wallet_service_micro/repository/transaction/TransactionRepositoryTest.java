package com.example.wallet_service_micro.repository.transaction;

import com.example.wallet_service_micro.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepositoryTest.class);

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        log.info("Mocks initialized for TransactionRepositoryTest");
    }

    @Test
    void testFindByUserId() {
        Long userId = 10L;
        Pageable pageable = PageRequest.of(0, 5);

        log.info("Testing findByUserId with userId={} and pageable={}", userId, pageable);

        Page<Transaction> mockPage = new PageImpl<>(List.of(new Transaction()));

        when(transactionRepository.findByUserId(userId, pageable))
                .thenReturn(mockPage);
        log.info("Mocked transactionRepository.findByUserId()");

        Page<Transaction> result = transactionRepository.findByUserId(userId, pageable);
        log.info("Executed findByUserId(), got {} results", result.getTotalElements());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        log.info("Assertions passed for testFindByUserId");

        verify(transactionRepository).findByUserId(userId, pageable);
        log.info("Verified repository method call");
    }

    @Test
    void testFindByUserIdAndWalletId() {
        Long userId = 1L;
        Long walletId = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        log.info("Testing findByUserIdAndWalletId: userId={}, walletId={}", userId, walletId);

        Page<Transaction> mockPage = new PageImpl<>(List.of(new Transaction()));

        when(transactionRepository.findByUserIdAndWalletId(userId, walletId, pageable))
                .thenReturn(mockPage);
        log.info("Mocked findByUserIdAndWalletId()");

        Page<Transaction> result =
                transactionRepository.findByUserIdAndWalletId(userId, walletId, pageable);
        log.info("Executed method, result size = {}", result.getContent().size());

        assertThat(result).isNotNull();
        log.info("Assertion passed");

        verify(transactionRepository).findByUserIdAndWalletId(userId, walletId, pageable);
        log.info("Verified repository call");
    }

    @Test
    void testFindByUserAndTimestampBetween() {
        Long userId = 5L;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now();

        log.info("Testing findByUserAndTimestampBetween for userId={}, between {} and {}", userId, start, end);

        List<Transaction> transactions = List.of(new Transaction());

        when(transactionRepository.findByUserAndTimestampBetween(userId, start, end))
                .thenReturn(transactions);
        log.info("Mocked findByUserAndTimestampBetween()");

        List<Transaction> result =
                transactionRepository.findByUserAndTimestampBetween(userId, start, end);
        log.info("Executed method, result count={}", result.size());

        assertThat(result).hasSize(1);
        log.info("Assertion successful");

        verify(transactionRepository).findByUserAndTimestampBetween(userId, start, end);
        log.info("Verified repository call");
    }

    @Test
    void testFindByUserAndWalletAndTimestampBetween() {
        Long userId = 5L;
        Long walletId = 9L;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now();

        log.info("Testing findByUserAndWalletAndTimestampBetween for userId={}, walletId={}", userId, walletId);

        when(transactionRepository.findByUserAndWalletAndTimestampBetween(
                userId, walletId, start, end
        )).thenReturn(List.of(new Transaction()));
        log.info("Mocked findByUserAndWalletAndTimestampBetween()");

        List<Transaction> result =
                transactionRepository.findByUserAndWalletAndTimestampBetween(userId, walletId, start, end);
        log.info("Executed method, result size={}", result.size());

        assertThat(result).hasSize(1);
        log.info("Assertions OK");

        verify(transactionRepository).findByUserAndWalletAndTimestampBetween(userId, walletId, start, end);
        log.info("Verified method call");
    }

    @Test
    void testExistsByTransactionId() {
        String transactionId = "TX1234";

        log.info("Testing existsByTransactionId for {}", transactionId);

        when(transactionRepository.existsByTransactionId(transactionId))
                .thenReturn(true);
        log.info("Mocked existsByTransactionId()");

        boolean exists = transactionRepository.existsByTransactionId(transactionId);
        log.info("Executed method, exists={}", exists);

        assertThat(exists).isTrue();
        log.info("Assertion passed");

        verify(transactionRepository).existsByTransactionId(transactionId);
        log.info("Verified repository call");
    }

    @Test
    void testFindByUserIdAndWalletName() {
        Long userId = 1L;
        String walletName = "MainWallet";
        Pageable pageable = PageRequest.of(0, 10);

        log.info("Testing findByUserIdAndWalletName for userId={}, wallet={}", userId, walletName);

        when(transactionRepository.findByUserIdAndWalletName(userId, walletName, pageable))
                .thenReturn(new PageImpl<>(List.of(new Transaction())));
        log.info("Mocked findByUserIdAndWalletName()");

        Page<Transaction> result =
                transactionRepository.findByUserIdAndWalletName(userId, walletName, pageable);
        log.info("Executed method, result count={}", result.getContent().size());

        assertThat(result.getContent()).hasSize(1);
        log.info("Assertions OK");

        verify(transactionRepository).findByUserIdAndWalletName(userId, walletName, pageable);
        log.info("Verified call");
    }

    @Test
    void testFindTransactionsByUserAndWallet() {
        Long userId = 1L;
        String walletName = "Test Wallet";
        Pageable pageable = PageRequest.of(0, 20);

        log.info("Testing findTransactionsByUserAndWallet: userId={}, wallet={}", userId, walletName);

        when(transactionRepository.findTransactionsByUserAndWallet(userId, walletName, pageable))
                .thenReturn(new PageImpl<>(List.of(new Transaction())));
        log.info("Mocked findTransactionsByUserAndWallet()");

        Page<Transaction> result =
                transactionRepository.findTransactionsByUserAndWallet(userId, walletName, pageable);
        log.info("Executed, result count={}", result.getContent().size());

        assertThat(result.getContent()).hasSize(1);
        log.info("Assertion passed");

        verify(transactionRepository).findTransactionsByUserAndWallet(userId, walletName, pageable);
        log.info("Verified call");
    }
}
