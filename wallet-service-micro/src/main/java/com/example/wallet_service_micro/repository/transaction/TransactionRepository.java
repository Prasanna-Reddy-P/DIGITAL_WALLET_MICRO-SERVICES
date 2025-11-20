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

    // Transaction History of the user.
    Page<Transaction> findByUserId(Long userId, Pageable pageable);;


    // Transaction History of a respective wallet of a user in a time range.
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

    // Duplicate Transaction Check
    boolean existsByTransactionId(String transactionId);


    // Extract Transaction of a user and walletName with pagination
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.walletName = :walletName")
    Page<Transaction> findTransactionsByUserAndWallet(
            @Param("userId") Long userId,
            @Param("walletName") String walletName,
            Pageable pageable
    );


    // ✅ Finds transactions belonging to a particular wallet
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);
}
