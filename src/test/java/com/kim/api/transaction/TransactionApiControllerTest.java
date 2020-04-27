package com.kim.api.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kim.api.core.model.transaction.Transaction;
import com.kim.api.transaction.enums.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class TransactionApiControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Transaction.Request getTestTransaction(int amount, Integer vat) {
        Transaction.Request transaction = new Transaction.Request();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setCvc("012");
        transaction.setMonth("00");
        transaction.setPeriod("1212");
        transaction.setCardNumber("1234567890123456");
        transaction.setPayAmount(new BigDecimal(amount));
        transaction.setVat(new BigDecimal(vat));
        return transaction;
    }

    @Test
    void payment() throws Exception {
        callPayment(getTestTransaction(11000, 1000))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.rawData").exists());
    }

    @Test
    void payment_400error() throws Exception {
        Transaction.Request transaction = new Transaction.Request();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setCardNumber("1");

        callPayment(transaction)
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.result").value("fail"));
    }

    @Test
    void paymentSearch() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        mvc.perform(get("/api/transaction/" + response.getTransactionId())
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value(response.getTransactionId()))
                .andExpect(jsonPath("$.cvc").value(request.getCvc()));
    }

    @Test
    void cancel() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);

        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        Transaction.Cancel cancel = new Transaction.Cancel();
        cancel.setTransactionId(response.getTransactionId());
        cancel.setPayAmount(request.getPayAmount());
        cancel.setVat(request.getVat());

        callCancel(cancel)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.rawData").exists());
    }

    @Test
    void cancel_retry() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        Transaction.Cancel cancel = new Transaction.Cancel();
        cancel.setTransactionId(response.getTransactionId());
        cancel.setPayAmount(request.getPayAmount());
        cancel.setVat(request.getVat());

        callCancel(cancel);

        // 같은 데이터로 취소 재시도
        callCancel(cancel).andDo(print()).andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("Already canceled"));
    }

    @Test
    void cancel_payAmount_notValid() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        Transaction.Cancel cancel = new Transaction.Cancel();
        cancel.setTransactionId(response.getTransactionId());
        cancel.setPayAmount(request.getPayAmount().add(new BigDecimal(500)));
        cancel.setVat(request.getVat());

        callCancel(cancel);

        // 결제금액보다 크게 취소
        callCancel(cancel).andDo(print()).andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("Cancel amount is not valid"));
    }

    @Test
    void cancel_vat_greaterThenPayAmount() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        Transaction.Cancel cancel = new Transaction.Cancel();
        cancel.setTransactionId(response.getTransactionId());
        cancel.setPayAmount(request.getPayAmount());
        cancel.setVat(request.getPayAmount().add(new BigDecimal(500)));

        callCancel(cancel);

        // 결제금액보다 큰 VAT 로 취소
        callCancel(cancel).andDo(print()).andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("VAT is greater than the pay amount"));
    }

    private ResultActions callPayment(Transaction.Request request) throws Exception {
        return mvc.perform(post("/api/transaction/payment")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions callCancel(Transaction.Cancel cancel) throws Exception {
        return mvc.perform(post("/api/transaction/cancel")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(cancel)));
    }

    private Transaction.Cancel getPartialCancel(Transaction.Response response, int amount, Integer vat) {
        Transaction.Cancel cancel = new Transaction.Cancel();
        cancel.setTransactionId(response.getTransactionId());
        cancel.setPayAmount(new BigDecimal(amount));
        cancel.setVat(new BigDecimal(vat));
        return cancel;
    }

    @Test
    void cancel_partial1() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        callCancel(getPartialCancel(response, 1100, 100)).andExpect(status().isOk());
        callCancel(getPartialCancel(response, 3300, null)).andExpect(status().isOk());
        callCancel(getPartialCancel(response, 7000, null)).andExpect(status().is5xxServerError());
        callCancel(getPartialCancel(response, 6600, 700)).andExpect(status().is5xxServerError());
        callCancel(getPartialCancel(response, 6600, 600)).andExpect(status().isOk());
        callCancel(getPartialCancel(response, 100, null)).andExpect(status().is5xxServerError());
    }

    @Test
    void cancel_partial2() throws Exception {
        Transaction.Request request = getTestTransaction(20000, 909);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        callCancel(getPartialCancel(response, 10000, 0)).andExpect(status().isOk());
        callCancel(getPartialCancel(response, 10000, 0)).andExpect(status().is5xxServerError());
        callCancel(getPartialCancel(response, 10000, 909)).andExpect(status().isOk());
    }

    @Test
    void cancel_partial3() throws Exception {
        Transaction.Request request = getTestTransaction(11000, 1000);
        MvcResult mvcResult = callPayment(request).andReturn();
        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        callCancel(getPartialCancel(response, 20000, null)).andExpect(status().isOk());
        callCancel(getPartialCancel(response, 10000, 1000)).andExpect(status().isOk());
        callCancel(getPartialCancel(response, 10000, 909)).andExpect(status().is5xxServerError());
        callCancel(getPartialCancel(response, 10000, null)).andExpect(status().isOk());
    }
}
