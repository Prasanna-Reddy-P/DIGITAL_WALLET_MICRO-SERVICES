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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/admin")
@Tag(name = "Admin Wallet Operations", description = "Admin-only secured wallet APIs")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final WalletService walletService;
    private final UserClient userClient;

    public AdminController(WalletService walletService, UserClient userClient) {
        this.walletService = walletService;
        this.userClient = userClient;
    }

    // ------------------------ Admin Validation (Internal) ------------------------
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

    // ------------------------ Get User Transaction History ------------------------
    @Operation(
            summary = "Get user transaction history",
            description = "Fetch paginated transactions of a specific user (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Transaction history fetched successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden — Admins only", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
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

        Page<TransactionDTO> transactions = walletService.getTransactions(user, page, size);

        return ResponseEntity.ok(transactions);
    }

    // ------------------------ Get Default Wallet Balance ------------------------
    @Operation(
            summary = "Get default wallet balance",
            description = "Fetch balance of the user's default wallet (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Balance fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @PostMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalanceByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserByIdInternal(userId);

        WalletBalanceResponse response = walletService.getWalletByUserIdAndWalletName(userId, "Default");
        response.setMessage("Balance fetched successfully for userId=" + userId);

        return ResponseEntity.ok(response);
    }

    // ------------------------ Get All Wallets ------------------------
    @Operation(
            summary = "Get all wallets for a user",
            description = "Fetch all wallet balances for a user (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Wallets fetched successfully")
    @PostMapping("/wallets")
    public ResponseEntity<List<WalletBalanceResponse>> getAllWalletsByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserByIdInternal(userId);

        List<WalletBalanceResponse> wallets = walletService.getAllWalletsByUserId(userId);

        return ResponseEntity.ok(wallets);
    }

    // ------------------------ Get Wallet Balance by Name ------------------------
    @Operation(
            summary = "Get wallet balance by name",
            description = "Fetch balance of a specific wallet by name (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Balance fetched successfully")
    @PostMapping("/wallets/by-name/balance")
    public ResponseEntity<WalletBalanceResponse> getWalletBalanceByUserIdAndWalletName(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();

        userClient.getUserByIdInternal(userId);
        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, walletName);
        wallet.setMessage("Balance fetched successfully for wallet '" + walletName + "'");

        return ResponseEntity.ok(wallet);
    }

    // ------------------------ Get Wallet Transaction History ------------------------
    @Operation(
            summary = "Get transaction history of a specific wallet",
            description = "Fetch paginated transactions for a specific wallet (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Transactions fetched successfully")
    @PostMapping("/wallets/transactions")
    public ResponseEntity<Page<TransactionDTO>> getWalletTransactionHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletTransactionRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();

        userClient.getUserByIdInternal(userId);

        Page<TransactionDTO> transactions =
                walletService.getTransactionsByWalletName(userId, walletName, request.getPage(), request.getSize());

        return ResponseEntity.ok(transactions);
    }

    // ------------------------ Blacklist Wallet ------------------------
    @Operation(
            summary = "Blacklist a wallet",
            description = "Block a specific wallet of a user (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Wallet blacklisted successfully")
    @PutMapping("/wallets/blacklist")
    public ResponseEntity<WalletBlacklistResponse> blacklistWallet(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

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

        return ResponseEntity.ok(response);
    }

    // ------------------------ Unblacklist Wallets ------------------------
    @Operation(
            summary = "Unblacklist all wallets for a user",
            description = "Removes blacklist status from all wallets and the user (ADMIN only)"
    )
    @ApiResponse(responseCode = "200", description = "Wallets unblacklisted successfully")
    @PutMapping("/wallets/unblacklist")
    public ResponseEntity<walletUnblacklistResponse> unblacklistUserWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserByIdInternal(userId);

        int walletCount = walletService.unblacklistAllWallets(userId, authHeader);

        walletUnblacklistResponse response = new walletUnblacklistResponse(
                userId,
                walletCount,
                "User and all wallets unblocked successfully"
        );

        return ResponseEntity.ok(response);
    }
}
