package com.example.wallet_service_micro.controller.admin;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.user.UserInfoResponse;
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

    // ✅ Get All Users (via user-service)
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        validateAdmin(authHeader);
        List<UserDTO> users = userClient.getAllUsers(authHeader);

        return ResponseEntity.ok(users);
    }

    // ✅ Get User Info + Default Wallet Balance
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        validateAdmin(authHeader);

        UserDTO user = userClient.getUserById(userId, authHeader);
        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, "Default");

        UserInfoResponse response =
                new UserInfoResponse(user.getName(), user.getEmail(), user.getRole(), wallet.getBalance());

        return ResponseEntity.ok(response);
    }

    // ✅ Get User Transaction History
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<Page<TransactionDTO>> getUserTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        validateAdmin(authHeader);

        UserDTO user = userClient.getUserById(userId, authHeader);
        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        Page<TransactionDTO> transactions = walletService.getTransactions(user, page, size);

        return ResponseEntity.ok(transactions);
    }

    // ✅ Get Default Wallet (DTO)
    @GetMapping("/users/{userId}/wallet")
    public ResponseEntity<WalletBalanceResponse> getWalletByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        validateAdmin(authHeader);
        userClient.getUserById(userId, authHeader);

        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, "Default");
        return ResponseEntity.ok(wallet);
    }

    // ✅ Get Default Wallet Balance
    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<WalletBalanceResponse> getBalanceByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        validateAdmin(authHeader);

        userClient.getUserById(userId, authHeader);
        WalletBalanceResponse response = walletService.getWalletByUserIdAndWalletName(userId, "Default");

        response.setMessage("Balance fetched successfully for userId=" + userId);
        return ResponseEntity.ok(response);
    }

    // ✅ Get All Wallets for a User (DTO)
    @GetMapping("/users/{userId}/wallets")
    public ResponseEntity<List<WalletBalanceResponse>> getAllWalletsByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        validateAdmin(authHeader);
        userClient.getUserById(userId, authHeader);

        List<WalletBalanceResponse> wallets = walletService.getAllWalletsByUserId(userId);

        return ResponseEntity.ok(wallets);
    }

    // ✅ Get Specific Wallet by Name (DTO)
    @GetMapping("/users/{userId}/wallets/by-name/{walletName}")
    public ResponseEntity<WalletBalanceResponse> getWalletByUserIdAndWalletName(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId,
            @PathVariable String walletName) {

        validateAdmin(authHeader);
        userClient.getUserById(userId, authHeader);

        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, walletName);

        return ResponseEntity.ok(wallet);
    }

    // ✅ Get Balance of Specific Wallet (DTO)
    @GetMapping("/users/{userId}/wallets/by-name/{walletName}/balance")
    public ResponseEntity<WalletBalanceResponse> getWalletBalanceByUserIdAndWalletName(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId,
            @PathVariable String walletName) {

        validateAdmin(authHeader);
        userClient.getUserById(userId, authHeader);

        WalletBalanceResponse wallet = walletService.getWalletByUserIdAndWalletName(userId, walletName);
        wallet.setMessage("Balance fetched successfully for wallet '" + walletName + "'");

        return ResponseEntity.ok(wallet);
    }
}
