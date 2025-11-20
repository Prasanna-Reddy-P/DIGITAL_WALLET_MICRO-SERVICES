package com.example.wallet_service_micro.service.transactions;

import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.mapper.transaction.TransactionMapper;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionService.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;
    private final WalletManagementService walletManagementService;

    public WalletTransactionService(TransactionRepository transactionRepository,
                                    WalletRepository walletRepository,
                                    TransactionMapper transactionMapper,
                                    WalletManagementService walletManagementService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.transactionMapper = transactionMapper;
        this.walletManagementService = walletManagementService;
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

        Wallet wallet = walletManagementService.getWallet(user.getId(), walletName);

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

        Wallet senderWallet = walletManagementService.getWallet(sender.getId(), senderWalletName);
        Wallet receiverWallet = walletManagementService.getWallet(recipient.getId(), receiverWalletName);

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

    public Page<TransactionDTO> getTransactionsByWallet(UserDTO user, String walletName, int page, int size) {
        logger.info("📄 Fetching transactions | userId={} | walletName={} | page={} | size={}",
                user.getId(), walletName, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions =
                transactionRepository.findTransactionsByUserAndWallet(user.getId(), walletName, pageable);
        return transactions.map(transactionMapper::toDTO);
    }

    public Page<TransactionDTO> getTransactions(UserDTO user, int page, int size) {
        logger.info("📜 Fetching paginated transactions | userId={} | page={} | size={}",
                user.getId(), page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> tx = transactionRepository.findByUserId(user.getId(), pageable);
        return tx.map(transactionMapper::toDTO);
    }

    public Page<TransactionDTO> getTransactionsByWalletName(Long userId, String walletName, int page, int size) {
        Wallet wallet = walletManagementService.getWalletOrThrow(userId, walletName);
        return transactionRepository.findByWalletId(wallet.getId(), PageRequest.of(page, size))
                .map(transactionMapper::toDTO);
    }

    public List<TransactionDTO> getUserTransactionsBetween(
            Long userId, String walletName, LocalDateTime start, LocalDateTime end) {

        List<Transaction> result =
                transactionRepository.findByUserAndWalletNameAndTimestampBetween(
                        userId, walletName, start, end
                );

        return result.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
