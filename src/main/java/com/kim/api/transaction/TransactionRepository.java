package com.kim.api.transaction;

import com.kim.api.core.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    int countByOriginalTransactionId(String originalTransactionId);

    List<Transaction> findByOriginalTransactionId(String originalTransactionId);

    Optional<Transaction> findByTransactionId(String transactionId);
}
