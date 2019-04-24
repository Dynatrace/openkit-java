/**
 * Copyright 2018-2019 Dynatrace LLC
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JSONTokenTest {

    @Test
    public void tokenTypeSetInConstructorCanBeRetrievedAgain() {
        // given
        JSONToken target = new JSONToken(JSONToken.TokenType.LITERAL_NULL, "null");

        // then
        assertThat(target.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_NULL)));
    }

    @Test
    public void tokenValueSetInConstructorCanBeRetrievedAgain() {
        // given
        JSONToken target = new JSONToken(JSONToken.TokenType.LITERAL_NULL, "null");

        // then
        assertThat(target.getValue(), is(equalTo("null")));
    }

    @Test
    public void constructorWithTokenTypeSetsValueToNull() {
        // given
        JSONToken target = new JSONToken(JSONToken.TokenType.LEFT_BRACE);

        // then
        assertThat(target.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_BRACE)));
        assertThat(target.getValue(), is(nullValue()));
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
        // given
        JSONToken target = new JSONToken(JSONToken.TokenType.COLON);

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is("JSONToken {tokenType=:, value=null}"));
    }

    @Test
    public void toStringForTokenWithValueGivesAppropriateStringRepresentation() {
        // given
        JSONToken target = new JSONToken(JSONToken.TokenType.VALUE_NUMBER, "12345");

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is("JSONToken {tokenType=NUMBER, value=12345}"));
    }
}
