package com.example.wallet_service_micro.repository.wallet;

import com.example.wallet_service_micro.model.wallet.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // ✅ Return all wallets for a user
    List<Wallet> findByUserId(Long userId);

    // ✅ Optional helper to fetch wallet by userId + walletName
    Optional<Wallet> findByUserIdAndWalletName(Long userId, String walletName);

    // ✅ Fetch wallet by walletId + userId
    Optional<Wallet> findByIdAndUserId(Long id, Long userId);

    // ✅ Check if wallet exists for user
    boolean existsByUserIdAndWalletName(Long userId, String walletName);

}
