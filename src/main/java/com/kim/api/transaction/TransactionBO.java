package com.kim.api.transaction;

import com.kim.api.card.TransactionExternalBO;
import com.kim.api.core.CommonResponse;
import com.kim.api.core.CryptoUtils;
import com.kim.api.core.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 결제, 결제취소를 처리하고 카드사와 통신을 요청
 */
@Slf4j
@Transactional
@Service
public class TransactionBO {
    private static final Supplier<RuntimeException> noTransaction = () -> new RuntimeException("No transaction");
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

        // 부가가치세 자동계산
        if (transaction.getVat() == null) {
            transaction.setVat(transaction.getPayAmount().divide(new BigDecimal(11), 0, RoundingMode.HALF_UP));
        }

        if (transaction.getVat().compareTo(transaction.getPayAmount()) > 0) {
            throw new RuntimeException("VAT is greater than the pay amount");
        }

        // TODO transactionId 생성
        transaction.setTransactionId("12345678901234567890");

        // TODO 결제가 가능한 카드인지 체크 (Multi thread)

        String cardInfo = StringUtils.join(StringUtils.DEFAULT_SEPARATOR, transaction.getCardNumber(), transaction.getPeriod(), transaction.getCvc());
        transaction.setEncryptedCardInfo(CryptoUtils.encrypt(cardInfo));
        transaction.setRawData(transaction.toString());

        transactionExternalBO.payment(transaction);
        Transaction save = transactionRepository.save(transaction);
        return Transaction.Response.create(save, new CommonResponse("success", "Transaction success"));
    }

    /**
     * 결제정보조회
     */
    public Transaction.Data getCancelTransaction(String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        return Transaction.Data.create(transaction.orElseThrow(noTransaction), new CommonResponse("success", "Transaction is exists"));
    }

    /**
     * 결제취소(전체)
     */
    public Transaction.Response cancel(Transaction.Cancel cancel) {
        boolean cancelable = transactionRepository.countByOriginalTransactionId(cancel.getTransactionId()) == 0;
        if (!cancelable) {
            throw new RuntimeException("Already canceled");
        }

        if (cancel.getVat() != null && cancel.getVat().compareTo(cancel.getPayAmount()) > 0) {
            throw new RuntimeException("VAT is greater than the pay amount");
        }

        Transaction original = transactionRepository.findById(cancel.getTransactionId()).orElseThrow(noTransaction);
        if (original.getPayAmount().compareTo(cancel.getPayAmount()) != 0) {
            throw new RuntimeException("Cancel amount is not valid");
        }

        Transaction transaction = getCancelTransaction(cancel, original);

        transactionExternalBO.payment(transaction);
        Transaction save = transactionRepository.save(transaction);
        return Transaction.Response.create(save, new CommonResponse("success", "Transaction cancel success"));
    }

    private Transaction getCancelTransaction(Transaction.Cancel cancel, Transaction original) {
        Transaction transaction = new Transaction();
        transaction.setPayAmount(cancel.getPayAmount());
        transaction.setTransactionId("12345678901234567890"); // TODO 신규 번호 할당
        transaction.setOriginalTransactionId(cancel.getTransactionId());
        transaction.setTransactionType(cancel.getTransactionType());

        transaction.setVat(cancel.getVat() == null ? original.getVat() : cancel.getVat());
        transaction.setEncryptedCardInfo(original.getEncryptedCardInfo());
        transaction.setMonth(Transaction.Cancel.DEFAULT_MONTH);
        transaction.setRawData(transaction.toString());
        return transaction;
    }
}
