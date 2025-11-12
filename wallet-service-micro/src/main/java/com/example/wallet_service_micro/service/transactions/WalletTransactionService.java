package com.example.wallet_service_micro.service.transactions;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WalletTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionService.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public WalletTransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    // --------------------------------------------------------------------
    // DUPLICATE CHECK
    // --------------------------------------------------------------------
    public boolean isDuplicate(String transactionId) {
        logger.debug("🔍 Checking for duplicate transaction with ID: {}", transactionId);
        boolean exists = transactionRepository.existsByTransactionId(transactionId);
        if (exists) {
            logger.warn("⚠️ Duplicate transaction detected: {}", transactionId);
        } else {
            logger.info("✅ Transaction ID '{}' is unique", transactionId);
        }
        return exists;
    }

    // --------------------------------------------------------------------
    // RECORD LOAD / SELF-CREDITED
    // --------------------------------------------------------------------
    public void recordLoadTransaction(UserDTO user, double amount, String txnId, String walletName) {
        logger.info("💰 Recording LOAD transaction | userId={} | walletName={} | txnId={} | amount={}",
                user.getId(), walletName, txnId, amount);

        Wallet wallet = walletRepository
                .findByUserIdAndWalletName(user.getId(), walletName)
                .orElseThrow(() -> {
                    logger.error("❌ Wallet '{}' not found for userId={}", walletName, user.getId());
                    return new RuntimeException("Wallet not found");
                });

        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setAmount(amount);
        tx.setType("SELF_CREDITED");
        tx.setTimestamp(LocalDateTime.now());
        tx.setUserEmail(user.getEmail());
        tx.setTransactionId(txnId);

        tx.setWalletId(wallet.getId());
        tx.setWalletName(wallet.getWalletName());

        tx.setSenderWalletName(null);
        tx.setReceiverWalletName(wallet.getWalletName());

        transactionRepository.save(tx);
        logger.info("✅ Load transaction recorded successfully | txnId={} | wallet={}", txnId, walletName);
    }

    // --------------------------------------------------------------------
    // RECORD EXTERNAL TRANSFER (Sender & Receiver)
    // --------------------------------------------------------------------
    public void recordTransferTransactions(
            UserDTO sender, UserDTO recipient,
            double amount, String txnId,
            String senderWalletName, String receiverWalletName) {

        logger.info("💸 Recording TRANSFER transaction | txnId={} | senderId={} | receiverId={} | amount={}",
                txnId, sender.getId(), recipient.getId(), amount);

        // ✅ Sender Wallet
        Wallet senderWallet = walletRepository
                .findByUserIdAndWalletName(sender.getId(), senderWalletName)
                .orElseThrow(() -> {
                    logger.error("❌ Sender wallet '{}' not found for userId={}", senderWalletName, sender.getId());
                    return new RuntimeException("Sender wallet not found");
                });

        // ✅ Receiver Wallet
        Wallet recipientWallet = walletRepository
                .findByUserIdAndWalletName(recipient.getId(), receiverWalletName)
                .orElseThrow(() -> {
                    logger.error("❌ Recipient wallet '{}' not found for userId={}", receiverWalletName, recipient.getId());
                    return new RuntimeException("Recipient wallet not found");
                });

        // ✅ Debit entry (Sender)
        Transaction debit = new Transaction();
        debit.setUserId(sender.getId());
        debit.setAmount(amount);
        debit.setType("DEBIT");
        debit.setTimestamp(LocalDateTime.now());
        debit.setUserEmail(sender.getEmail());
        debit.setTransactionId(txnId);

        debit.setWalletId(senderWallet.getId());
        debit.setWalletName(senderWallet.getWalletName());

        debit.setSenderWalletName(senderWalletName);
        debit.setReceiverWalletName(receiverWalletName);

        transactionRepository.save(debit);
        logger.debug("💳 Debit transaction saved | userId={} | wallet={} | amount={}",
                sender.getId(), senderWalletName, amount);

        // ✅ Credit entry (Receiver)
        Transaction credit = new Transaction();
        credit.setUserId(recipient.getId());
        credit.setAmount(amount);
        credit.setType("CREDIT");
        credit.setTimestamp(LocalDateTime.now());
        credit.setUserEmail(recipient.getEmail());
        credit.setTransactionId(txnId);

        credit.setWalletId(recipientWallet.getId());
        credit.setWalletName(recipientWallet.getWalletName());

        credit.setSenderWalletName(senderWalletName);
        credit.setReceiverWalletName(receiverWalletName);

        transactionRepository.save(credit);
        logger.debug("💰 Credit transaction saved | userId={} | wallet={} | amount={}",
                recipient.getId(), receiverWalletName, amount);

        logger.info("✅ Transfer transaction recorded successfully | txnId={}", txnId);
    }

    // --------------------------------------------------------------------
    // RECORD INTERNAL WALLET-TO-WALLET TRANSFER (Same user)
    // --------------------------------------------------------------------
    public void recordInternalTransfer(
            UserDTO user, double amount, String txnId,
            String senderWalletName, String receiverWalletName) {

        logger.info("🔄 Recording INTERNAL TRANSFER | userId={} | senderWallet={} | receiverWallet={} | amount={}",
                user.getId(), senderWalletName, receiverWalletName, amount);

        Wallet senderWallet = walletRepository
                .findByUserIdAndWalletName(user.getId(), senderWalletName)
                .orElseThrow(() -> {
                    logger.error("❌ Sender wallet '{}' not found for userId={}", senderWalletName, user.getId());
                    return new RuntimeException("Sender wallet not found");
                });

        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setAmount(amount);
        tx.setType("INTERNAL");
        tx.setTimestamp(LocalDateTime.now());
        tx.setUserEmail(user.getEmail());
        tx.setTransactionId(txnId);

        tx.setWalletId(senderWallet.getId());
        tx.setWalletName(senderWallet.getWalletName());

        tx.setSenderWalletName(senderWalletName);
        tx.setReceiverWalletName(receiverWalletName);

        transactionRepository.save(tx);
        logger.info("✅ Internal transfer recorded successfully | txnId={} | senderWallet={} | receiverWallet={}",
                txnId, senderWalletName, receiverWalletName);
    }
}
