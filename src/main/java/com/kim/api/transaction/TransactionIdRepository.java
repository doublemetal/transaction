package com.kim.api.transaction;

import com.kim.api.core.model.transaction.TransactionIdGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Deprecated // TODO 멀티스레드 문제로, 시퀀스로 id 생성
@Repository
public interface TransactionIdRepository extends JpaRepository<TransactionIdGenerator, String> {
}
