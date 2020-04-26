package com.kim.api.transaction;

import com.kim.api.core.CommonResponse;
import com.kim.api.transaction.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 결제 정보 모델
 */
@Getter
@Setter
public class Transaction {

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
        private BigDecimal payAmount; // 결제금액(100원 이상, 10억원 이하, 숫자)
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
