package com.example.wallet_service_micro.repository.wallet;

import com.example.wallet_service_micro.model.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalletRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(WalletRepositoryTest.class);

    @Mock
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        log.info("Mocks initialized for WalletRepositoryTest");
    }

    @Test
    void testFindByUserId() {
        log.info("Running testFindByUserId...");

        Wallet w1 = new Wallet(1L, "Primary");
        Wallet w2 = new Wallet(1L, "Savings");

        log.info("Mocking findByUserId for userId=1");
        when(walletRepository.findByUserId(1L)).thenReturn(Arrays.asList(w1, w2));

        List<Wallet> wallets = walletRepository.findByUserId(1L);
        log.info("Executed findByUserId, result count={}", wallets.size());

        assertThat(wallets).hasSize(2);
        log.info("Assertion: size is 2");

        assertThat(wallets).extracting("walletName").containsExactlyInAnyOrder("Primary", "Savings");
        log.info("Assertion: correct wallet names");

        verify(walletRepository).findByUserId(1L);
        log.info("Verified findByUserId was called once");
    }

    @Test
    void testExistsByUserIdAndWalletName() {
        log.info("Running testExistsByUserIdAndWalletName...");

        when(walletRepository.existsByUserIdAndWalletName(1L, "Primary"))
                .thenReturn(true);
        log.info("Mocked existsByUserIdAndWalletName(userId=1, walletName=Primary)");

        boolean exists = walletRepository.existsByUserIdAndWalletName(1L, "Primary");
        log.info("Executed existsByUserIdAndWalletName, exists={}", exists);

        assertThat(exists).isTrue();
        log.info("Assertion passed: exists=true");

        verify(walletRepository).existsByUserIdAndWalletName(1L, "Primary");
        log.info("Verified repository call");
    }

    @Test
    void testFindByIdAndUserId() {
        log.info("Running testFindByIdAndUserId...");

        Wallet w = new Wallet(1L, "Primary");
        w.setId(100L);

        log.info("Mocking findByIdAndUserId for id=100, userId=1");
        when(walletRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(w));

        Optional<Wallet> result = walletRepository.findByIdAndUserId(100L, 1L);
        log.info("Executed findByIdAndUserId, present={}", result.isPresent());

        assertThat(result).isPresent();
        assertThat(result.get().getWalletName()).isEqualTo("Primary");
        log.info("Assertions passed");

        verify(walletRepository).findByIdAndUserId(100L, 1L);
        log.info("Verified repository call");
    }

    @Test
    void testFindByUserId_empty() {
        log.info("Running testFindByUserId_empty...");

        when(walletRepository.findByUserId(99L)).thenReturn(Collections.emptyList());
        log.info("Mocked findByUserId for userId=99 returning empty list");

        List<Wallet> wallets = walletRepository.findByUserId(99L);
        log.info("Executed findByUserId, size={}", wallets.size());

        assertThat(wallets).isEmpty();
        log.info("Assertion passed: list is empty");

        verify(walletRepository).findByUserId(99L);
        log.info("Verified method call");
    }

    @Test
    void testExistsByUserIdAndWalletName_notFound() {
        log.info("Running testExistsByUserIdAndWalletName_notFound...");

        when(walletRepository.existsByUserIdAndWalletName(1L, "NonExistent"))
                .thenReturn(false);
        log.info("Mocked non-existing walletName for userId=1");

        boolean exists = walletRepository.existsByUserIdAndWalletName(1L, "NonExistent");
        log.info("Executed method, exists={}", exists);

        assertThat(exists).isFalse();
        log.info("Assertion passed: exists=false");

        verify(walletRepository).existsByUserIdAndWalletName(1L, "NonExistent");
        log.info("Verified repository call");
    }

    @Test
    void testFindByIdAndUserId_notFound() {
        log.info("Running testFindByIdAndUserId_notFound...");

        when(walletRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());
        log.info("Mocked findByIdAndUserId to return empty");

        Optional<Wallet> result = walletRepository.findByIdAndUserId(999L, 1L);
        log.info("Executed method, present={}", result.isPresent());

        assertThat(result).isEmpty();
        log.info("Assertion passed: empty result");

        verify(walletRepository).findByIdAndUserId(999L, 1L);
        log.info("Verified repository call");
    }
}
