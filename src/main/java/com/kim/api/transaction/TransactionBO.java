package com.kim.api.transaction;

import com.kim.api.card.TransactionExternalBO;
import com.kim.api.core.CommonResponse;
import com.kim.api.core.model.transaction.Transaction;
import com.kim.api.utils.BigDecimalUtils;
import com.kim.api.utils.CryptoUtils;
import com.kim.api.utils.DateUtils;
import com.kim.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 결제, 결제취소를 처리하고 카드사와 통신을 요청
 */
@Slf4j
@Transactional
@Service
public class TransactionBO {
    private static final int TRANSACTION_ID_SEQUENCE_LENGTH = 12;
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
        Transaction data = Transaction.create(request);

        // 부가가치세 자동계산
        if (data.getVat() == null) {
            data.setVat(data.getPayAmount().divide(new BigDecimal(11), 0, RoundingMode.HALF_UP));
        }

        if (data.getVat().compareTo(data.getPayAmount()) > 0) {
            throw new RuntimeException("VAT is greater than the pay amount");
        }

        // TODO 결제가 가능한 카드인지 체크 (Multi thread)

        String cardInfo = StringUtils.join(StringUtils.DEFAULT_SEPARATOR, data.getCardNumber(), data.getPeriod(), data.getCvc());
        data.setEncryptedCardInfo(CryptoUtils.encrypt(cardInfo));
        data.setRawData(data.toString());

        Transaction transaction = transactionRepository.save(data);
        transaction.setTransactionId(generateId(transaction.getSequence()));

        transactionRepository.save(transaction);
        transactionExternalBO.payment(data);
        return Transaction.Response.create(transaction, new CommonResponse("success", "Transaction success"));
    }

    /**
     * 결제정보조회
     */
    public Transaction.Data getTransaction(String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findByTransactionId(transactionId);
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

        Transaction original = transactionRepository.findByTransactionId(cancel.getTransactionId()).orElseThrow(noTransaction);
        if (original.getPayAmount().compareTo(cancel.getPayAmount()) != 0) {
            throw new RuntimeException("Cancel amount is not valid");
        }

        Transaction data = getCancelTransaction(cancel, original);

        Transaction transaction = transactionRepository.save(data);
        transaction.setTransactionId(generateId(transaction.getSequence()));

        transactionRepository.save(transaction);
        transactionExternalBO.payment(data);
        return Transaction.Response.create(transaction, new CommonResponse("success", "Transaction cancel success"));
    }

    // 부가가치세가 없는 경우는 결제의 부가가치세로 취소
    private Transaction getCancelTransaction(Transaction.Cancel cancel, Transaction original) {
        Transaction transaction = new Transaction();
        transaction.setPayAmount(cancel.getPayAmount());
        transaction.setOriginalTransactionId(cancel.getTransactionId());
        transaction.setTransactionType(cancel.getTransactionType());

        transaction.setVat(cancel.getVat() == null ? cancel.getPayAmount().divide(new BigDecimal(11), 0, RoundingMode.HALF_UP) : cancel.getVat());
        transaction.setEncryptedCardInfo(original.getEncryptedCardInfo());
        transaction.setMonth(Transaction.Cancel.DEFAULT_MONTH);

        String[] cardInfos = CryptoUtils.decrypt(transaction.getEncryptedCardInfo()).split(StringUtils.DEFAULT_SEPARATOR);
        transaction.setCardNumber(cardInfos[0]);
        transaction.setPeriod(cardInfos[1]);
        transaction.setCvc(cardInfos[2]);

        transaction.setRawData(transaction.toString());
        return transaction;
    }

    private String generateId(long sequence) {
        return DateUtils.getToday() + StringUtils.leftPad(StringUtils
                .substring(String.valueOf(sequence), 0, TRANSACTION_ID_SEQUENCE_LENGTH), TRANSACTION_ID_SEQUENCE_LENGTH, "0");
    }

    /**
     * 부분취소
     */
    public Transaction.Response cancelPartial(Transaction.Cancel cancel) {
        if (cancel.getVat() != null && cancel.getVat().compareTo(cancel.getPayAmount()) > 0) {
            throw new RuntimeException("VAT is greater than the pay amount");
        }

        Transaction original = transactionRepository.findByTransactionId(cancel.getTransactionId()).orElseThrow(noTransaction);
        Transaction data = getCancelTransaction(cancel, original);

        List<Transaction> canceledList = transactionRepository.findByOriginalTransactionId(data.getOriginalTransactionId());
        BigDecimal totalCancelAmount = canceledList.stream().map(Transaction::getPayAmount).reduce(BigDecimal.ZERO, BigDecimalUtils::add);
        BigDecimal totalVat = canceledList.stream().map(Transaction::getVat).reduce(BigDecimal.ZERO, BigDecimalUtils::add);

        if (BigDecimalUtils.add(totalCancelAmount, data.getPayAmount()).compareTo(original.getPayAmount()) > 0) {
            throw new RuntimeException("The cancel amount is greater than the remaining pay amount");
        }

        if (BigDecimalUtils.add(totalVat, data.getVat()).compareTo(original.getVat()) > 0) {
            if (cancel.getVat() == null) {
                data.setVat(original.getVat().subtract(totalVat)); // 취소가 가능한데, VAT 가 남은 금액보다 더 큰 경우이고 자동계산이면, 남은 VAT 으로 차감한다
            } else {
                throw new RuntimeException("The cancel vat is greater than the remaining pay vat");
            }
        }

        if (original.getVat().subtract(BigDecimalUtils.add(totalVat, data.getVat()))
                .compareTo(original.getPayAmount().subtract(BigDecimalUtils.add(totalCancelAmount, data.getPayAmount()))) > 0) {
            throw new RuntimeException("VAT is greater than the pay amount");
        }

        Transaction transaction = transactionRepository.save(data);
        transaction.setTransactionId(generateId(transaction.getSequence()));
        transaction.setRawData(transaction.toString());

        transactionRepository.save(transaction);
        transactionExternalBO.payment(transaction);
        return Transaction.Response.create(transaction, new CommonResponse("success", "Transaction cancel success"));
    }
}
