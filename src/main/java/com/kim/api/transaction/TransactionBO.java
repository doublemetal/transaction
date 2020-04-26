package com.kim.api.transaction;

import com.kim.api.card.TransactionExternalBO;
import com.kim.api.core.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * 결제, 결제취소를 처리하고 카드사와 통신을 요청
 */
@Slf4j
@Transactional
@Service
public class TransactionBO {
    private final TransactionRepository transactionRepository;
    private final TransactionExternalBO transactionExternalBO;

    public TransactionBO(TransactionRepository transactionRepository, TransactionExternalBO transactionExternalBO) {
        this.transactionRepository = transactionRepository;
        this.transactionExternalBO = transactionExternalBO;
    }

    /**
     * 요청 받은 정보로 결제를 진행합니다
     *
     * @param request 결제요청정보
     */
    public Transaction.Response payment(Transaction.Request request) {
        Transaction transaction = Transaction.create(request);

        // TODO 결제가 가능한 카드인지 체크 (Multi thread)
        transactionExternalBO.payment(transaction);

        transaction.setEncryptedCardNumber(transaction.getCardNumber());
        // TODO 카드번호 암호화
        // TODO rawData 생성
        // TODO transactionId 생성
        Transaction save = transactionRepository.save(transaction);

        return Transaction.Response.create(save, new CommonResponse("success", "결제 성공"));
    }
}
