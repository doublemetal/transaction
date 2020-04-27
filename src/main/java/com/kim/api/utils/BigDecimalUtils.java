package com.kim.api.utils;

import java.math.BigDecimal;

/**
 * BigDecimal 처리를 편하게 하기 위한 Utils
 */
public class BigDecimalUtils {
    public static String getPlainString(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return BigDecimal.ZERO.toPlainString();
        }
        return bigDecimal.toPlainString();
    }
}
