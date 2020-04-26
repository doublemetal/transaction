package com.kim.api.core;

/**
 * Apache common lang3 을 기반으로한 StringUtils
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static final String DEFAULT_SEPARATOR = ",";

    public static String join(String separator, String... elements) {
        return org.apache.commons.lang3.StringUtils.join(elements, separator);
    }
}
