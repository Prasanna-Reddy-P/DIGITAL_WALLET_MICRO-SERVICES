package com.example.wallet_service_micro.service.factory;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletManagementService {

    private static final Logger logger = LoggerFactory.getLogger(WalletManagementService.class);

    private final WalletRepository walletRepository;

    public WalletManagementService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // ✅ Create wallet (explicit)
    @Transactional
    public CreateWalletResponse createWallet(UserDTO user, String walletName) {
        logger.info("🆕 Request to create wallet | userId={} | walletName='{}'", user.getId(), walletName);

        walletName = walletName.trim();

        Wallet existing = walletRepository.findByUserIdAndWalletName(user.getId(), walletName)
                .orElse(null);

        if (existing != null) {
            logger.warn("⚠️ Wallet '{}' already exists for userId={}", walletName, user.getId());
            throw new IllegalArgumentException("Wallet already exists: " + walletName);
        }

        Wallet wallet = new Wallet(user.getId(), walletName);
        walletRepository.save(wallet);
        logger.info("✅ Wallet '{}' created successfully for userId={}", walletName, user.getId());

        CreateWalletResponse response = new CreateWalletResponse();
        response.setWalletName(walletName);
        response.setBalance(wallet.getBalance());
        response.setMessage("Wallet created successfully ✅");

        logger.debug("📦 Wallet creation response: {}", response);
        return response;
    }

    // ✅ Fetch wallet ONLY if it exists
    public Wallet getExistingWallet(UserDTO user, String walletName) {
        logger.debug("🔍 Fetching existing wallet | userId={} | walletName='{}'", user.getId(), walletName);

        Wallet wallet = walletRepository.findByUserIdAndWalletName(user.getId(), walletName)
                .orElseThrow(() -> {
                    logger.error("❌ Wallet '{}' does not exist for userId={}", walletName.trim(), user.getId());
                    return new IllegalArgumentException(
                            "Wallet '" + walletName.trim() + "' does not exist. Create it first."
                    );
                });

        logger.info("✅ Wallet '{}' found for userId={}", walletName, user.getId());
        return wallet;
    }
}
