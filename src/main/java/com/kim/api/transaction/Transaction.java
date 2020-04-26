package com.kim.api.transaction;

import com.kim.api.core.CommonResponse;
import com.kim.api.transaction.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * 결제 정보 모델
 */
@Getter
@Setter
@Entity
public class Transaction {
    @Id
    @Column(length = 20)
    private String transactionId; // 거래시간 + Sequence
    @Column(length = 450)
    private String rawData; // 카드사에 전송하는 string 데이터
    @Column(length = 20)
    private String originalTransactionId; // 취소의 원거래번호
    @Column(length = 10)
    private TransactionType transactionType; // 거래유형

    @Column(length = 20)
    private String cardNumber; // 카드번호, 10 ~ 20
    @Column(length = 300)
    private String encryptedCardNumber; // 암호화한 카드번호, 300자

    @Column(length = 4)
    private String period; // 유효기간(4자리 숫자, mmyy)
    private int month; // 할부개월수, 0(일시불), 1 ~ 12, 취소는 일시불(00)으로 저장
    private int cvc; // cvc(3자리 숫자)

    private BigDecimal payAmount; // 거래금액(100원 이상, 10억원 이하, 숫자), 취소는 결제 금액보다 작아야함
    private BigDecimal vat; // 부가가치세, 거래금액보다 작아야함, 취소는 원거래와 취소의 부가가치세가 같아야함

    /**
     * 결제 Request
     */
    @Getter
    @Setter
    public static class Request {
        private TransactionType transactionType; // 거래유형
        private String period; // 유효기간(4자리 숫자, mmyy)
        private int cvc; // cvc(3자리 숫자)
        private int month; // 할부개월수, 0(일시불), 1 ~ 12
        private BigDecimal payAmount; // 거래금액(100원 이상, 10억원 이하, 숫자)
        private BigDecimal vat; // 부가가치세
    }

    /**
     * 결제 성공 Response
     */
    @Getter
    @Setter
    public static class Response extends CommonResponse {
        private String transactionId; // 거래번호
        private String rawData; // 카드사에 전달한 string 데이터(공통 헤더 + 데이터)
    }
}
