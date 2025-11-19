package com.example.wallet_service_micro.repository.transaction;

import com.example.wallet_service_micro.model.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // -------------------------------
    // Pagination for transactions of a user
    // -------------------------------
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    // Pagination for transactions of a specific wallet
    Page<Transaction> findByUserIdAndWalletId(Long userId, Long walletId, Pageable pageable);

    // -------------------------------
    // Filter transactions by date range
    // -------------------------------
    // Custom JPQL query (uses entity field names, not table columns).
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.timestamp BETWEEN :start AND :end " +
            "ORDER BY t.timestamp DESC")
    List<Transaction> findByUserAndTimestampBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.walletName = :walletName " +
            "AND t.timestamp BETWEEN :start AND :end " +
            "ORDER BY t.timestamp DESC")
    List<Transaction> findByUserAndWalletNameAndTimestampBetween(
            @Param("userId") Long userId,
            @Param("walletName") String walletName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.walletId = :walletId " +
            "AND t.timestamp BETWEEN :start AND :end " +
            "ORDER BY t.timestamp DESC")
    List<Transaction> findByUserAndWalletAndTimestampBetween(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // -------------------------------
    // Duplicate transaction check
    // -------------------------------
    boolean existsByTransactionId(String transactionId);

    Page<Transaction> findByUserIdAndWalletName(Long userId, String walletName, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.walletName = :walletName")
    Page<Transaction> findTransactionsByUserAndWallet(
            @Param("userId") Long userId,
            @Param("walletName") String walletName,
            Pageable pageable
    );


    // ✅ Finds transactions belonging to a particular wallet
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);
}
