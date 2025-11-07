package com.example.wallet_service_micro.repository.transaction;

import com.example.wallet_service_micro.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionRepositoryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByUserIdWithPagination() {
        Transaction t1 = new Transaction();
        t1.setTransactionId("TXN1");
        t1.setUserId(1L);
        Transaction t2 = new Transaction();
        t2.setTransactionId("TXN2");
        t2.setUserId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(Arrays.asList(t1, t2));

        when(transactionRepository.findByUserId(1L, pageable)).thenReturn(page);

        Page<Transaction> result = transactionRepository.findByUserId(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("transactionId").containsExactlyInAnyOrder("TXN1", "TXN2");
        verify(transactionRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    void testExistsByTransactionId() {
        when(transactionRepository.existsByTransactionId("TXN123")).thenReturn(true);

        boolean exists = transactionRepository.existsByTransactionId("TXN123");

        assertThat(exists).isTrue();
        verify(transactionRepository, times(1)).existsByTransactionId("TXN123");
    }

    @Test
    void testFindByUserAndTimestampBetween() {
        Transaction t1 = new Transaction();
        t1.setTransactionId("TXN1");
        t1.setUserId(1L);
        t1.setTimestamp(LocalDateTime.now().minusDays(1));

        Transaction t2 = new Transaction();
        t2.setTransactionId("TXN2");
        t2.setUserId(1L);
        t2.setTimestamp(LocalDateTime.now());

        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();

        when(transactionRepository.findByUserAndTimestampBetween(1L, start, end))
                .thenReturn(Arrays.asList(t1, t2));

        List<Transaction> transactions = transactionRepository.findByUserAndTimestampBetween(1L, start, end);

        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting("transactionId").containsExactlyInAnyOrder("TXN1", "TXN2");
        verify(transactionRepository, times(1)).findByUserAndTimestampBetween(1L, start, end);
    }

    @Test
    void testFindByUserIdWithPagination_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList());

        when(transactionRepository.findByUserId(99L, pageable)).thenReturn(emptyPage);

        Page<Transaction> result = transactionRepository.findByUserId(99L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
        verify(transactionRepository, times(1)).findByUserId(99L, pageable);
    }

    @Test
    void testExistsByTransactionId_notFound() {
        when(transactionRepository.existsByTransactionId("NON_EXISTENT")).thenReturn(false);

        boolean exists = transactionRepository.existsByTransactionId("NON_EXISTENT");

        assertThat(exists).isFalse();
        verify(transactionRepository, times(1)).existsByTransactionId("NON_EXISTENT");
    }

    @Test
    void testFindByUserAndTimestampBetween_noTransactions() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(4);

        when(transactionRepository.findByUserAndTimestampBetween(1L, start, end))
                .thenReturn(Collections.emptyList());

        List<Transaction> transactions = transactionRepository.findByUserAndTimestampBetween(1L, start, end);

        assertThat(transactions).isEmpty();
        verify(transactionRepository, times(1)).findByUserAndTimestampBetween(1L, start, end);
    }

    @Test
    void testFindByUserIdWithPagination_pageOutOfBounds() {
        Pageable pageable = PageRequest.of(10, 5); // page 10 with page size 5
        Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList());

        when(transactionRepository.findByUserId(1L, pageable)).thenReturn(emptyPage);

        Page<Transaction> result = transactionRepository.findByUserId(1L, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(transactionRepository, times(1)).findByUserId(1L, pageable);
    }

}
