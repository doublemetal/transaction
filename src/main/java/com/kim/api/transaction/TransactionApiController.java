package com.kim.api.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/transaction")
@RestController
public class TransactionApiController {

    /**
     * 카드결제
     *
     * @return 결제 결과
     */
    @PostMapping("/payment")
    public String payment() {
        return "payment";
    }

    /**
     * 결제취소
     *
     * @return 결제취소 결과
     */
    @GetMapping("/{transactionId}/cancel")
    public String cancel(@PathVariable String transactionId) {
        return "cancel";
    }

    /**
     * 결제정보 조회
     *
     * @return 결제 정보
     */
    @GetMapping("/{transactionId}")
    public String getTransactionId(@PathVariable String transactionId) {
        return "payment";
    }
}
