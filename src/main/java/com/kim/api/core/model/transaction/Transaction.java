package com.kim.api.core.model.transaction;

import com.kim.api.core.CommonResponse;
import com.kim.api.transaction.enums.TransactionType;
import com.kim.api.utils.BigDecimalUtils;
import com.kim.api.utils.CryptoUtils;
import com.kim.api.utils.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 결제 정보 모델
 */
@Getter
@Setter
@Table(name = "trx")
@Entity
public class Transaction {
    private static final String BLANK = "_";
    private static final String NUMBER_BLANK = "0";

    @Id
    @Column(name = "trx_id", length = 20)
    private String transactionId; // 거래시간 + Sequence
    @Column(length = 450)
    private String rawData; // 카드사에 전송하는 string 데이터
    @Column(name = "origin_trx_id", length = 20)
    private String originalTransactionId; // 취소의 원거래번호
    @Column(name = "trx_type", length = 10)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // 거래유형

    @Transient
    private String cardNumber; // 카드번호, 10 ~ 20
    @Transient
    private String period; // 유효기간(4자리 숫자, mmyy)
    @Transient
    private String cvc; // cvc(3자리 숫자)
    @Column(length = 300)
    private String encryptedCardInfo; // 암호화한 카드 정보, 300자

    @Column(length = 2)
    private String month; // 할부개월수, 00(일시불), 1 ~ 12, 취소는 일시불(00)으로 저장

    private BigDecimal payAmount; // 거래금액(100원 이상, 10억원 이하, 숫자), 취소는 결제 금액보다 작아야함
    private BigDecimal vat; // 부가가치세, 거래금액보다 작아야함, 취소는 원거래와 취소의 부가가치세가 같아야함

    public static Transaction create(Request request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType(request.getTransactionType());

        transaction.setCardNumber(request.getCardNumber());
        transaction.setPeriod(request.getPeriod());
        transaction.setCvc(request.getCvc());
        transaction.setMonth(request.getMonth());

        transaction.setPayAmount(request.getPayAmount());
        transaction.setVat(request.getVat());
        return transaction;
    }

    public String toString() {
        String data = StringUtils.rightPad(transactionType.name(), 10, BLANK)
                + StringUtils.rightPad(transactionId, 20, BLANK)
                + StringUtils.rightPad(cardNumber, 20, BLANK)
                + StringUtils.leftPad(month, 2, NUMBER_BLANK)
                + StringUtils.rightPad(period, 4, BLANK)
                + StringUtils.rightPad(cvc, 3, BLANK)
                + StringUtils.leftPad(BigDecimalUtils.getPlainString(payAmount), 10, BLANK)
                + StringUtils.leftPad(BigDecimalUtils.getPlainString(vat), 10, NUMBER_BLANK)
                + StringUtils.rightPad(StringUtils.defaultIfEmpty(originalTransactionId, BLANK), 20, BLANK)
                + StringUtils.rightPad(encryptedCardInfo, 300, BLANK)
                + StringUtils.rightPad(BLANK, 47, BLANK);
        int totalLength = data.length();
        return StringUtils.leftPad(String.valueOf(totalLength), 4, BLANK) + data;
    }

    /**
     * 결제 Request
     */
    @Getter
    @Setter
    public static class Request {
        @NotNull
        private TransactionType transactionType; // 거래유형
        @NotNull
        @Length(min = 10, max = 16)
        private String cardNumber;
        @NotNull
        @Length(min = 4, max = 4)
        private String period; // 유효기간(4자리 숫자, mmyy)
        @NotNull
        @Length(min = 3, max = 3)
        private String cvc; // cvc(3자리 숫자)
        @NotNull
        @Length(min = 2, max = 2)
        private String month; // 할부개월수, 00(일시불), 1 ~ 12
        @NotNull
        @Min(100)
        @Max(1000000000)
        private BigDecimal payAmount; // 거래금액(100원 이상, 10억원 이하, 숫자)
        private BigDecimal vat; // 부가가치세
    }

    /**
     * 결제/취소 성공 Response
     */
    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response extends CommonResponse {
        private String transactionId; // 거래번호
        private String rawData; // 카드사에 전달한 string 데이터(공통 헤더 + 데이터)

        public Response(CommonResponse response) {
            this.result = response.getResult();
            this.message = response.getMessage();
        }

        public static Response create(Transaction transaction, CommonResponse commonResponse) {
            Transaction.Response response = new Transaction.Response(commonResponse);
            response.setTransactionId(transaction.getTransactionId());
            response.setRawData(transaction.getRawData());
            return response;
        }
    }

    /**
     * 결제취소 Request
     */
    @Getter
    @Setter
    public static class Cancel {
        public static final String DEFAULT_MONTH = "00";

        @NotNull
        private TransactionType transactionType = TransactionType.CANCEL;
        private String transactionId;

        @NotNull
        private BigDecimal payAmount;
        private BigDecimal vat;
    }

    /**
     * 데이터 조회 Response
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Data extends CommonResponse {
        private String transactionId;
        private String cardNumber;
        private String period;
        private String cvc;
        private TransactionType transactionType;
        private BigDecimal payAmount;
        private BigDecimal vat;


        public Data(CommonResponse response) {
            this.result = response.getResult();
            this.message = response.getMessage();
        }

        public static Data create(Transaction transaction, CommonResponse commonResponse) {
            Transaction.Data data = new Transaction.Data(commonResponse);
            data.setTransactionId(transaction.getTransactionId());

            String[] cardInfos = CryptoUtils.decrypt(transaction.getEncryptedCardInfo()).split(StringUtils.DEFAULT_SEPARATOR);

            data.setCardNumber(cardInfos[0]);
            data.setPeriod(cardInfos[1]);
            data.setCvc(cardInfos[2]);
            data.setTransactionType(transaction.getTransactionType());
            data.setPayAmount(transaction.getPayAmount());
            data.setVat(transaction.getVat());
            return data;
        }
    }
}
