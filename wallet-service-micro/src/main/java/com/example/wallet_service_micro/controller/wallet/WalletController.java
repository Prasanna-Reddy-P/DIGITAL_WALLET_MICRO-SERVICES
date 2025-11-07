package com.example.wallet_service_micro.controller.wallet;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyRequest;
import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferRequest;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferResponse;
import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.dto.transferMoney.TransferRequest;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletRequest;
import com.example.wallet_service_micro.dto.walletCreation.CreateWalletResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.service.factory.WalletManagementService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final UserClient userClient;
    private final WalletService walletService;
    private final WalletManagementService walletManagementService;

    public WalletController(UserClient userClient,
                            WalletService walletService,
                            WalletManagementService walletManagementService) {
        this.userClient = userClient;
        this.walletService = walletService;
        this.walletManagementService = walletManagementService;
    }

    // --------------------------------------------------------------------
    // ✅ Get balance of a specific wallet (MUST already exist)
    // --------------------------------------------------------------------
    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam String walletName) {

        logger.info("Fetching balance for wallet: {}", walletName);

        UserDTO user = userClient.getUserFromToken(authHeader);

        // ✅ Ensure wallet exists (do NOT auto-create)
        Wallet wallet = walletManagementService.getExistingWallet(user, walletName);

        WalletBalanceResponse response = walletService.toWalletBalanceResponse(wallet);

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // ✅ Get all transactions
    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
// ✅ Get transactions for specific wallet of logged-in user
// --------------------------------------------------------------------
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(required = false) String walletName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserDTO user = userClient.getUserFromToken(authHeader);

        if (walletName == null)
            throw new IllegalArgumentException("walletName is required");

        // ✅ verify wallet exists
        walletManagementService.getExistingWallet(user, walletName);

        Page<TransactionDTO> tx =
                walletService.getTransactionsByWallet(user, walletName, page, size);

        return ResponseEntity.ok(tx);
    }



    // --------------------------------------------------------------------
    // ✅ Load money to wallet
    // --------------------------------------------------------------------
    @PostMapping("/load")
    public ResponseEntity<LoadMoneyResponse> loadMoney(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody LoadMoneyRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        String txnId = UUID.randomUUID().toString();

        LoadMoneyResponse response = walletService.loadMoney(
                user,
                request,
                txnId,
                request.getWalletName()
        );

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // ✅ Transfer money to another user
    // --------------------------------------------------------------------
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody TransferRequest request) {

        UserDTO sender = userClient.getUserFromToken(authHeader);
        String transactionId = UUID.randomUUID().toString();

        TransferResponse response = walletService.transferAmount(
                sender,
                request.getReceiverId(),
                request.getAmount(),
                transactionId,
                request.getSenderWalletName(),
                authHeader
        );

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // ✅ Get all wallets for logged-in user
    // --------------------------------------------------------------------
    @GetMapping("/my-wallets")
    public ResponseEntity<List<WalletBalanceResponse>> getAllWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        UserDTO user = userClient.getUserFromToken(authHeader);

        List<WalletBalanceResponse> wallets = walletService.getAllWalletsForUserDTO(user);

        return ResponseEntity.ok(wallets);
    }

    // --------------------------------------------------------------------
    // ✅ Internal Transfer (User's own wallets)
    // --------------------------------------------------------------------
    @PostMapping("/transfer/internal")
    public ResponseEntity<UserInternalTransferResponse> transferWithinWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody UserInternalTransferRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        String transactionId = UUID.randomUUID().toString();

        UserInternalTransferResponse response = walletService.transferWithinUserWallets(
                user,
                request.getSenderWalletName(),
                request.getReceiverWalletName(),
                request.getAmount(),
                transactionId
        );

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // ✅ Create wallet
    // --------------------------------------------------------------------
    @PostMapping("/create")
    public ResponseEntity<CreateWalletResponse> createWallet(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody CreateWalletRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);

        // ✅ Validate wallet name before calling service
        if (request.getWalletName() == null || request.getWalletName().trim().isEmpty()) {
            CreateWalletResponse errorResp = new CreateWalletResponse();
            errorResp.setMessage("Wallet name cannot be empty");
            return ResponseEntity.badRequest().body(errorResp);
        }

        // Call service only if valid
        CreateWalletResponse response =
                walletManagementService.createWallet(user, request.getWalletName());

        return ResponseEntity.ok(response);
    }

}
