package com.example.wallet_service_micro.service.wallet;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.config.properties.WalletProperties;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferResponse;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;
import com.example.wallet_service_micro.mapper.transaction.TransactionMapper;
import com.example.wallet_service_micro.mapper.wallet.WalletMapper;
import com.example.wallet_service_micro.model.transaction.Transaction;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.repository.transaction.TransactionRepository;
import com.example.wallet_service_micro.repository.wallet.WalletRepository;
import com.example.wallet_service_micro.service.factory.WalletManagementService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private final WalletProperties walletProperties;
    private final TransactionMapper transactionMapper;
    private final WalletMapper walletMapper;

    private final WalletValidator walletValidator;
    private final WalletTransactionService txnService;
    private final WalletManagementService walletManagementService;
    private final UserClient userClient;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository,
                         WalletProperties walletProperties,
                         TransactionMapper transactionMapper,
                         WalletMapper walletMapper,
                         WalletValidator walletValidator,
                         WalletTransactionService txnService,
                         WalletManagementService walletManagementService,
                         UserClient userClient) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletProperties = walletProperties;
        this.transactionMapper = transactionMapper;
        this.walletMapper = walletMapper;
        this.walletValidator = walletValidator;
        this.txnService = txnService;
        this.walletManagementService = walletManagementService;
        this.userClient = userClient;
    }

    // --------------------------------------------------------------------
    // ✅ LOAD MONEY
    // --------------------------------------------------------------------
    public LoadMoneyResponse loadMoney(UserDTO user, LoadMoneyRequest request, String transactionId, String walletName) {
        logger.info("💰 Initiating loadMoney | userId={} | walletName={} | txnId={}",
                user != null ? user.getId() : null, walletName, transactionId);

        if (user == null) throw new UserNotFoundException("User not found");
        if (request == null || request.getAmount() == null)
            throw new IllegalArgumentException("Amount cannot be null");

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        double amount = request.getAmount();
        logger.debug("🔍 Fetching wallet for load operation | walletName={} | amount={}", walletName, amount);

        // Fetch wallet once
        Wallet wallet = walletManagementService.getExistingWallet(user, walletName);

        // Validation logs
        walletValidator.validateNotBlacklisted(wallet);
        walletValidator.validateAmount(amount, "Load");
        wallet.resetDailyIfNewDay();
        walletValidator.validateDailyLimit(wallet, amount);

        logger.info("✅ Validation passed for loadMoney | wallet={} | amount={}", walletName, amount);

        return performLoadMoney(user, wallet, amount, transactionId);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public LoadMoneyResponse performLoadMoney(UserDTO user, Wallet wallet, double amount, String transactionId) {
        logger.info("⚙️ Performing transactional wallet load | userId={} | walletName={} | amount={}",
                user.getId(), wallet.getWalletName(), amount);

        double projectedDailySpent = wallet.getDailySpent() + amount;

        // If transaction exceeds daily limit, log and throw
        if (projectedDailySpent > walletProperties.getDailyLimit()) {
            double remainingLimit = walletProperties.getDailyLimit() - wallet.getDailySpent();
            logger.warn("❌ Daily limit exceeded | walletName={} | currentSpent={} | attemptedAmount={} | remainingLimit={}",
                    wallet.getWalletName(), wallet.getDailySpent(), amount, remainingLimit);
            throw new IllegalArgumentException(
                    "Transaction exceeds daily limit. You can only add " + remainingLimit + " more today.");
        }

        // Update wallet balance
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setDailySpent(projectedDailySpent);

        // Freeze wallet if exactly reaching limit
        if (wallet.getDailySpent() == walletProperties.getDailyLimit()) {
            wallet.setFrozen(true);
            logger.warn("❄️ Wallet frozen due to reaching daily limit | walletName={} | dailySpent={}",
                    wallet.getWalletName(), wallet.getDailySpent());
        }

        walletRepository.saveAndFlush(wallet);
        logger.debug("💾 Wallet updated | newBalance={} | dailySpent={}", wallet.getBalance(), wallet.getDailySpent());

        // Record transaction
        txnService.recordLoadTransaction(user, amount, transactionId, wallet.getWalletName());
        logger.info("🧾 Load transaction recorded | txnId={}", transactionId);

        // Prepare response
        LoadMoneyResponse response = walletMapper.toLoadMoneyResponse(wallet);
        response.setWalletName(wallet.getWalletName());
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - wallet.getDailySpent());
        response.setMessage(wallet.getFrozen()
                ? "Wallet frozen as daily limit reached"
                : "Wallet loaded successfully ✅");

        logger.info("✅ LoadMoney completed | userId={} | walletName={} | finalBalance={}",
                user.getId(), wallet.getWalletName(), wallet.getBalance());

        return response;
    }



    public TransferResponse transferAmount(UserDTO sender, Long receiverId, Double amount, String transactionId,
                                           String senderWalletName, String authHeader) {
        logger.info("💸 Initiating transfer | senderId={} | receiverId={} | amount={} | txnId={}",
                sender != null ? sender.getId() : null, receiverId, amount, transactionId);

        if (sender == null) throw new UserNotFoundException("Sender not found");

        UserDTO recipient = userClient.getUserByIdInternal(receiverId);
        if (recipient == null) throw new UserNotFoundException("Recipient not found");

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        walletValidator.validateAmount(amount, "Transfer");

        Wallet senderWallet = walletManagementService.getExistingWallet(sender, senderWalletName);
        Wallet receiverWallet = walletManagementService.getExistingWallet(recipient, "Default");

        walletValidator.validateNotBlacklisted(senderWallet);
        walletValidator.validateNotBlacklisted(receiverWallet);

        senderWallet.resetDailyIfNewDay();
        receiverWallet.resetDailyIfNewDay();

        walletValidator.validateFrozen(senderWallet);
        walletValidator.validateBalance(senderWallet, amount);
        walletValidator.validateDailyLimit(senderWallet, amount);

        logger.debug("✅ Transfer validation passed | senderWallet={} | receiverWallet={}",
                senderWalletName, receiverWallet.getWalletName());

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                logger.info("🚀 Performing transfer attempt #{}", attempt);
                return performTransfer(sender, recipient, amount, transactionId, senderWallet, receiverWallet);
            } catch (ObjectOptimisticLockingFailureException e) {
                logger.warn("⚠️ Optimistic locking conflict during transfer attempt #{}", attempt);
                if (attempt == 3) throw e;
                sleep(300);
            }
        }
        throw new RuntimeException("Unexpected transfer failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public TransferResponse performTransfer(UserDTO sender, UserDTO recipient, double amount, String transactionId,
                                            Wallet senderWallet, Wallet receiverWallet) {
        logger.info("🔄 Executing transactional transfer | senderId={} | receiverId={} | amount={}",
                sender.getId(), recipient.getId(), amount);

        if (sender.getId().equals(recipient.getId())) {
            logger.info("🔄 Redirecting to internal transfer | userId={} | wallet='{}'", sender.getId(), senderWallet.getWalletName());
            throw new IllegalArgumentException("Use internal transfer API for transferring between your own wallets");
        }

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        senderWallet.setDailySpent(senderWallet.getDailySpent() + amount);

        if (senderWallet.getDailySpent() >= walletProperties.getDailyLimit())
            senderWallet.setFrozen(true);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        txnService.recordTransferTransactions(sender, recipient, amount, transactionId,
                senderWallet.getWalletName(), receiverWallet.getWalletName());

        logger.info("✅ Transfer complete | senderBalance={} | receiverBalance={}",
                senderWallet.getBalance(), receiverWallet.getBalance());

        TransferResponse response = walletMapper.toTransferResponse(senderWallet);
        response.setSenderWalletName(senderWallet.getWalletName());
        response.setReceiverWalletName(receiverWallet.getWalletName());
        response.setAmountTransferred(amount);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - senderWallet.getDailySpent());
        response.setFrozen(senderWallet.getFrozen());

        if (senderWallet.getFrozen()) {
            response.setMessage("Wallet Frozen due to daily limit hit");
        } else {
            response.setMessage("Transfer successful ✅");
        }

        return response;
    }


    // --------------------------------------------------------------------
    // ✅ INTERNAL TRANSFER (USER → USER)
    // --------------------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public UserInternalTransferResponse transferWithinUserWallets(UserDTO user,
                                                                  String senderWalletName,
                                                                  String receiverWalletName,
                                                                  double amount,
                                                                  String transactionId) {
        logger.info("🔁 Initiating internal transfer | userId={} | from={} | to={} | amount={}",
                user.getId(), senderWalletName, receiverWalletName, amount);

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        walletValidator.validateAmount(amount, "Internal Transfer");

        Wallet senderWallet = walletManagementService.getExistingWallet(user, senderWalletName);
        Wallet receiverWallet = walletManagementService.getExistingWallet(user, receiverWalletName);

        walletValidator.validateNotBlacklisted(senderWallet);
        walletValidator.validateNotBlacklisted(receiverWallet);

        senderWallet.resetDailyIfNewDay();
        receiverWallet.resetDailyIfNewDay();

        walletValidator.validateFrozen(senderWallet);
        walletValidator.validateBalance(senderWallet, amount);
        walletValidator.validateDailyLimit(senderWallet, amount);

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        senderWallet.setDailySpent(senderWallet.getDailySpent() + amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        if (senderWallet.getDailySpent() >= walletProperties.getDailyLimit())
            senderWallet.setFrozen(true);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);
        txnService.recordTransferTransactions(user, user, amount, transactionId,
                senderWalletName, receiverWalletName);

        logger.info("✅ Internal transfer complete | fromBalance={} | toBalance={}",
                senderWallet.getBalance(), receiverWallet.getBalance());

        UserInternalTransferResponse response = walletMapper.toInternalTransferResponse(senderWallet);
        response.setSenderWalletName(senderWalletName);
        response.setReceiverWalletName(receiverWalletName);
        response.setReceiverBalance(receiverWallet.getBalance());
        response.setAmountTransferred(amount);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - senderWallet.getDailySpent());
        if (senderWallet.getFrozen()) {
            response.setMessage("Wallet is  FROZEN due to daily limit hit❌");
        } else {
            response.setMessage("Transfer successful ✅");
        }

        return response;
    }

    // --------------------------------------------------------------------
    // ✅ OTHER HELPERS / ADMIN METHODS WITH LOGS
    // --------------------------------------------------------------------
    public List<WalletBalanceResponse> getAllWalletsForUserDTO(UserDTO user) {
        logger.info("📋 Fetching all wallets for userId={}", user.getId());
        return walletRepository.findByUserId(user.getId()).stream()
                .map(wallet -> {
                    WalletBalanceResponse dto = new WalletBalanceResponse();
                    dto.setBalance(wallet.getBalance());
                    dto.setFrozen(wallet.getFrozen());
                    dto.setMessage("Wallet: " + wallet.getWalletName());
                    return dto;
                })
                .toList();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public WalletBalanceResponse toWalletBalanceResponse(Wallet wallet) {
        WalletBalanceResponse response = walletMapper.toBalanceResponse(wallet);
        response.setMessage("Balance fetched successfully ✅");
        return response;
    }


}
