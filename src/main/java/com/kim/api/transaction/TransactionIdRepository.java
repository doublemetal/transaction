package com.kim.api.transaction;

import com.kim.api.core.model.transaction.TransactionIdGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionIdRepository extends JpaRepository<TransactionIdGenerator, String> {
}
