package com.example.wallet_service_micro.rollBack;

import com.example.wallet_service_micro.client.user.UserClient;
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
class WalletTransferTransactionTest {

    private static final Logger log = LoggerFactory.getLogger(WalletTransferTransactionTest.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @MockBean
    private UserClient userClient;

    private UserDTO sender;
    private UserDTO receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setup() {
        log.info("🧹 Cleaning up test database...");
        walletRepository.deleteAll();

        sender = new UserDTO();
        sender.setId(1L);
        sender.setName("Alice");
        sender.setEmail("alice@example.com");


        receiver = new UserDTO();
        receiver.setId(2L);
        receiver.setName("Bob");
        receiver.setEmail("bob@example.com");

        senderWallet = new Wallet();
        senderWallet.setUserId(sender.getId());
        senderWallet.setWalletName("Main");
        senderWallet.setBalance(100.0);
        senderWallet = walletRepository.saveAndFlush(senderWallet);

        receiverWallet = new Wallet();
        receiverWallet.setUserId(receiver.getId());
        receiverWallet.setWalletName("Default");
        receiverWallet.setBalance(50.0);
        receiverWallet = walletRepository.saveAndFlush(receiverWallet);

        Mockito.when(userClient.getUserById(Mockito.eq(sender.getId()), Mockito.any()))
                .thenReturn(sender);
        Mockito.when(userClient.getUserById(Mockito.eq(receiver.getId()), Mockito.any()))
                .thenReturn(receiver);

        log.info("✅ Wallets initialized: sender={}, receiver={}", senderWallet.getBalance(), receiverWallet.getBalance());
    }

    @Test
    void testSuccessfulTransfer() {
        log.info("▶️ Starting successful transfer transaction...");

        walletService.transferAmount(sender, receiver.getId(), 40.0,
                UUID.randomUUID().toString(), "Main", "Bearer dummy");

        Wallet updatedSender = walletRepository.findById(senderWallet.getId()).orElseThrow();
        Wallet updatedReceiver = walletRepository.findById(receiverWallet.getId()).orElseThrow();

        log.info("✅ Transaction committed successfully.");
        log.info("💰 Sender Balance: {}, Receiver Balance: {}", updatedSender.getBalance(), updatedReceiver.getBalance());

        assertThat(updatedSender.getBalance()).isEqualTo(60.0);
        assertThat(updatedReceiver.getBalance()).isEqualTo(90.0);
    }

    @Test
    void testRollbackOnInsufficientBalance() {
        log.info("▶️ Starting rollback scenario for insufficient funds...");

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferAmount(sender, receiver.getId(), 500.0,
                        UUID.randomUUID().toString(), "Main", "Bearer dummy"));

        Wallet afterSender = walletRepository.findById(senderWallet.getId()).orElseThrow();
        Wallet afterReceiver = walletRepository.findById(receiverWallet.getId()).orElseThrow();

        log.warn("⚠️ Transaction rolled back due to insufficient balance.");
        log.info("💰 Sender Balance After Rollback: {}, Receiver Balance After Rollback: {}",
                afterSender.getBalance(), afterReceiver.getBalance());

        assertThat(afterSender.getBalance()).isEqualTo(100.0);
        assertThat(afterReceiver.getBalance()).isEqualTo(50.0);
    }
}

