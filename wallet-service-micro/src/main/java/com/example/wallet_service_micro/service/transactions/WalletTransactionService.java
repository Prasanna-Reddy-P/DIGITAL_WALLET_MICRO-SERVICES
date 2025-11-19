package com.example.wallet_service_micro.service.transactions;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionService.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public WalletTransactionService(TransactionRepository transactionRepository,
                                    WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    // --------------------------------------------------------------------
    // HELPER: GET WALLET
    // --------------------------------------------------------------------
    private Wallet getWallet(Long userId, String walletName) {
        return walletRepository
                .findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> {
                    logger.error("❌ Wallet '{}' not found for userId={}", walletName, userId);
                    return new RuntimeException("Wallet not found");
                });
    }

    // --------------------------------------------------------------------
    // HELPER: CREATE TRANSACTION OBJECT
    // --------------------------------------------------------------------
    private Transaction createTransaction(
            UserDTO user,
            double amount,
            String type,
            String txnId,
            Wallet wallet,
            String senderWallet,
            String receiverWallet) {

        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setUserEmail(user.getEmail());
        tx.setAmount(amount);
        tx.setType(type);
        tx.setTransactionId(txnId);
        tx.setTimestamp(LocalDateTime.now());

        tx.setWalletId(wallet.getId());
        tx.setWalletName(wallet.getWalletName());

        tx.setSenderWalletName(senderWallet);
        tx.setReceiverWalletName(receiverWallet);

        return tx;
    }

    // --------------------------------------------------------------------
    // DUPLICATE CHECK
    // --------------------------------------------------------------------
    public boolean isDuplicate(String transactionId) {
        boolean exists = transactionRepository.existsByTransactionId(transactionId);
        if (exists) {
            logger.warn("⚠️ Duplicate transaction detected: {}", transactionId);
        }
        return exists;
    }

    // --------------------------------------------------------------------
    // RECORD LOAD / SELF-CREDITED
    // --------------------------------------------------------------------
    public void recordLoadTransaction(UserDTO user, double amount, String txnId, String walletName) {

        logger.info("💰 Recording LOAD | userId={} | wallet={} | txnId={} | amount={}",
                user.getId(), walletName, txnId, amount);

        Wallet wallet = getWallet(user.getId(), walletName);

        Transaction tx = createTransaction(
                user,
                amount,
                "SELF_CREDITED",
                txnId,
                wallet,
                null,
                walletName
        );

        transactionRepository.save(tx);
        logger.info("✅ Load transaction saved | txnId={}", txnId);
    }

    // --------------------------------------------------------------------
    // RECORD EXTERNAL TRANSFER
    // --------------------------------------------------------------------
    public void recordTransferTransactions(
            UserDTO sender,
            UserDTO recipient,
            double amount,
            String txnId,
            String senderWalletName,
            String receiverWalletName) {

        logger.info("💸 Recording TRANSFER | txnId={} | senderId={} | receiverId={} | amount={}",
                txnId, sender.getId(), recipient.getId(), amount);

        Wallet senderWallet = getWallet(sender.getId(), senderWalletName);
        Wallet receiverWallet = getWallet(recipient.getId(), receiverWalletName);

        // Debit (sender)
        Transaction debit = createTransaction(
                sender, amount, "DEBIT", txnId,
                senderWallet, senderWalletName, receiverWalletName
        );

        // Credit (recipient)
        Transaction credit = createTransaction(
                recipient, amount, "CREDIT", txnId,
                receiverWallet, senderWalletName, receiverWalletName
        );

        transactionRepository.save(debit);
        transactionRepository.save(credit);

        logger.info("✅ Transfer recorded successfully | txnId={}", txnId);
    }

    /*
    public void recordInternalTransfer(
            UserDTO user,
            double amount,
            String txnId,
            String senderWalletName,
            String receiverWalletName) {

        logger.info("🔄 Recording INTERNAL TRANSFER | userId={} | sender={} | receiver={} | amount={}",
                user.getId(), senderWalletName, receiverWalletName, amount);

        Wallet senderWallet = getWallet(user.getId(), senderWalletName);

        Transaction tx = createTransaction(
                user,
                amount,
                "INTERNAL",
                txnId,
                senderWallet,
                senderWalletName,
                receiverWalletName
        );

        transactionRepository.save(tx);

        logger.info("✅ Internal transfer recorded | txnId={}", txnId);
    }
     */
}
