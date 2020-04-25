package com.kim.api.core;

import lombok.Getter;
import lombok.Setter;

/**
 * 결제 성공 Response
 */
@Getter
@Setter
public class TransactionResponse extends CommonResponse {
    private String transactionId; // 거래번호
    private String rawData; // 카드사에 전달한 string 데이터(공통 헤더 + 데이터)
}
