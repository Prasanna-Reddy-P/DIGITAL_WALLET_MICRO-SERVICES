package com.example.wallet_service_micro.service.factory;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletManagementService {

    private final WalletRepository walletRepository;

    public WalletManagementService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // ✅ Create wallet (explicit)
    @Transactional
    public CreateWalletResponse createWallet(UserDTO user, String walletName) {

        if (walletRepository.existsByUserIdAndWalletName(user.getId(), walletName)) {
            throw new IllegalArgumentException("Wallet already exists: " + walletName);
        }

        Wallet wallet = new Wallet(user.getId(), walletName);
        walletRepository.save(wallet);

        CreateWalletResponse response = new CreateWalletResponse();
        response.setWalletName(walletName);
        response.setBalance(wallet.getBalance());
        response.setMessage("Wallet created successfully ✅");

        return response;
    }

    // ✅ Fetch wallet ONLY if it exists
    public Wallet getExistingWallet(UserDTO user, String walletName) {

        return walletRepository.findByUserIdAndWalletName(user.getId(), walletName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet '" + walletName + "' does not exist. Create it first."
                ));
    }
}
