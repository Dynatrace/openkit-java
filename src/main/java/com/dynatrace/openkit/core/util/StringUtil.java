package com.dynatrace.openkit.core.util;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * Generates a 64 bit hash from the given string.
     *
     * @param stringValue the value to be hashed
     * @return the 64 bit hash of the given string ({@code 0} in case the given string is {@code null}) or empty.
     */
    public static long to64BitHash(String stringValue) {
        if(stringValue == null) {
            return 0;
        }

        long hash = 0;

        for (int i = 0; i < stringValue.length(); i++) {
            hash = 31 * hash + stringValue.charAt(i);
        }
        return hash;
    }
}
