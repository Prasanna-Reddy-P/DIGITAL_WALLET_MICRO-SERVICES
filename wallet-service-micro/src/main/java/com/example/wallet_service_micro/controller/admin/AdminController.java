package com.example.wallet_service_micro.controller.admin;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.BlackList.WalletBlacklistResponse;
import com.example.wallet_service_micro.dto.BlackList.walletUnblacklistResponse;
import com.example.wallet_service_micro.dto.userRequest.UserIdRequest;
import com.example.wallet_service_micro.dto.userRequest.UserTransactionRequest;
import com.example.wallet_service_micro.dto.walletRequest.WalletNameRequest;
import com.example.wallet_service_micro.dto.walletRequest.WalletTransactionRequest;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.exception.auth.ForbiddenException;
import com.example.wallet_service_micro.exception.auth.UnauthorizedException;
import com.example.wallet_service_micro.exception.user.UserNotFoundException;
import com.example.wallet_service_micro.service.wallet.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final WalletService walletService;
    private final UserClient userClient;

    public AdminController(WalletService walletService, UserClient userClient) {
        this.walletService = walletService;
        this.userClient = userClient;
    }

    // ✅ Unified Admin Validation
    private UserDTO validateAdmin(String authHeader) {
        logger.debug("🔐 Validating admin from Authorization header...");
        UserDTO admin = userClient.getUserFromToken(authHeader);

        if (admin == null) {
            logger.warn("❌ Unauthorized access attempt: token invalid or missing");
            throw new UnauthorizedException("Unauthorized access");
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            logger.warn("🚫 Forbidden access attempt by userId={} with role={}", admin.getId(), admin.getRole());
            throw new ForbiddenException("Admins only");
        }

        logger.info("✅ Admin validated successfully: id={}, name={}, role={}", admin.getId(), admin.getName(), admin.getRole());
        return admin;
    }

    // ✅ Get User Transaction History
    @PostMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getUserTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserTransactionRequest request) {

        logger.info("📘 Admin requested transaction history for userId={} (page={}, size={})",
                request.getUserId(), request.getPage(), request.getSize());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        int page = request.getPage();
        int size = request.getSize();

        UserDTO user = userClient.getUserByIdInternal(userId);
        if (user == null) {
            logger.error("❌ User not found with ID={}", userId);
            throw new UserNotFoundException("User not found with ID " + userId);
        }

        logger.debug("➡ Fetching transactions from WalletService for userId={}", userId);
        Page<TransactionDTO> transactions = walletService.getTransactions(user, page, size);

        logger.info("✅ Transaction history fetched successfully for userId={} with {} records", userId, transactions.getTotalElements());
        return ResponseEntity.ok(transactions);
    }

    // ✅ Get Default Wallet Balance
    @PostMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalanceByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        logger.info("💰 Admin requested default wallet balance for userId={}", request.getUserId());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserByIdInternal(userId);

        WalletBalanceResponse response = walletService.getWalletByUserIdAndWalletName(userId, "Default");
        response.setMessage("Balance fetched successfully for userId=" + userId);

        logger.info("✅ Default wallet balance fetched successfully for userId={}, balance={}", userId, response.getBalance());
        return ResponseEntity.ok(response);
    }

    // ✅ Get All Wallets for a User
    @PostMapping("/wallets")
    public ResponseEntity<List<WalletBalanceResponse>> getAllWalletsByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        logger.info("🧾 Admin requested all wallets for userId={}", request.getUserId());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserByIdInternal(userId);

        List<WalletBalanceResponse> wallets = walletService.getAllWalletsByUserId(userId);
        logger.info("✅ {} wallets fetched successfully for userId={}", wallets.size(), userId);

        return ResponseEntity.ok(wallets);
    }

    // ✅ Get Balance of Specific Wallet
    @PostMapping("/wallets/by-name/balance")
    public ResponseEntity<WalletBalanceResponse> getWalletBalanceByUserIdAndWalletName(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

        logger.info("💳 Admin requested balance for wallet='{}' (userId={})",
                request.getWalletName(), request.getUserId());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();

        userClient.getUserByIdInternal(userId);
        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, walletName);
        wallet.setMessage("Balance fetched successfully for wallet '" + walletName + "'");

        logger.info("✅ Wallet balance fetched: userId={}, wallet='{}', balance={}",
                userId, walletName, wallet.getBalance());
        return ResponseEntity.ok(wallet);
    }

    // ✅ Get Transaction History of Specific Wallet
    @PostMapping("/wallets/transactions")
    public ResponseEntity<Page<TransactionDTO>> getWalletTransactionHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletTransactionRequest request) {

        logger.info("📄 Admin requested transaction history for wallet='{}' (userId={}, page={}, size={})",
                request.getWalletName(), request.getUserId(), request.getPage(), request.getSize());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();
        int page = request.getPage();
        int size = request.getSize();

        userClient.getUserByIdInternal(userId);
        Page<TransactionDTO> transactions =
                walletService.getTransactionsByWalletName(userId, walletName, page, size);

        logger.info("✅ Wallet transaction history fetched: userId={}, wallet='{}', records={}",
                userId, walletName, transactions.getTotalElements());

        return ResponseEntity.ok(transactions);
    }

    // ✅ Blacklist Wallet
    @PutMapping("/wallets/blacklist")
    public ResponseEntity<WalletBlacklistResponse> blacklistWallet(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

        logger.warn("⚠️ Admin attempting to blacklist wallet='{}' for userId={}",
                request.getWalletName(), request.getUserId());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();

        userClient.getUserByIdInternal(userId);
        walletService.blacklistWalletByName(userId, walletName, authHeader);

        boolean allBlacklisted = walletService.areAllWalletsBlacklisted(userId);

        WalletBlacklistResponse response = new WalletBlacklistResponse(
                userId,
                walletName,
                true,
                allBlacklisted,
                allBlacklisted
                        ? "Wallet blacklisted. User also blacklisted because all wallets are blacklisted."
                        : "Wallet blacklisted. User is NOT blacklisted because other wallets are active."
        );

        logger.info("✅ Wallet '{}' blacklisted successfully for userId={}. All wallets blacklisted? {}",
                walletName, userId, allBlacklisted);

        return ResponseEntity.ok(response);
    }

    // ✅ Unblacklist All Wallets
    @PutMapping("/wallets/unblacklist")
    public ResponseEntity<walletUnblacklistResponse> unblacklistUserWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        logger.warn("♻️ Admin attempting to unblacklist all wallets for userId={}", request.getUserId());

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserByIdInternal(userId);

        int walletCount = walletService.unblacklistAllWallets(userId, authHeader);

        walletUnblacklistResponse response = new walletUnblacklistResponse(
                userId,
                walletCount,
                "User and all wallets unblocked successfully"
        );

        logger.info("✅ Successfully unblacklisted {} wallets for userId={}", walletCount, userId);
        return ResponseEntity.ok(response);
    }
}
