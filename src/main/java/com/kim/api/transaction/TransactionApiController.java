package com.kim.api.transaction;

import com.kim.api.core.model.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public Transaction.Response payment(@Valid @RequestBody Transaction.Request request) {
        return transactionBO.payment(request);
    }

    /**
     * 결제취소
     *
     * @return 결제취소 결과
     */
    @PostMapping("/cancel")
    public Transaction.Response cancel(@Valid @RequestBody Transaction.Cancel cancel) {
        return transactionBO.cancel(cancel);
    }

    /**
     * 부분취소
     *
     * @return 취소 결과
     */
    @PostMapping("/cancel-partial")
    public Transaction.Response cancelPartial(@Valid @RequestBody Transaction.Cancel cancel) {
        return transactionBO.cancelPartial(cancel);
    }

    /**
     * 결제정보 조회
     *
     * @return 결제 정보
     */
    @GetMapping("/{transactionId}")
    public Transaction.Data getTransaction(@PathVariable String transactionId) {
        return transactionBO.getTransaction(transactionId);
    }
}
