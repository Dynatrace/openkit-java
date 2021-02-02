/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.util.json.lexer;

import com.dynatrace.openkit.util.json.constants.JSONLiterals;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class JSONTokenTest {

    @Test
    public void tokenTypesOfPredefinedTokensAreCorrect() {
        // when boolean true token, then
        assertThat(JSONToken.BOOLEAN_TRUE_TOKEN.getTokenType(), is(JSONToken.TokenType.LITERAL_BOOLEAN));
        // when boolean false token, then
        assertThat(JSONToken.BOOLEAN_FALSE_TOKEN.getTokenType(), is(JSONToken.TokenType.LITERAL_BOOLEAN));
        // when null token, then
        assertThat(JSONToken.NULL_TOKEN.getTokenType(), is(JSONToken.TokenType.LITERAL_NULL));
        // when left braces token, then
        assertThat(JSONToken.LEFT_BRACE_TOKEN.getTokenType(), is(JSONToken.TokenType.LEFT_BRACE));
        // when right braces token, then
        assertThat(JSONToken.RIGHT_BRACE_TOKEN.getTokenType(), is(JSONToken.TokenType.RIGHT_BRACE));
        // when left square bracket token, then
        assertThat(JSONToken.LEFT_SQUARE_BRACKET_TOKEN.getTokenType(), is(JSONToken.TokenType.LEFT_SQUARE_BRACKET));
        // when right square bracket token, then
        assertThat(JSONToken.RIGHT_SQUARE_BRACKET_TOKEN.getTokenType(), is(JSONToken.TokenType.RIGHT_SQUARE_BRACKET));
        // when comma token, then
        assertThat(JSONToken.COMMA_TOKEN.getTokenType(), is(JSONToken.TokenType.COMMA));
        // when colon token, then
        assertThat(JSONToken.COLON_TOKEN.getTokenType(), is(JSONToken.TokenType.COLON));
    }

    @Test
    public void tokenValuesOfPredefinedTokensAreCorrect() {
        // when boolean true token, then
        assertThat(JSONToken.BOOLEAN_TRUE_TOKEN.getValue(), is(JSONLiterals.BOOLEAN_TRUE_LITERAL));
        // when boolean false token, then
        assertThat(JSONToken.BOOLEAN_FALSE_TOKEN.getValue(), is(JSONLiterals.BOOLEAN_FALSE_LITERAL));
        // when null token, then
        assertThat(JSONToken.NULL_TOKEN.getValue(), is(JSONLiterals.NULL_LITERAL));
        // when left braces token, then
        assertThat(JSONToken.LEFT_BRACE_TOKEN.getValue(), is(nullValue()));
        // when right braces token, then
        assertThat(JSONToken.RIGHT_BRACE_TOKEN.getValue(), is(nullValue()));
        // when left square bracket token, then
        assertThat(JSONToken.LEFT_SQUARE_BRACKET_TOKEN.getValue(), is(nullValue()));
        // when right square bracket token, then
        assertThat(JSONToken.RIGHT_SQUARE_BRACKET_TOKEN.getValue(), is(nullValue()));
        // when comma token, then
        assertThat(JSONToken.COMMA_TOKEN.getValue(), is(nullValue()));
        // when colon token, then
        assertThat(JSONToken.COLON_TOKEN.getValue(), is(nullValue()));
    }

    @Test
    public void createStringTokenReturnsAppropriateJSONToken() {
        // when
        JSONToken obtained = JSONToken.createStringToken("foobar");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(JSONToken.TokenType.VALUE_STRING));
        assertThat(obtained.getValue(), is(equalTo("foobar")));
    }

    @Test
    public void createNumberTokenReturnsAppropriateJSONToken() {
        // when
        JSONToken obtained = JSONToken.createNumberToken("12345");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(JSONToken.TokenType.VALUE_NUMBER));
        assertThat(obtained.getValue(), is(equalTo("12345")));
    }

    @Test
    public void tokenTypeToStringReturnsAppropriateStringRepresentations() {

        // when called with number, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.VALUE_NUMBER), is(equalTo("NUMBER")));
        // when called with string, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.VALUE_STRING), is(equalTo("STRING")));
        // when called with boolean, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.LITERAL_BOOLEAN), is(equalTo("BOOLEAN")));
        // when called with null, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.LITERAL_NULL), is(equalTo(JSONLiterals.NULL_LITERAL)));
        // when called with left brace, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.LEFT_BRACE), is(equalTo("{")));
        // when called with right brace, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.RIGHT_BRACE), is(equalTo("}")));
        // when called with left square bracket, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.LEFT_SQUARE_BRACKET), is(equalTo("[")));
        // when called with right square bracket, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.RIGHT_SQUARE_BRACKET), is(equalTo("]")));
        // when called with comma, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.COMMA), is(equalTo(",")));
        // when called with colon, then
        assertThat(JSONToken.tokenTypeToString(JSONToken.TokenType.COLON), is(equalTo(":")));
    }

    @Test
    public void toStringForTokenWithoutValueGivesAppropriateStringRepresentation() {
        // given a token that does not store a value
        JSONToken target = JSONToken.COLON_TOKEN;

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is("JSONToken {tokenType=:, value=null}"));
    }

    @Test
    public void toStringForTokenWithValueGivesAppropriateStringRepresentation() {
        // given
        JSONToken target = JSONToken.createNumberToken("12345");

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is("JSONToken {tokenType=NUMBER, value=12345}"));
    }
}
