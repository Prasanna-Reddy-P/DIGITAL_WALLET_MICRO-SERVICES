package com.example.wallet_service_micro.OptimisticLockTest;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.service.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
class WalletOptimisticLockingTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    // 👇 Mock the user-service client so no remote calls happen
    @MockBean
    private UserClient userClient;

    private UserDTO sender;
    private UserDTO receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    @Transactional
    void setup() {
        walletRepository.deleteAll();

        // Simulate Sender
        sender = new UserDTO();
        sender.setId(1L);
        sender.setName("Alice");
        sender.setEmail("alice@example.com");

        senderWallet = new Wallet();
        senderWallet.setUserId(sender.getId());
        senderWallet.setWalletName("Default");
        senderWallet.setBalance(200.0);
        senderWallet.setVersion(0L);
        senderWallet = walletRepository.saveAndFlush(senderWallet);

        // Simulate Receiver
        receiver = new UserDTO();
        receiver.setId(2L);
        receiver.setName("Bob");
        receiver.setEmail("bob@example.com");


        receiverWallet = new Wallet();
        receiverWallet.setUserId(receiver.getId());
        receiverWallet.setWalletName("Default");
        receiverWallet.setBalance(50.0);
        receiverWallet.setVersion(0L);
        receiverWallet = walletRepository.saveAndFlush(receiverWallet);

        // 👇 Mock responses for userClient calls
        Mockito.when(userClient.getUserById(eq(receiver.getId()), any()))
                .thenReturn(receiver);
        Mockito.when(userClient.getUserById(eq(sender.getId()), any()))
                .thenReturn(sender);
    }

    @Test
    void testConcurrentTransfersCauseOptimisticLockException() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> task1 = () -> {
            walletService.transferAmount(
                    sender,
                    receiver.getId(),
                    40.0,
                    UUID.randomUUID().toString(),
                    "Default",
                    "Bearer dummy"
            );
            return null;
        };

        Callable<Void> task2 = () -> {
            walletService.transferAmount(
                    sender,
                    receiver.getId(),
                    30.0,
                    UUID.randomUUID().toString(),
                    "Default",
                    "Bearer dummy"
            );
            return null;
        };

        Future<Void> f1 = executor.submit(task1);
        Future<Void> f2 = executor.submit(task2);

        boolean optimisticLockDetected = false;

        try {
            f1.get();
            f2.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ObjectOptimisticLockingFailureException) {
                optimisticLockDetected = true;
                System.out.println("⚠️ OptimisticLock detected as expected.");
            } else {
                throw e;
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Wallet updatedSender = walletRepository.findById(senderWallet.getId()).orElseThrow();
        Wallet updatedReceiver = walletRepository.findById(receiverWallet.getId()).orElseThrow();

        System.out.println("✅ Sender Final Balance: " + updatedSender.getBalance());
        System.out.println("✅ Receiver Final Balance: " + updatedReceiver.getBalance());
        System.out.println("✅ Sender Version: " + updatedSender.getVersion());
        System.out.println("✅ Receiver Version: " + updatedReceiver.getVersion());

        assertThat(updatedSender.getBalance()).isLessThan(200.0);
        assertThat(updatedReceiver.getBalance()).isGreaterThan(50.0);
        assertThat(updatedSender.getVersion()).isGreaterThanOrEqualTo(1L);

        if (optimisticLockDetected) {
            System.out.println("✅ Test confirmed optimistic locking behavior.");
        }
    }
}
