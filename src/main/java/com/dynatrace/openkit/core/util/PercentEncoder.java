/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.util;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * Utility class for percent-encoding (also known as URL encoding) strings.
 *
 * <p>
 *     This class works basically the same as Java's {@code URLEncoder}, except that
 *     space characters are percent encoded and not using a plus.
 * </p>
 *
 * <p>
 *     Unlike Java's {@code URLEncoder} this class uses RFC 3986 to determine
 *     the unreserved characters(see also <a href="https://tools.ietf.org/html/rfc3986#section-2.3">https://tools.ietf.org/html/rfc3986#section-2.3</a>)
 * </p>
 */
public class PercentEncoder {

    private static final int UNRESERVED_CHARACTERS_BITS = 128; // US-ASCII range
    private static final BitSet UNRESERVED_CHARACTERS = new BitSet(UNRESERVED_CHARACTERS_BITS);

    static {
        // initialize all unreserved characters
        for (int i = 'a'; i <= 'z'; i++) {
            UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            UNRESERVED_CHARACTERS.set(i);
        }
        UNRESERVED_CHARACTERS.set('-');
        UNRESERVED_CHARACTERS.set('.');
        UNRESERVED_CHARACTERS.set('_');
        UNRESERVED_CHARACTERS.set('~');
    }

    /**
     * Default constructor.
     *
     * <p>
     *     This constructor is private, since this class shall be used as utility class.
     * </p>
     */
    private PercentEncoder() {
    }

    /**
     * Percent-encode a given input string.
     *
     * @param input The input string to percent-encode.
     * @param encoding Encoding used to encode characters.
     * @return Percent encoded string.
     */
    public static String encode(String input, String encoding) {
        return encode(input, encoding, null);
    }

    /**
     * Percent-encode a given input string.
     *
     * @param input The input string to percent-encode.
     * @param encoding Encoding used to encode characters.
     * @param additionalReservedChars Characters that should be unreserved, but need
     *                                to be considered reserved too.
     * @return Percent encoded string.
     */
    public static String encode(String input, String encoding, char[] additionalReservedChars) {

        BitSet unreservedSet = buildUnreservedCharacters(additionalReservedChars);
        StringBuilder resultBuilder = new StringBuilder(input.length());

        int index = 0;
        while (index < input.length()) {
            int c = input.charAt(index);
            if (unreservedSet.get(c)) {
                // unreserved character, which does need to be percent encoded
                resultBuilder.append((char)c);
                index++;
            } else {
                // reserved character, but encoding needs to be applied first
                StringBuilder sb = new StringBuilder().append((char)c);
                index++;
                while (index < input.length() && !unreservedSet.get(input.charAt(index))) {
                    sb.append(input.charAt(index));
                    index++;
                }

                // encode temp string using given encoding; & percent encoding
                try {
                    byte[] encoded = sb.toString().getBytes(encoding);
                    // now perform percent encoding
                    for (byte b : encoded) {
                        resultBuilder.append(hexEncode(b));
                    }
                } catch (UnsupportedEncodingException e) {
                    // should not be reached
                    return null;
                }
            }
        }

        return resultBuilder.toString();
    }

    private static char[] hexEncode(byte b) {
        char[] result = new char[3];
        result[0] = '%';
        char c = Character.forDigit((b >> 4) & 0x0F, 16);
        if (Character.isLetter(c)) {
            c = Character.toUpperCase(c);
        }
        result[1] = c;
        c = Character.forDigit(b & 0x0F, 16);
        if (Character.isLetter(c)) {
            c = Character.toUpperCase(c);
        }
        result[2] = c;

        return result;
    }

    private static BitSet buildUnreservedCharacters(char[] additionalReservedChars) {
        BitSet unreservedSet = UNRESERVED_CHARACTERS;
        if (additionalReservedChars != null && additionalReservedChars.length > 0) {
            // duplicate the
            unreservedSet = new BitSet(UNRESERVED_CHARACTERS_BITS);
            unreservedSet.or(UNRESERVED_CHARACTERS);
            for (char c : additionalReservedChars) {
                if (c < UNRESERVED_CHARACTERS_BITS) {
                    unreservedSet.clear(c);
                }
            }
        }

        return unreservedSet;
    }
}
