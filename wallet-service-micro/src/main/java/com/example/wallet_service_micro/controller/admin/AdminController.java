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
        UserDTO admin = userClient.getUserFromToken(authHeader);
        if (admin == null)
            throw new UnauthorizedException("Unauthorized access");

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new ForbiddenException("Admins only");

        return admin;
    }

    // ✅ Get User Transaction History
    @PostMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getUserTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserTransactionRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        int page = request.getPage();
        int size = request.getSize();

        UserDTO user = userClient.getUserById(userId, authHeader);
        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        Page<TransactionDTO> transactions = walletService.getTransactions(user, page, size);
        return ResponseEntity.ok(transactions);
    }

    // ✅ Get Default Wallet Balance
    @PostMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalanceByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserById(userId, authHeader);

        WalletBalanceResponse response =
                walletService.getWalletByUserIdAndWalletName(userId, "Default");

        response.setMessage("Balance fetched successfully for userId=" + userId);
        return ResponseEntity.ok(response);
    }

    // ✅ Get All Wallets for a User
    @PostMapping("/wallets")
    public ResponseEntity<List<WalletBalanceResponse>> getAllWalletsByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserById(userId, authHeader);

        List<WalletBalanceResponse> wallets = walletService.getAllWalletsByUserId(userId);
        return ResponseEntity.ok(wallets);
    }

    // ✅ Get Balance of Specific Wallet
    @PostMapping("/wallets/by-name/balance")
    public ResponseEntity<WalletBalanceResponse> getWalletBalanceByUserIdAndWalletName(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();

        userClient.getUserById(userId, authHeader);
        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, walletName);
        wallet.setMessage("Balance fetched successfully for wallet '" + walletName + "'");
        return ResponseEntity.ok(wallet);
    }

    // ✅ Get Transaction History of Specific Wallet
    @PostMapping("/wallets/transactions")
    public ResponseEntity<Page<TransactionDTO>> getWalletTransactionHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletTransactionRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();
        int page = request.getPage();
        int size = request.getSize();

        userClient.getUserById(userId, authHeader);

        Page<TransactionDTO> transactions =
                walletService.getTransactionsByWalletName(userId, walletName, page, size);

        return ResponseEntity.ok(transactions);
    }


    // ✅ Blacklist Wallet
    @PutMapping("/wallets/blacklist")
    public ResponseEntity<WalletBlacklistResponse> blacklistWallet(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        String walletName = request.getWalletName();

        userClient.getUserById(userId, authHeader);
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

    @PutMapping("/wallets/unblacklist")
    public ResponseEntity<walletUnblacklistResponse> unblacklistUserWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody UserIdRequest request) {

        validateAdmin(authHeader);

        Long userId = request.getUserId();
        userClient.getUserById(userId, authHeader); // ensure user exists

        int walletCount = walletService.unblacklistAllWallets(userId, authHeader);

        walletUnblacklistResponse response = new walletUnblacklistResponse(
                userId,
                walletCount,
                "User and all wallets unblocked successfully"
        );

        return ResponseEntity.ok(response);
    }

}
