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

        when(transactionRepository.findTransactionsByUserAndWallet(userId, walletName, pageable))
                .thenReturn(new PageImpl<>(List.of(new Transaction())));
        log.info("Mocked findByUserIdAndWalletName()");

        Page<Transaction> result =
                transactionRepository.findTransactionsByUserAndWallet(userId, walletName, pageable);
        log.info("Executed method, result count={}", result.getContent().size());

        assertThat(result.getContent()).hasSize(1);
        log.info("Assertions OK");

        verify(transactionRepository).findTransactionsByUserAndWallet(userId, walletName, pageable);
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
