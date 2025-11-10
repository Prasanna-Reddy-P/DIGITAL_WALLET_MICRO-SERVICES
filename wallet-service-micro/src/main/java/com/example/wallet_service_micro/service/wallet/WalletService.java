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

import java.util.List;

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

        if (user == null) throw new UserNotFoundException("User not found");
        if (request == null || request.getAmount() == null)
            throw new IllegalArgumentException("Amount cannot be null");

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return performLoadMoney(user, request.getAmount(), transactionId, walletName);
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == 3) throw e;
                sleep(300);
            }
        }
        throw new RuntimeException("Unexpected load failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public LoadMoneyResponse performLoadMoney(UserDTO user, double amount, String transactionId, String walletName) {

        walletValidator.validateAmount(amount, "Load");

        // ✅ Wallet must already exist
        Wallet wallet = walletManagementService.getExistingWallet(user, walletName);

        wallet.resetDailyIfNewDay();
        walletValidator.validateDailyLimit(wallet, amount);

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setDailySpent(wallet.getDailySpent() + amount);

        if (wallet.getDailySpent() >= walletProperties.getDailyLimit())
            wallet.setFrozen(true);

        walletRepository.saveAndFlush(wallet);

        txnService.recordLoadTransaction(user, amount, transactionId, walletName);

        LoadMoneyResponse response = walletMapper.toLoadMoneyResponse(wallet);
        response.setWalletName(walletName);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - wallet.getDailySpent());
        response.setMessage("Wallet loaded successfully ✅");

        return response;
    }

    // --------------------------------------------------------------------
    // ✅ TRANSFER TO ANOTHER USER
    // --------------------------------------------------------------------
    public TransferResponse transferAmount(UserDTO sender, Long receiverId, Double amount,
                                           String transactionId, String senderWalletName, String authHeader) {

        if (sender == null) throw new UserNotFoundException("Sender not found");

        UserDTO recipient = userClient.getUserById(receiverId, authHeader);
        if (recipient == null) throw new UserNotFoundException("Recipient not found");

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return performTransfer(sender, recipient, amount, transactionId, senderWalletName, "Default");
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == 3) throw e;
                sleep(300);
            }
        }
        throw new RuntimeException("Unexpected transfer failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public TransferResponse performTransfer(UserDTO sender, UserDTO recipient, double amount,
                                            String transactionId, String senderWalletName, String receiverWalletName) {

        walletValidator.validateAmount(amount, "Transfer");

        Wallet senderWallet = walletManagementService.getExistingWallet(sender, senderWalletName);
        Wallet receiverWallet = walletManagementService.getExistingWallet(recipient, receiverWalletName);

        senderWallet.resetDailyIfNewDay();
        receiverWallet.resetDailyIfNewDay();

        walletValidator.validateFrozen(senderWallet);
        walletValidator.validateBalance(senderWallet, amount);

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        senderWallet.setDailySpent(senderWallet.getDailySpent() + amount);

        if (senderWallet.getDailySpent() >= walletProperties.getDailyLimit())
            senderWallet.setFrozen(true);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        txnService.recordTransferTransactions(sender, recipient, amount, transactionId,
                senderWalletName, receiverWalletName);

        TransferResponse response = walletMapper.toTransferResponse(senderWallet);
        response.setSenderWalletName(senderWalletName);
        response.setReceiverWalletName(receiverWalletName);
        response.setAmountTransferred(amount);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - senderWallet.getDailySpent());
        response.setFrozen(senderWallet.getFrozen());
        response.setMessage("Transfer successful ✅");

        return response;
    }

    // --------------------------------------------------------------------
    // ✅ INTERNAL TRANSFER (User → User)
    // --------------------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public UserInternalTransferResponse transferWithinUserWallets(UserDTO user,
                                                                  String senderWalletName,
                                                                  String receiverWalletName,
                                                                  double amount,
                                                                  String transactionId) {

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        walletValidator.validateAmount(amount, "Internal Transfer");

        Wallet senderWallet = walletManagementService.getExistingWallet(user, senderWalletName);
        Wallet receiverWallet = walletManagementService.getExistingWallet(user, receiverWalletName);

        senderWallet.resetDailyIfNewDay();
        receiverWallet.resetDailyIfNewDay();

        walletValidator.validateFrozen(senderWallet);
        walletValidator.validateBalance(senderWallet, amount);

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        senderWallet.setDailySpent(senderWallet.getDailySpent() + amount);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        if (senderWallet.getDailySpent() >= walletProperties.getDailyLimit())
            senderWallet.setFrozen(true);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        txnService.recordTransferTransactions(user, user, amount, transactionId,
                senderWalletName, receiverWalletName);

        UserInternalTransferResponse response = walletMapper.toInternalTransferResponse(senderWallet);
        response.setSenderWalletName(senderWalletName);
        response.setReceiverWalletName(receiverWalletName);
        response.setReceiverBalance(receiverWallet.getBalance());
        response.setAmountTransferred(amount);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - senderWallet.getDailySpent());
        response.setMessage("Transfer successful ✅");

        return response;
    }

    // --------------------------------------------------------------------
    // ✅ LIST WALLETS FOR USER
    // --------------------------------------------------------------------
    public List<WalletBalanceResponse> getAllWalletsForUserDTO(UserDTO user) {
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

    // --------------------------------------------------------------------
    // ✅ GET TRANSACTION HISTORY
    // --------------------------------------------------------------------
    public Page<TransactionDTO> getTransactionsByWallet(UserDTO user, String walletName, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactions =
                transactionRepository.findTransactionsByUserAndWallet(
                        user.getId(),
                        walletName,
                        pageable
                );

        return transactions.map(transactionMapper::toDTO);
    }



    // --------------------------------------------------------------------
    // ✅ Helper: Sleep for retry logic
    // --------------------------------------------------------------------
    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // --------------------------------------------------------------------
// Convert Wallet to WalletBalanceResponse
// --------------------------------------------------------------------
    public WalletBalanceResponse toWalletBalanceResponse(Wallet wallet) {
        WalletBalanceResponse response = walletMapper.toBalanceResponse(wallet);
        response.setMessage("Balance fetched successfully ✅");
        return response;
    }

    // --------------------------------------------------------------------
// ✅ ADMIN: Get all wallets for a user (DTO response)
// --------------------------------------------------------------------
    public List<WalletBalanceResponse> getAllWalletsByUserId(Long userId) {

        List<Wallet> wallets = walletRepository.findByUserId(userId);

        return wallets.stream()
                .map(wallet -> {
                    WalletBalanceResponse dto = walletMapper.toBalanceResponse(wallet);
                    dto.setMessage("Wallet: " + wallet.getWalletName());
                    return dto;
                })
                .toList();
    }


    // --------------------------------------------------------------------
// ✅ ADMIN: Get specific wallet by userId and walletName (DTO response)
// --------------------------------------------------------------------
    public WalletBalanceResponse getWalletByUserIdAndWalletName(Long userId, String walletName) {

        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet '" + walletName + "' not found for userId=" + userId
                ));

        WalletBalanceResponse response = walletMapper.toBalanceResponse(wallet);
        response.setMessage("Wallet fetched successfully ✅");

        return response;
    }

    public Page<TransactionDTO> getTransactions(UserDTO user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> tx = transactionRepository.findByUserId(user.getId(), pageable);
        return tx.map(transactionMapper::toDTO);
    }

    // --------------------------------------------------------------------
// ✅ ADMIN: Blacklist wallet by userId + walletName
// --------------------------------------------------------------------
    public void blacklistWalletByName(Long userId, String walletName, String authHeader) {

        // ✅ Fetch wallet for user
        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet '" + walletName + "' not found for userId=" + userId
                ));

        // ✅ Blacklist only this wallet (not frozen)
        wallet.setBlacklisted(true);
        walletRepository.save(wallet);

        // ✅ Also blacklist the user in user-service
        userClient.blacklistUser(userId, authHeader);

        logger.info("✅ Wallet '{}' for user {} has been BLACKLISTED, user also blacklisted", walletName, userId);
    }



}
