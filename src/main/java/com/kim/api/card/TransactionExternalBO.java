package com.kim.api.card;

import com.kim.api.core.model.transaction.Transaction;
import com.kim.api.transaction.enums.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 카드사와 통신
 */
@Slf4j
@Service
public class TransactionExternalBO {
    public void payment(Transaction transaction) {
        if (transaction.getTransactionType() == TransactionType.PAYMENT) {
            log.info("결제 성공");
        } else {
            log.info("결제취소 성공");
        }
    }
}
