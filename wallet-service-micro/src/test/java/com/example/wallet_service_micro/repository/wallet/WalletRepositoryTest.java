package com.example.wallet_service_micro.repository.wallet;


import com.example.wallet_service_micro.model.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalletRepositoryTest {

    @Mock
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByUserId() {
        Wallet w1 = new Wallet(1L, "Primary");
        Wallet w2 = new Wallet(1L, "Savings");

        when(walletRepository.findByUserId(1L)).thenReturn(Arrays.asList(w1, w2));

        List<Wallet> wallets = walletRepository.findByUserId(1L);

        assertThat(wallets).hasSize(2);
        assertThat(wallets).extracting("walletName").containsExactlyInAnyOrder("Primary", "Savings");
        verify(walletRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testExistsByUserIdAndWalletName() {
        when(walletRepository.existsByUserIdAndWalletName(1L, "Primary")).thenReturn(true);

        boolean exists = walletRepository.existsByUserIdAndWalletName(1L, "Primary");

        assertThat(exists).isTrue();
        verify(walletRepository, times(1)).existsByUserIdAndWalletName(1L, "Primary");
    }

    @Test
    void testFindByIdAndUserId() {
        Wallet w = new Wallet(1L, "Primary");
        w.setId(100L);

        when(walletRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(w));

        Optional<Wallet> result = walletRepository.findByIdAndUserId(100L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getWalletName()).isEqualTo("Primary");
        verify(walletRepository, times(1)).findByIdAndUserId(100L, 1L);
    }

    @Test
    void testFindByUserId_empty() {
        when(walletRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        List<Wallet> wallets = walletRepository.findByUserId(99L);

        assertThat(wallets).isEmpty();
        verify(walletRepository, times(1)).findByUserId(99L);
    }

    @Test
    void testExistsByUserIdAndWalletName_notFound() {
        when(walletRepository.existsByUserIdAndWalletName(1L, "NonExistent")).thenReturn(false);

        boolean exists = walletRepository.existsByUserIdAndWalletName(1L, "NonExistent");

        assertThat(exists).isFalse();
        verify(walletRepository, times(1)).existsByUserIdAndWalletName(1L, "NonExistent");
    }

    @Test
    void testFindByIdAndUserId_notFound() {
        when(walletRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        Optional<Wallet> result = walletRepository.findByIdAndUserId(999L, 1L);

        assertThat(result).isEmpty();
        verify(walletRepository, times(1)).findByIdAndUserId(999L, 1L);
    }

}
