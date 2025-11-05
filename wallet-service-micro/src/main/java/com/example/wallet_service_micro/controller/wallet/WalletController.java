package com.example.wallet_service_micro.controller.wallet;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.transferMoney.TransferRequest;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.service.factory.WalletFactory;
import jakarta.validation.Valid;
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
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        logger.info("Fetching wallet balance request");

        UserDTO user = userClient.getUserFromToken(authHeader);
        Wallet wallet = walletFactory.getOrCreateWallet(user);

        WalletBalanceResponse response = walletService.toWalletBalanceResponse(wallet);
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
    public ResponseEntity<LoadMoneyResponse> loadMoney(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody LoadMoneyRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        String txnId = UUID.randomUUID().toString();

        logger.info("💰 [API][LOAD] user={}, amount={}, txnId={}",
                user.getEmail(), request.getAmount(), txnId);

        LoadMoneyResponse response = walletService.loadMoney(user, request, txnId);
        return ResponseEntity.ok(response);
    }


    // --------------------------------------------------------------------
    // Transfer Money
    // --------------------------------------------------------------------
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody TransferRequest request) {

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
