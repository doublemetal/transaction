package com.kim.api.card;

import com.kim.api.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 카드사와 통신
 */
@Slf4j
@Service
public class TransactionExternalBO {
    public void payment(Transaction transaction) {
        log.info("결제 성공");
    }
}
