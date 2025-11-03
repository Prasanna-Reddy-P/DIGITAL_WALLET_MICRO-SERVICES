package com.example.wallet_service_micro.controller;

import com.example.wallet_service_micro.client.UserClient;
import com.example.wallet_service_micro.dto.*;
import com.example.wallet_service_micro.model.Wallet;
import com.example.wallet_service_micro.service.WalletService;
import com.example.wallet_service_micro.service.wallet.WalletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final UserClient userClient;
    private final WalletService walletService;
    private final WalletFactory walletFactory;

    public WalletController(UserClient userClient, WalletService walletService, WalletFactory walletFactory) {
        this.userClient = userClient;
        this.walletService = walletService;
        this.walletFactory = walletFactory;
    }

    // --------------------------------------------------------------------
    // Get Wallet Balance
    // --------------------------------------------------------------------
    @GetMapping("/balance")
    public ResponseEntity<LoadMoneyResponse> getBalance(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        logger.info("Fetching wallet balance request");

        // ✅ Fetch user info from user-service-micro
        UserDTO user = userClient.getUserFromToken(authHeader);

        Wallet wallet = walletFactory.getOrCreateWallet(user);
        LoadMoneyResponse response = walletService.toLoadMoneyResponse(wallet);

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // Get All Transactions
    // --------------------------------------------------------------------
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Fetching transactions (page={}, size={})", page, size);

        UserDTO user = userClient.getUserFromToken(authHeader);
        Page<TransactionDTO> transactions = walletService.getTransactions(user, page, size);

        return ResponseEntity.ok(transactions);
    }

    // --------------------------------------------------------------------
    // Load Money
    // --------------------------------------------------------------------
    @PostMapping("/load")
    public ResponseEntity<LoadMoneyResponse> loadMoney(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                       @RequestBody TransferRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        String txnId = UUID.randomUUID().toString();

        logger.info("Load request: user={}, amount={}, txnId={}", user.getEmail(), request.getAmount(), txnId);

        LoadMoneyResponse response = walletService.loadMoney(user, request.getAmount(), txnId);
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // Transfer Money
    // --------------------------------------------------------------------
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody TransferRequest request) {

        // ✅ Get sender user info from the token using UserClient
        UserDTO sender = userClient.getUserFromToken(authHeader);

        String transactionId = UUID.randomUUID().toString();

        TransferResponse response = walletService.transferAmount(
                sender,
                request.getReceiverId(),
                request.getAmount(),
                transactionId,
                authHeader
        );

        return ResponseEntity.ok(response);
    }


}
