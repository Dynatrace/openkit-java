/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.util.json.lexer;

import com.dynatrace.openkit.util.json.constants.JSONLiterals;

/**
 * Container class representing a token.
 */
public final class JSONToken {

    /**
     * Token's type.
     */
    public enum TokenType {
        VALUE_NUMBER,         // numeric value, the value is the number text
        VALUE_STRING,         // string value, value does not include leading and trailing " characters
        LITERAL_BOOLEAN,      // boolean literal, value is either true or false
        LITERAL_NULL,         // null literal, value will be null (as string)
        LEFT_BRACE,           // {
        RIGHT_BRACE,          // }
        LEFT_SQUARE_BRACKET,  // [
        RIGHT_SQUARE_BRACKET, // ]
        COMMA,                // ,
        COLON                 // :
    }

    /** Type of this token */
    private final TokenType tokenType;
    /** Token value for primitive tokens */
    private final String value;

    /**
     * Construct the token with type only and set value to {@code null}.
     *
     * @param tokenType Type of this token.
     */
    JSONToken(TokenType tokenType) {
        this(tokenType, null);
    }

    /**
     * Construct the token with type and value.
     *
     * @param tokenType Type of this token.
     * @param value Value of this token, if it has a value.
     */
    JSONToken(TokenType tokenType, String value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    /**
     * Get the type of the token.
     *
     * @return Token's type.
     */
    public TokenType getTokenType() {
        return tokenType;
    }

    /**
     * Get token value as string.
     *
     * <p>
     *     This method is only relevant for number, boolean and string tokens.
     * </p>
     *
     * @return Token value as string.
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "JSONToken {" + "tokenType=" + tokenTypeToString(tokenType) + ", value=" + value + "}";
    }

    /**
     * Get string representation of given {@link TokenType}.
     *
     * @param tokenType The {@link TokenType} for which to obtain string representation.
     * @return String representation of tokenType
     */
    static String tokenTypeToString(TokenType tokenType) {
        switch (tokenType) {
            case VALUE_NUMBER:
                return "NUMBER";
            case VALUE_STRING:
                return "STRING";
            case LITERAL_BOOLEAN:
                return "BOOLEAN";
            case LITERAL_NULL:
                return JSONLiterals.NULL_LITERAL;
            case LEFT_BRACE:
                return "{";
            case RIGHT_BRACE:
                return "}";
            case LEFT_SQUARE_BRACKET:
                return "[";
            case RIGHT_SQUARE_BRACKET:
                return "]";
            case COMMA:
                return ",";
            case COLON:
                return ":";
            default:
                throw new IllegalStateException("Unknown token type " + tokenType);
        }
    }
}
