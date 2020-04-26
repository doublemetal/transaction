package com.kim.api.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private Transaction.Request getTestTransaction() {
        Transaction.Request transaction = new Transaction.Request();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setCvc("012");
        transaction.setMonth("00");
        transaction.setPeriod("1212");
        transaction.setCardNumber("1234567890123456");
        transaction.setPayAmount(new BigDecimal(100));
        return transaction;
    }

    @Test
    void payment() throws Exception {
        mvc.perform(post("/api/transaction/payment")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(getTestTransaction())))
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

        mvc.perform(post("/api/transaction/payment")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(transaction))).andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("fail"));
    }

    @Test
    void paymentSearch() throws Exception {
        Transaction.Request request = getTestTransaction();

        MvcResult mvcResult = mvc.perform(post("/api/transaction/payment")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

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
        Transaction.Request request = getTestTransaction();

        MvcResult mvcResult = mvc.perform(post("/api/transaction/payment")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        Transaction.Response response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Transaction.Response.class);

        Transaction.Cancel cancel = new Transaction.Cancel();
        cancel.setTransactionId(response.getTransactionId());
        cancel.setPayAmount(request.getPayAmount());
        cancel.setVat(request.getVat());

        mvc.perform(post("/api/transaction/cancel")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsString(cancel)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value(cancel.getTransactionId()))
                .andExpect(jsonPath("$.rawData").exists());
    }
}
