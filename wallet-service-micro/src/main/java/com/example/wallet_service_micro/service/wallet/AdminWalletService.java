package com.example.wallet_service_micro.service.wallet;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminWalletService {

    private static final Logger log = LoggerFactory.getLogger(AdminWalletService.class);

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final UserClient userClient;

    // ---------- Constructor (Manual — NO Lombok) ----------
    public AdminWalletService(
            WalletRepository walletRepository,
            WalletMapper walletMapper,
            UserClient userClient
    ) {
        this.walletRepository = walletRepository;
        this.walletMapper = walletMapper;
        this.userClient = userClient;
    }

    // ---------- Business Logic ----------

    public List<WalletBalanceResponse> getAllWalletsByUserId(Long userId) {
        log.info("📋 Admin fetching wallets for userId={}", userId);

        List<Wallet> wallets = walletRepository.findByUserId(userId);

        return wallets.stream()
                .map(wallet -> {
                    WalletBalanceResponse dto = walletMapper.toBalanceResponse(wallet);
                    dto.setMessage("Wallet: " + wallet.getWalletName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public WalletBalanceResponse getWalletByUserIdAndWalletName(Long userId, String walletName) {
        log.info("🔍 Admin fetching wallet | userId={} | walletName={}", userId, walletName);

        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet '" + walletName + "' not found for userId=" + userId
                ));

        WalletBalanceResponse response = walletMapper.toBalanceResponse(wallet);
        response.setMessage("Wallet fetched successfully ✅");

        return response;
    }

    @Transactional
    public void blacklistWalletByName(Long userId, String walletName, String authHeader) {
        log.warn("🚫 Blacklisting wallet | userId={} | walletName={}", userId, walletName);

        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet '" + walletName + "' not found for userId=" + userId
                ));

        wallet.setBlacklisted(Boolean.TRUE);
        walletRepository.save(wallet);

        boolean allBlacklisted = walletRepository.findByUserId(userId)
                .stream()
                .allMatch(w -> Boolean.TRUE.equals(w.getBlacklisted()));

        if (allBlacklisted) {
            userClient.blacklistUser(userId, authHeader);
            log.info("✅ All wallets for user {} are BLACKLISTED → User also BLACKLISTED", userId);
        } else {
            log.info("⚠️ Wallet '{}' blacklisted, but other wallets still active", walletName);
        }
    }

    public boolean areAllWalletsBlacklisted(Long userId) {
        boolean result = walletRepository.findByUserId(userId)
                .stream()
                .allMatch(w -> Boolean.TRUE.equals(w.getBlacklisted()));

        log.debug("🧩 Check all wallets blacklisted | userId={} | result={}", userId, result);
        return result;
    }

    @Transactional
    public int unblacklistAllWallets(Long userId, String authHeader) {
        log.info("♻️ Unblocking all wallets | userId={}", userId);

        List<Wallet> wallets = walletRepository.findByUserId(userId);

        if (wallets.isEmpty()) {
            log.warn("⚠️ No wallets found for userId={}", userId);
            return 0;
        }

        wallets.forEach(wallet -> wallet.setBlacklisted(Boolean.FALSE));
        walletRepository.saveAll(wallets);

        userClient.unblacklistUser(userId, authHeader);
        log.info("✅ All wallets unblocked for userId={}", userId);

        return wallets.size();
    }
}
