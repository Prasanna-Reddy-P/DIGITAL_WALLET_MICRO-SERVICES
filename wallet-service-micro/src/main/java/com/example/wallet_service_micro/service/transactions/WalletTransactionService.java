package com.example.wallet_service_micro.service.transactions;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // RECORD LOAD TRANSACTION
    // --------------------------------------------------------------------
    @Transactional
    public void recordLoadTransaction(UserDTO user, double amount, String txnId) {
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + user.getId()));

        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setAmount(amount);
        txn.setType("SELF_CREDITED");
        txn.setUserId(user.getId()); // changed: store user ID only
        txn.setUserEmail(user.getEmail()); // optional field if Transaction has userEmail

        transactionRepository.save(txn);
    }

    // --------------------------------------------------------------------
    // RECORD TRANSFER TRANSACTIONS
    // --------------------------------------------------------------------
    @Transactional
    public void recordTransferTransactions(UserDTO sender, UserDTO receiver, double amount, String txnId) {
        // Sender transaction
        Transaction senderTxn = new Transaction();
        senderTxn.setTransactionId(txnId + "-SENDER");
        senderTxn.setAmount(amount);
        senderTxn.setType("DEBIT");
        senderTxn.setUserId(sender.getId());
        senderTxn.setUserEmail(sender.getEmail());

        // Receiver transaction
        Transaction receiverTxn = new Transaction();
        receiverTxn.setTransactionId(txnId + "-RECEIVER");
        receiverTxn.setAmount(amount);
        receiverTxn.setType("CREDIT");
        receiverTxn.setUserId(receiver.getId());
        receiverTxn.setUserEmail(receiver.getEmail());

        transactionRepository.save(senderTxn);
        transactionRepository.save(receiverTxn);
    }
}
