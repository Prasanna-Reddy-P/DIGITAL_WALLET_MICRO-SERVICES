package com.example.wallet_service_micro.service.transactions;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WalletTransactionService {

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
        return transactionRepository.existsByTransactionId(transactionId);
    }

    // --------------------------------------------------------------------
    // RECORD LOAD / SELF-CREDITED
    // --------------------------------------------------------------------
    public void recordLoadTransaction(UserDTO user, double amount, String txnId, String walletName) {

        Wallet wallet = walletRepository
                .findByUserIdAndWalletName(user.getId(), walletName)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

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
    }

    // --------------------------------------------------------------------
    // RECORD EXTERNAL TRANSFER (Sender & Receiver)
    // --------------------------------------------------------------------
    public void recordTransferTransactions(
            UserDTO sender, UserDTO recipient,
            double amount, String txnId,
            String senderWalletName, String receiverWalletName) {

        // ✅ Sender Wallet
        Wallet senderWallet = walletRepository
                .findByUserIdAndWalletName(sender.getId(), senderWalletName)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        // ✅ Receiver Wallet
        Wallet recipientWallet = walletRepository
                .findByUserIdAndWalletName(recipient.getId(), receiverWalletName)
                .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));

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
    }

    // --------------------------------------------------------------------
    // RECORD INTERNAL WALLET-TO-WALLET TRANSFER (Same user)
    // --------------------------------------------------------------------
    public void recordInternalTransfer(
            UserDTO user, double amount, String txnId,
            String senderWalletName, String receiverWalletName) {

        Wallet senderWallet = walletRepository
                .findByUserIdAndWalletName(user.getId(), senderWalletName)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

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
    }
}
