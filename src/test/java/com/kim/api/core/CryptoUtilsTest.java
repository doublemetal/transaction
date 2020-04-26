package com.kim.api.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CryptoUtilsTest {
    @Test
    public void cryptoTest() {
        String plainText = "1234123412341234";

        String encrypt = CryptoUtils.encrypt(plainText);
        log.info("encrypt: {}", encrypt);

        String decrypt = CryptoUtils.decrypt(encrypt);
        log.info("decrypt: {}", decrypt);

        assertTrue(!plainText.equals(encrypt));
        assertEquals(plainText, decrypt);
    }
}
