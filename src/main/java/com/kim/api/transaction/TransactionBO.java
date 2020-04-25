package com.kim.api.transaction;

import com.kim.api.core.TransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 결제, 결제취소를 처리하고 카드사와 통신을 요청
 */
@Slf4j
@Service
public class TransactionBO {

    /**
     * 요청 받은 정보로 결제를 진행합니다
     *
     * @param transaction 결제정보
     */
    public TransactionResponse payment(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        return response;
    }
}
