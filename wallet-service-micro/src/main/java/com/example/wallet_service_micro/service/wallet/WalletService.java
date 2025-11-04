package com.example.wallet_service_micro.service.wallet;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.config.properties.WalletProperties;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.exception.UserNotFoundException;
import com.example.wallet_service_micro.mapper.transaction.TransactionMapper;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.service.factory.WalletFactory;
import com.example.wallet_service_micro.service.transactions.WalletTransactionService;
import com.example.wallet_service_micro.service.validator.WalletValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletProperties walletProperties;
    private final TransactionMapper transactionMapper;
    private final WalletMapper walletMapper;
    private final WalletFactory walletFactory;
    private final WalletValidator walletValidator;
    private final WalletTransactionService txnService;
    private final UserClient userClient;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository,
                         WalletProperties walletProperties,
                         TransactionMapper transactionMapper,
                         WalletMapper walletMapper,
                         WalletFactory walletFactory,
                         WalletValidator walletValidator,
                         WalletTransactionService txnService,
                         UserClient userClient) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletProperties = walletProperties;
        this.transactionMapper = transactionMapper;
        this.walletMapper = walletMapper;
        this.walletFactory = walletFactory;
        this.walletValidator = walletValidator;
        this.txnService = txnService;
        this.userClient = userClient;
    }

    // --------------------------------------------------------------------
// LOAD MONEY
// --------------------------------------------------------------------
    public LoadMoneyResponse loadMoney(UserDTO user, LoadMoneyRequest request, String transactionId) {
        if (user == null) throw new UserNotFoundException("User not found");
        if (request == null || request.getAmount() == null)
            throw new IllegalArgumentException("Invalid request: amount cannot be null");

        double amount = request.getAmount();

        String thread = Thread.currentThread().getName();
        logger.info("🚀 [LOAD][{}] Start loadMoney | user={} | txnId={} | amount={}",
                thread, user.getEmail(), transactionId, amount);

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction — already processed.");

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return performLoadMoney(user, amount, transactionId);
            } catch (ObjectOptimisticLockingFailureException e) {
                logger.warn("🔒 [LOAD][{}] Version conflict detected — retrying attempt {}", thread, attempt);
                if (attempt == maxRetries)
                    throw new RuntimeException("Load failed after retries", e);
                sleep(500);
            }
        }
        throw new RuntimeException("Unexpected loadMoney failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public LoadMoneyResponse performLoadMoney(UserDTO user, double amount, String transactionId) {
        walletValidator.validateAmount(amount, "Load");

        Wallet wallet = walletFactory.getOrCreateWallet(user);
        wallet.resetDailyIfNewDay();
        walletValidator.validateDailyLimit(wallet, amount);

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setDailySpent(wallet.getDailySpent() + amount);

        if (wallet.getDailySpent() >= walletProperties.getDailyLimit())
            wallet.setFrozen(true);

        walletRepository.saveAndFlush(wallet);
        txnService.recordLoadTransaction(user, amount, transactionId);

        // ✅ Use WalletMapper instead of manual object creation
        LoadMoneyResponse response = walletMapper.toLoadMoneyResponse(wallet);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - wallet.getDailySpent());
        response.setMessage("Wallet loaded successfully ✅");

        return response;
    }


    // --------------------------------------------------------------------
    // TRANSFER MONEY
    // --------------------------------------------------------------------
    public TransferResponse transferAmount(UserDTO sender, Long receiverId, Double amount, String transactionId, String authHeader)
    {
        if (sender == null)
            throw new UserNotFoundException("Sender not found");

        UserDTO recipient = userClient.getUserById(receiverId, authHeader);
        if (recipient == null)
            throw new UserNotFoundException("Recipient not found with id: " + receiverId);

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction — already processed.");

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return performTransfer(sender, recipient, amount, transactionId);
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == maxRetries)
                    throw new RuntimeException("Transfer failed after retries", e);
                sleep(500);
            }
        }
        throw new RuntimeException("Unexpected transfer failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public TransferResponse performTransfer(UserDTO sender, UserDTO recipient, double amount, String transactionId) {
        walletValidator.validateAmount(amount, "Transfer");

        Wallet senderWallet = walletFactory.getOrCreateWallet(sender);
        Wallet receiverWallet = walletFactory.getOrCreateWallet(recipient);

        senderWallet.resetDailyIfNewDay();
        walletValidator.validateFrozen(senderWallet);
        walletValidator.validateBalance(senderWallet, amount);

        receiverWallet.resetDailyIfNewDay();

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        senderWallet.setDailySpent(senderWallet.getDailySpent() + amount);
        if (senderWallet.getDailySpent() >= walletProperties.getDailyLimit())
            senderWallet.setFrozen(true);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        txnService.recordTransferTransactions(sender, recipient, amount, transactionId);

        TransferResponse response = walletMapper.toTransferResponse(senderWallet);
        response.setAmountTransferred(amount);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - senderWallet.getDailySpent());
        response.setFrozen(senderWallet.getFrozen());
        response.setMessage("Transfer successful ✅");

        return response;
    }

    // --------------------------------------------------------------------
    // TRANSACTION HISTORY
    // --------------------------------------------------------------------
    public Page<TransactionDTO> getTransactions(UserDTO user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByUserId(user.getId(), pageable)
                .map(transactionMapper::toDTO);
    }

    // --------------------------------------------------------------------
    // HELPER MAPPERS
    // --------------------------------------------------------------------
    public WalletBalanceResponse toWalletBalanceResponse(Wallet wallet) {
        WalletBalanceResponse response = walletMapper.toBalanceResponse(wallet);
        response.setMessage("Balance fetched successfully ✅");
        return response;
    }


    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
