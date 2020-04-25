package com.kim.api.transaction;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 결제 정보 모델
 */
@Getter
@Setter
public class Transaction {
    private String transactionType; // 거래유형
    private String period; // 유효기간(4자리 숫자, mmyy)
    private int cvc; // cvc(3자리 숫자)
    private int month; // 할부개월수, 0(일시불), 1 ~ 12
    private BigDecimal payAmount; // 결제금액(100원 이상, 10억원 이하, 숫자)
}
