package com.kim.api.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/transaction")
@RestController
public class TransactionApiController {
    private final TransactionBO transactionBO;

    public TransactionApiController(TransactionBO transactionBO) {
        this.transactionBO = transactionBO;
    }

    /**
     * 카드결제
     *
     * @return 결제 결과
     */
    @PostMapping("/payment")
    public Transaction.Response payment(@RequestBody Transaction.Request request) {
        return transactionBO.payment(request);
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
    public String getTransaction(@PathVariable String transactionId) {
        return "transaction";
    }
}
