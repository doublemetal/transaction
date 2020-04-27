package com.kim.api.transaction;

import com.kim.api.core.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    int countByOriginalTransactionId(String originalTransactionId);
}
