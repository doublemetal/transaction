package com.kim.api.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kim.api.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class TransactionApiControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void payment() throws Exception {
        Transaction.Request transaction = new Transaction.Request();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setCvc("012");
        transaction.setMonth("00");
        transaction.setPeriod("1212");
        transaction.setPayAmount(new BigDecimal(100));

        mvc.perform(post("/api/transaction/payment")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.toString())
                .content(objectMapper.writeValueAsBytes(transaction))).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").exists());
    }
}
