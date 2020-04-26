package com.kim.api.core;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@Slf4j
public class CryptoUtils {
    private static SecretKey key;

    static {
        try {
            key = KeyGenerator.getInstance("AES").generateKey();
        } catch (Exception e) {
            log.error("SecretKey creation failure", e);
        }
    }

    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new BASE64Encoder().encode(cipher.doFinal(plainText.getBytes()));
        } catch (Exception e) {
            log.error("Encryption failure", e);
        }

        return "";
    }

    public static String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] bytes = new BASE64Decoder().decodeBuffer(cipherText);
            return new String(cipher.doFinal(bytes));
        } catch (Exception e) {
            log.error("Decryption failure", e);
        }

        return "";
    }
}
