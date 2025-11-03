package com.example.wallet_service_micro.service.wallet;

import com.example.wallet_service_micro.dto.UserDTO;
import com.example.wallet_service_micro.model.Wallet;
import com.example.wallet_service_micro.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class WalletFactory {

    private static final Logger log = LoggerFactory.getLogger(WalletFactory.class);
    private final WalletRepository walletRepository;

    public WalletFactory(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet getOrCreateWallet(UserDTO user) {
        return walletRepository.findByUserId(user.getId()).orElseGet(() -> {
            log.info("🪙 Creating wallet for new user {}", user.getEmail());
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(0.0);
            wallet.setDailySpent(0.0);
            wallet.setFrozen(false);
            wallet.setLastTransactionDate(LocalDate.now());
            return walletRepository.save(wallet);
        });
    }
}
