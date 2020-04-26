package com.kim.api.transaction;

import com.kim.api.card.TransactionExternalBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 결제, 결제취소를 처리하고 카드사와 통신을 요청
 */
@Slf4j
@Service
public class TransactionBO {
    private final TransactionExternalBO transactionExternalBO;

    public TransactionBO(TransactionExternalBO transactionExternalBO) {
        this.transactionExternalBO = transactionExternalBO;
    }

    /**
     * 요청 받은 정보로 결제를 진행합니다
     *
     * @param transaction 결제정보
     */
    public Transaction.Response payment(Transaction transaction) {
        Transaction.Response response = new Transaction.Response();

        response.setTransactionId("2020042600000000001");
        response.setRawData("enums");

        return response;
    }
}
