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
        logger.info("💰 Initiating loadMoney | userId={} | walletName={} | txnId={}",
                user != null ? user.getId() : null, walletName, transactionId);

        if (user == null) throw new UserNotFoundException("User not found");
        if (request == null || request.getAmount() == null)
            throw new IllegalArgumentException("Amount cannot be null");

        if (txnService.isDuplicate(transactionId))
            throw new IllegalArgumentException("Duplicate transaction");

        double amount = request.getAmount();
        logger.debug("🔍 Fetching wallet for load operation | walletName={} | amount={}", walletName, amount);

        Wallet wallet = walletManagementService.getExistingWallet(user, walletName);

        walletValidator.validateNotBlacklisted(wallet);
        walletValidator.validateAmount(amount, "Load");
        wallet.resetDailyIfNewDay();
        walletValidator.validateDailyLimit(wallet, amount);

        logger.info("✅ Validation passed for loadMoney | wallet={} | amount={}", walletName, amount);

        return performLoadMoney(user, amount, transactionId, walletName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = Exception.class)
    public LoadMoneyResponse performLoadMoney(UserDTO user, double amount, String transactionId, String walletName) {
        logger.info("⚙️ Performing transactional wallet load | userId={} | walletName={} | amount={}",
                user.getId(), walletName, amount);

        Wallet wallet = walletManagementService.getExistingWallet(user, walletName);

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setDailySpent(wallet.getDailySpent() + amount);

        if (wallet.getDailySpent() >= walletProperties.getDailyLimit())
            wallet.setFrozen(true);

        walletRepository.saveAndFlush(wallet);
        logger.debug("💾 Wallet updated | newBalance={} | dailySpent={}", wallet.getBalance(), wallet.getDailySpent());

        txnService.recordLoadTransaction(user, amount, transactionId, walletName);
        logger.info("🧾 Load transaction recorded | txnId={}", transactionId);

        LoadMoneyResponse response = walletMapper.toLoadMoneyResponse(wallet);
        response.setWalletName(walletName);
        response.setRemainingDailyLimit(walletProperties.getDailyLimit() - wallet.getDailySpent());
        response.setMessage("Wallet loaded successfully ✅");

        logger.info("✅ LoadMoney completed | userId={} | walletName={} | finalBalance={}",
                user.getId(), walletName, wallet.getBalance());
        return response;
    }

    // --------------------------------------------------------------------
    // ✅ TRANSFER TO ANOTHER USER
    // --------------------------------------------------------------------
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

        logger.debug("✅ Transfer validation passed | senderWallet={} | receiverWallet={}",
                senderWalletName, receiverWallet.getWalletName());

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                logger.info("🚀 Performing transfer attempt #{}", attempt);
                return performTransfer(sender, recipient, amount, transactionId, senderWalletName, "Default");
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
                                            String senderWalletName, String receiverWalletName) {
        logger.info("🔄 Executing transactional transfer | senderId={} | receiverId={} | amount={}",
                sender.getId(), recipient.getId(), amount);

        Wallet senderWallet = walletManagementService.getExistingWallet(sender, senderWalletName);
        Wallet receiverWallet = walletManagementService.getExistingWallet(recipient, receiverWalletName);

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        senderWallet.setDailySpent(senderWallet.getDailySpent() + amount);

        if (senderWallet.getDailySpent() >= walletProperties.getDailyLimit())
            senderWallet.setFrozen(true);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        txnService.recordTransferTransactions(sender, recipient, amount, transactionId,
                senderWalletName, receiverWalletName);

        logger.info("✅ Transfer complete | senderBalance={} | receiverBalance={}",
                senderWallet.getBalance(), receiverWallet.getBalance());

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
        response.setMessage("Transfer successful ✅");

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

    public Page<TransactionDTO> getTransactionsByWallet(UserDTO user, String walletName, int page, int size) {
        logger.info("📄 Fetching transactions | userId={} | walletName={} | page={} | size={}",
                user.getId(), walletName, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions =
                transactionRepository.findTransactionsByUserAndWallet(user.getId(), walletName, pageable);
        return transactions.map(transactionMapper::toDTO);
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

    public List<WalletBalanceResponse> getAllWalletsByUserId(Long userId) {
        logger.info("📋 Admin fetching wallets for userId={}", userId);
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(wallet -> {
                    WalletBalanceResponse dto = walletMapper.toBalanceResponse(wallet);
                    dto.setMessage("Wallet: " + wallet.getWalletName());
                    return dto;
                })
                .toList();
    }

    public WalletBalanceResponse getWalletByUserIdAndWalletName(Long userId, String walletName) {
        logger.info("🔍 Admin fetching wallet | userId={} | walletName={}", userId, walletName);
        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException("Wallet '" + walletName + "' not found for userId=" + userId));
        WalletBalanceResponse response = walletMapper.toBalanceResponse(wallet);
        response.setMessage("Wallet fetched successfully ✅");
        return response;
    }

    public Page<TransactionDTO> getTransactions(UserDTO user, int page, int size) {
        logger.info("📜 Fetching paginated transactions | userId={} | page={} | size={}",
                user.getId(), page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> tx = transactionRepository.findByUserId(user.getId(), pageable);
        return tx.map(transactionMapper::toDTO);
    }

    public void blacklistWalletByName(Long userId, String walletName, String authHeader) {
        logger.warn("🚫 Blacklisting wallet | userId={} | walletName={}", userId, walletName);

        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException("Wallet '" + walletName + "' not found for userId=" + userId));

        wallet.setBlacklisted(true);
        walletRepository.save(wallet);

        boolean allBlacklisted = walletRepository.findByUserId(userId)
                .stream()
                .allMatch(Wallet::getBlacklisted);

        if (allBlacklisted) {
            userClient.blacklistUser(userId, authHeader);
            logger.info("✅ All wallets for user {} are BLACKLISTED → User also BLACKLISTED", userId);
        } else {
            logger.info("⚠️ Wallet '{}' blacklisted, but user NOT blacklisted (other wallets active)", walletName);
        }
    }

    public boolean areAllWalletsBlacklisted(Long userId) {
        boolean result = walletRepository.findByUserId(userId)
                .stream()
                .allMatch(Wallet::getBlacklisted);
        logger.debug("🧩 Check all wallets blacklisted | userId={} | result={}", userId, result);
        return result;
    }

    @Transactional
    public int unblacklistAllWallets(Long userId, String authHeader) {
        logger.info("♻️ Unblocking all wallets | userId={}", userId);
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        if (wallets.isEmpty()) {
            logger.warn("⚠️ No wallets found for userId={}", userId);
            return 0;
        }

        wallets.forEach(wallet -> wallet.setBlacklisted(false));
        walletRepository.saveAll(wallets);
        userClient.unblacklistUser(userId, authHeader);

        logger.info("✅ All wallets unblocked for userId={}", userId);
        return wallets.size();
    }

    public Page<TransactionDTO> getTransactionsByWalletName(Long userId, String walletName, int page, int size) {
        logger.info("📄 Fetching transactions by wallet | userId={} | walletName={}", userId, walletName);
        Wallet wallet = walletRepository.findByUserIdAndWalletName(userId, walletName)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletName));

        return transactionRepository.findByWalletId(wallet.getId(), PageRequest.of(page, size))
                .map(transactionMapper::toDTO);
    }
}
