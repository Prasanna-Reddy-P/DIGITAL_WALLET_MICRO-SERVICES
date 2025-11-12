package com.example.wallet_service_micro.rollBack;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.service.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WalletLoadMoneyTransactionTest {

    private static final Logger log = LoggerFactory.getLogger(WalletLoadMoneyTransactionTest.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @MockBean
    private UserClient userClient;

    private UserDTO user;
    private Wallet wallet;

    @BeforeEach
    void setup() {
        log.info("🧹 Cleaning up test database...");
        walletRepository.deleteAll();

        user = new UserDTO();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");

        wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setWalletName("Default");
        wallet.setBalance(100.0);
        wallet.setVersion(0L);

        // ✅ assign persisted entity back to variable
        wallet = walletRepository.saveAndFlush(wallet);

        log.info("✅ Test wallet created: ID={}, Balance={}", wallet.getId(), wallet.getBalance());

        Mockito.when(userClient.getUserById(Mockito.eq(user.getId()), Mockito.any()))
                .thenReturn(user);
    }

    @Test
    void testSuccessfulLoadMoney() {
        log.info("▶️ Starting successful load money transaction...");

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(50.0);

        walletService.loadMoney(user, req, UUID.randomUUID().toString(), "Default");

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();

        log.info("✅ Transaction committed successfully.");
        log.info("💰 Final Balance after load: {}", updated.getBalance());

        assertThat(updated.getBalance()).isEqualTo(150.0);
    }

    @Test
    void testRollbackOnFailure() {
        log.info("▶️ Starting rollback scenario (invalid load money request)...");

        LoadMoneyRequest req = new LoadMoneyRequest();
        req.setAmount(-500.0); // invalid amount triggers rollback

        assertThrows(IllegalArgumentException.class, () ->
                walletService.loadMoney(user, req, UUID.randomUUID().toString(), "Default"));

        Wallet after = walletRepository.findById(wallet.getId()).orElseThrow();

        log.warn("⚠️ Transaction rolled back due to invalid amount.");
        log.info("💰 Balance after rollback: {}", after.getBalance());

        assertThat(after.getBalance()).isEqualTo(100.0);
    }
}
