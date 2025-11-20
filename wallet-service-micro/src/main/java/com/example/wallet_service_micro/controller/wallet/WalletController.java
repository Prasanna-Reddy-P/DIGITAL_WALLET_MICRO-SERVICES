package com.example.wallet_service_micro.controller.wallet;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.walletRequest.WalletNameRequest;
import com.example.wallet_service_micro.dto.walletRequest.WalletTransactionRequest;
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
import com.example.wallet_service_micro.service.transactions.WalletTransactionService;
import com.example.wallet_service_micro.service.wallet.WalletService;
import com.example.wallet_service_micro.service.factory.WalletManagementService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final UserClient userClient;
    private final WalletService walletService;
    private final WalletManagementService walletManagementService;
    private final WalletTransactionService walletTransactionService;

    public WalletController(UserClient userClient,
                            WalletService walletService,
                            WalletManagementService walletManagementService,
                            WalletTransactionService walletTransactionService) {
        this.userClient = userClient;
        this.walletService = walletService;
        this.walletManagementService = walletManagementService;
        this.walletTransactionService = walletTransactionService;
    }

    // --------------------------------------------------------------------
    // ✅ Get balance of a specific wallet
    // --------------------------------------------------------------------
    @Operation(summary = "Get wallet balance", description = "Fetches balance for a specific wallet of the logged-in user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid wallet name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletNameRequest request) {

        String walletName = request.getWalletName();
        logger.info("💰 Request received: Fetching balance for wallet='{}'", walletName);

        UserDTO user = userClient.getUserFromToken(authHeader);
        logger.debug("🔑 Authenticated user: id={}, name={}, email={}", user.getId(), user.getName(), user.getEmail());

        Wallet wallet = walletManagementService.getExistingWallet(user, walletName);
        WalletBalanceResponse response = walletService.toWalletBalanceResponse(wallet);
        response.setMessage("Balance fetched successfully for wallet '" + walletName + "'");

        logger.info("✅ Wallet balance fetched for userId={}, wallet='{}', balance={}",
                user.getId(), walletName, response.getBalance());

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------
    // ✅ Get transactions for a wallet
    // --------------------------------------------------------------------
    @Operation(summary = "Get wallet transactions",
            description = "Returns paginated transaction history for a user's wallet.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Missing wallet name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody WalletTransactionRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        logger.info("📜 User requested transaction history: userId={}, wallet='{}', page={}, size={}",
                user.getId(), request.getWalletName(), request.getPage(), request.getSize());

        String walletName = request.getWalletName();
        int page = request.getPage();
        int size = request.getSize();

        if (walletName == null || walletName.isBlank()) {
            logger.warn("⚠️ Missing walletName in transaction history request from userId={}", user.getId());
            throw new IllegalArgumentException("walletName is required");
        }

        walletManagementService.getExistingWallet(user, walletName);

        Page<TransactionDTO> tx = walletTransactionService.getTransactionsByWallet(user, walletName, page, size);

        logger.info("✅ {} transactions fetched for userId={}, wallet='{}'",
                tx.getTotalElements(), user.getId(), walletName);

        return ResponseEntity.ok(tx);
    }

    // --------------------------------------------------------------------
    // ✅ Load money to wallet
    // --------------------------------------------------------------------
    @Operation(summary = "Load money", description = "Adds money to the specified wallet.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Money loaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/load")
    public ResponseEntity<LoadMoneyResponse> loadMoney(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody LoadMoneyRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        String txnId = UUID.randomUUID().toString();

        logger.info("💵 Load money request: userId={}, wallet='{}', amount={}, txnId={}",
                user.getId(), request.getWalletName(), request.getAmount(), txnId);

        LoadMoneyResponse response = walletService.loadMoney(
                user,
                request,
                txnId,
                request.getWalletName()
        );

        logger.info("✅ Money loaded successfully: userId={}, wallet='{}', newBalance={}, txnId={}",
                user.getId(), request.getWalletName(), response.getBalance(), txnId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --------------------------------------------------------------------
    // ✅ Transfer money to another user
    // --------------------------------------------------------------------
    @Operation(summary = "Transfer to another user", description = "Transfers money from sender to another user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer completed"),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody TransferRequest request) {

        UserDTO sender = userClient.getUserFromToken(authHeader);
        String transactionId = UUID.randomUUID().toString();

        logger.info("💸 Transfer initiated: senderId={}, receiverId={}, amount={}, senderWallet='{}', txnId={}",
                sender.getId(), request.getReceiverId(), request.getAmount(), request.getSenderWalletName(), transactionId);

        TransferResponse response = walletService.transferAmount(
                sender,
                request.getReceiverId(),
                request.getAmount(),
                transactionId,
                request.getSenderWalletName(),
                authHeader
        );

        logger.info("✅ Transfer completed: senderId={}, receiverId={}, amount={}, txnId={}, status={}",
                sender.getId(), request.getReceiverId(), request.getAmount(), transactionId, response.getMessage());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --------------------------------------------------------------------
    // ✅ Get all wallets for logged-in user
    // --------------------------------------------------------------------
    @Operation(summary = "Get all user wallets", description = "Lists all wallets owned by the logged-in user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallets fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-wallets")
    public ResponseEntity<List<WalletBalanceResponse>> getAllWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        logger.info("📂 Fetching all wallets for userId={}, email={}", user.getId(), user.getEmail());

        List<WalletBalanceResponse> wallets = walletService.getAllWalletsForUserDTO(user);

        logger.info("✅ {} wallets fetched for userId={}", wallets.size(), user.getId());
        return ResponseEntity.ok(wallets);
    }

    // --------------------------------------------------------------------
    // ✅ Internal transfer
    // --------------------------------------------------------------------
    @Operation(summary = "Internal wallet transfer", description = "Transfers money between user's own wallets.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Internal transfer successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/transfer/internal")
    public ResponseEntity<UserInternalTransferResponse> transferWithinWallets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody UserInternalTransferRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        String transactionId = UUID.randomUUID().toString();

        logger.info("🔄 Internal transfer initiated: userId={}, from='{}', to='{}', amount={}, txnId={}",
                user.getId(), request.getSenderWalletName(), request.getReceiverWalletName(), request.getAmount(), transactionId);

        UserInternalTransferResponse response = walletService.transferWithinUserWallets(
                user,
                request.getSenderWalletName(),
                request.getReceiverWalletName(),
                request.getAmount(),
                transactionId
        );

        logger.info("✅ Internal transfer successful: userId={}, from='{}', to='{}', amount={}, txnId={}",
                user.getId(), request.getSenderWalletName(), request.getReceiverWalletName(), request.getAmount(), transactionId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --------------------------------------------------------------------
    // ✅ Create wallet
    // --------------------------------------------------------------------
    @Operation(summary = "Create wallet", description = "Creates a new wallet for logged-in user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid wallet name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/create")
    public ResponseEntity<CreateWalletResponse> createWallet(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody CreateWalletRequest request) {

        UserDTO user = userClient.getUserFromToken(authHeader);
        logger.info("🆕 Create wallet request: userId={}, requestedName='{}'", user.getId(), request.getWalletName());

        if (request.getWalletName() == null || request.getWalletName().trim().isEmpty()) {
            logger.warn("⚠️ Wallet creation failed: empty wallet name provided by userId={}", user.getId());
            CreateWalletResponse errorResp = new CreateWalletResponse();
            errorResp.setMessage("Wallet name cannot be empty");
            return ResponseEntity.badRequest().body(errorResp);
        }

        CreateWalletResponse response =
                walletManagementService.createWallet(user, request.getWalletName());

        logger.info("✅ Wallet created successfully: userId={}, wallet='{}'", user.getId(), request.getWalletName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
