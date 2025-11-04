package com.example.wallet_service_micro.repository.wallet;

import com.example.wallet_service_micro.model.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // find wallet by userId (instead of User object)
    Optional<Wallet> findByUserId(Long userId);
}
