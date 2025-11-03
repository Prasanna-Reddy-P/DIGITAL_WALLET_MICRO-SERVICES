package com.example.wallet_service_micro.controller;

import com.example.wallet_service_micro.client.UserClient;
import com.example.wallet_service_micro.dto.TransactionDTO;
import com.example.wallet_service_micro.dto.UserDTO;
import com.example.wallet_service_micro.dto.UserInfoResponse;
import com.example.wallet_service_micro.exception.ForbiddenException;
import com.example.wallet_service_micro.exception.UnauthorizedException;
import com.example.wallet_service_micro.exception.UserNotFoundException;
import com.example.wallet_service_micro.model.Wallet;
import com.example.wallet_service_micro.service.WalletService;
import com.example.wallet_service_micro.service.wallet.WalletFactory;

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
    private final WalletFactory walletFactory;
    private final UserClient userClient;

    public AdminController(WalletService walletService, WalletFactory walletFactory, UserClient userClient) {
        this.walletService = walletService;
        this.walletFactory = walletFactory;
        this.userClient = userClient;
    }

    // --------------------------------------------------------------------
    // ✅ GET ALL USERS (via user-service)
    // --------------------------------------------------------------------
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        logger.info("Received request: GET /admin/users");

        UserDTO admin = userClient.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        List<UserDTO> users = userClient.getAllUsers(authHeader);

        logger.info("Fetched {} users successfully", users.size());

        return ResponseEntity.ok(users);
    }

    // --------------------------------------------------------------------
    // ✅ GET USER DETAILS + WALLET INFO
    // --------------------------------------------------------------------
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        logger.info("Received request: GET /admin/users/{}", userId);

        UserDTO admin = userClient.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        UserDTO user = userClient.getUserById(userId, authHeader);

        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        Wallet wallet = walletFactory.getOrCreateWallet(user);
        UserInfoResponse response = new UserInfoResponse(user.getName(), user.getEmail(), user.getRole(), wallet.getBalance());

        logger.info("Fetched user {} successfully", userId);
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // ✅ GET USER TRANSACTIONS
    // --------------------------------------------------------------------
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<Page<TransactionDTO>> getUserTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Received request: GET /admin/users/{}/transactions?page={}&size={}", userId, page, size);

        UserDTO admin = userClient.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        UserDTO user = userClient.getUserById(userId, authHeader);

        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        Page<TransactionDTO> transactions = walletService.getTransactions(user, page, size);

        logger.info("Fetched {} transactions for user {}", transactions.getNumberOfElements(), userId);
        return ResponseEntity.ok(transactions);
    }

    // --------------------------------------------------------------------
    // ✅ GET WALLET DETAILS BY USER ID
    // --------------------------------------------------------------------
    @GetMapping("/users/{userId}/wallet")
    public ResponseEntity<Wallet> getWalletByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        logger.info("Received request: GET /admin/users/{}/wallet", userId);

        UserDTO admin = userClient.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        UserDTO user = userClient.getUserById(userId, authHeader);

        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        Wallet wallet = walletFactory.getOrCreateWallet(user);
        logger.info("Wallet fetched successfully for user {}", userId);

        return ResponseEntity.ok(wallet);
    }

    // --------------------------------------------------------------------
    // ✅ GET BALANCE BY USER ID
    // --------------------------------------------------------------------
    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<Double> getBalanceByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        logger.info("Received request: GET /admin/users/{}/balance", userId);

        UserDTO admin = userClient.getUserFromToken(authHeader);
        if (admin == null) throw new UnauthorizedException("Unauthorized access");
        if (!"ADMIN".equals(admin.getRole())) throw new ForbiddenException("Admins only");

        UserDTO user = userClient.getUserById(userId, authHeader);

        if (user == null) throw new UserNotFoundException("User not found with ID " + userId);

        Wallet wallet = walletFactory.getOrCreateWallet(user);
        logger.info("Balance fetched for user {}: {}", userId, wallet.getBalance());

        return ResponseEntity.ok(wallet.getBalance());
    }
}
