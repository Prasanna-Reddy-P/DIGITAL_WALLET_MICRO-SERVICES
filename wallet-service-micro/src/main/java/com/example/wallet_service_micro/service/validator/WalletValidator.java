package com.example.wallet_service_micro.service.validator;

import com.example.wallet_service_micro.config.properties.WalletProperties;
import com.example.wallet_service_micro.model.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WalletValidator {

    private static final Logger logger = LoggerFactory.getLogger(WalletValidator.class);

    private final WalletProperties walletProperties;

    public WalletValidator(WalletProperties walletProperties) {
        this.walletProperties = walletProperties;
    }

    public void validateAmount(double amount, String operation) {
        logger.debug("Validating amount: {} for operation: {}", amount, operation);

        if (amount <= 0) {
            logger.warn("❌ Invalid amount: {} (must be > 0) for operation: {}", amount, operation);
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if (amount < walletProperties.getMinAmount() || amount > walletProperties.getMaxAmount()) {
            logger.warn("❌ {} amount {} out of range ({} - {})",
                    operation, amount, walletProperties.getMinAmount(), walletProperties.getMaxAmount());
            throw new IllegalArgumentException(operation + " amount must be between "
                    + walletProperties.getMinAmount() + " and " + walletProperties.getMaxAmount());
        }

        logger.info("✅ Amount validation passed for operation: {}", operation);
    }

    public void validateDailyLimit(Wallet wallet, double amount) {
        double remaining = walletProperties.getDailyLimit() - wallet.getDailySpent();
        logger.debug("Checking daily limit for wallet: {} | spent: {} | remaining: {} | requested: {}",
                wallet.getWalletName(), wallet.getDailySpent(), remaining, amount);

        if (amount > remaining) {
            logger.warn("❌ Daily limit exceeded for wallet: {} | attempted: {} | remaining: {}",
                    wallet.getWalletName(), amount, remaining);
            throw new IllegalArgumentException("Daily limit exceeded for wallet, may be the wallet is frozen or you are performing a transaction where amount is greater than 30,000");
        }

        logger.info("✅ Daily limit validation passed for wallet: {}", wallet.getWalletName());
    }

    public void validateFrozen(Wallet wallet) {
        logger.debug("Checking frozen status for wallet: {}", wallet.getWalletName());

        if (wallet.getFrozen()) {
            logger.error("❌ Wallet '{}' is frozen. Cannot proceed.", wallet.getWalletName());
            throw new IllegalArgumentException("Wallet frozen. Cannot proceed.");
        }

        logger.info("✅ Wallet '{}' is active (not frozen)", wallet.getWalletName());
    }

    public void validateBalance(Wallet wallet, double amount) {
        logger.debug("Validating balance for wallet: {} | balance: {} | required: {}",
                wallet.getWalletName(), wallet.getBalance(), amount);

        if (wallet.getBalance() < amount) {
            logger.warn("❌ Insufficient balance in wallet: {} | balance: {} | required: {}",
                    wallet.getWalletName(), wallet.getBalance(), amount);
            throw new IllegalArgumentException("Insufficient balance");
        }

        logger.info("✅ Wallet '{}' has sufficient balance", wallet.getWalletName());
    }

    public void validateNotBlacklisted(Wallet wallet) {
        logger.debug("Checking blacklist status for wallet: {}", wallet.getWalletName());

        if (Boolean.TRUE.equals(wallet.getBlacklisted())) {
            logger.error("❌ Wallet '{}' is blacklisted. Transaction denied.", wallet.getWalletName());
            throw new IllegalStateException(
                    "Wallet '" + wallet.getWalletName() + "' is blacklisted and cannot perform transactions."
            );
        }

        logger.info("✅ Wallet '{}' is not blacklisted", wallet.getWalletName());
    }
}
