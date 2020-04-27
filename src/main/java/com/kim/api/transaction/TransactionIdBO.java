package com.kim.api.transaction;

import com.kim.api.core.model.transaction.TransactionIdGenerator;
import com.kim.api.utils.DateUtils;
import com.kim.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Transaction Id 를 관리
 */
@Deprecated // TODO 멀티스레드 문제로, 시퀀스로 id 생성
@Slf4j
@Service
public class TransactionIdBO {
    private final TransactionIdRepository transactionIdRepository;

    public TransactionIdBO(TransactionIdRepository transactionIdRepository) {
        this.transactionIdRepository = transactionIdRepository;
    }

    /**
     * 오늘을 기준으로 id 를 생성한다
     *
     * @return transactionId
     */
    public String generateId() {
        TransactionIdGenerator trx = new TransactionIdGenerator("trx", 1L);
        Optional<TransactionIdGenerator> find = transactionIdRepository.findById(trx.getType());

        // TODO Remove 운영단계에서는 배치 등 다른 방법으로 처리 가능
        TransactionIdGenerator exists;
        if (find.isPresent()) {
            exists = find.get();
            if (exists.getSeq() >= 999999999999L) {
                exists.setSeq(0L);
            }

            // TODO 멀테스레드에 안전하게 개선
            exists.setSeq(exists.getSeq() + 1);
            transactionIdRepository.save(exists);
        } else {
            exists = transactionIdRepository.saveAndFlush(trx);
        }

        transactionIdRepository.saveAndFlush(exists);
        return DateUtils.getToday() + StringUtils.leftPad(String.valueOf(exists.getSeq()), 12, "0");
    }
}
