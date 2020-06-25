/**
 * Copyright 2018-2020 Dynatrace LLC
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONLexerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void lexingEmptyStringReturnsNullAsNextToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingStringWithWhitespacesOnlyReturnsNullAsNextToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer(" \r\n\t");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingStructuralCharacterBeginArrayGivesExpectedToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("[");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_SQUARE_BRACKET)));
        assertThat(obtained.getValue(), is(nullValue()));
    }

    @Test
    public void lexingStructuralCharacterEndArrayGivesExpectedToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("]");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_SQUARE_BRACKET)));
        assertThat(obtained.getValue(), is(nullValue()));
    }

    @Test
    public void lexingArrayTokensWithoutWhitespaceWorks() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("[]");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_SQUARE_BRACKET)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_SQUARE_BRACKET)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingArrayTokensWorksWithWhitespaces() throws LexerException {
        // given
        JSONLexer target = new JSONLexer(" \t[ \r\n]\t");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_SQUARE_BRACKET)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_SQUARE_BRACKET)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingStructuralCharacterBeginObjectGivesExpectedToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("{");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_BRACE)));
        assertThat(obtained.getValue(), is(nullValue()));
    }

    @Test
    public void lexingStructuralCharacterEndObjectGivesExpectedToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("}");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_BRACE)));
        assertThat(obtained.getValue(), is(nullValue()));
    }

    @Test
    public void lexingObjectTokensWithoutWhitespaceWorks() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("{}");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_BRACE)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_BRACE)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingObjectTokensWorksWithWhitespaces() throws LexerException {
        // given
        JSONLexer target = new JSONLexer(" \t{ \r\n}\t");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_BRACE)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_BRACE)));
        assertThat(obtained.getValue(), is(nullValue()));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingNameSeparatorTokenGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer(":");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COLON)));
        assertThat(obtained.getValue(), is(nullValue()));
    }

    @Test
    public void lexingValueSeparatorTokenGiveAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer(",");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COMMA)));
        assertThat(obtained.getValue(), is(nullValue()));
    }

    @Test
    public void lexingBooleanTrueLiteralGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("true");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_BOOLEAN)));
        assertThat(obtained.getValue(), is(equalTo("true")));
    }

    @Test
    public void lexingBooleanTrueLiteralWithLeadingAndTrailingWhitespaces() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\t true \t");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_BOOLEAN)));
        assertThat(obtained.getValue(), is(equalTo("true")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }


    @Test
    public void lexingBooleanTrueLiteralWithWrongCasingThrowsAnError() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("trUe");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Unexpected literal \"trUe\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingBooleanFalseLiteralGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("false");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_BOOLEAN)));
        assertThat(obtained.getValue(), is(equalTo("false")));
    }

    @Test
    public void lexingBooleanFalseLiteralWithLeadingAndTrailingWhitespaces() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\t false \t");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_BOOLEAN)));
        assertThat(obtained.getValue(), is(equalTo("false")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void lexingNullLiteralGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("null");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_NULL)));
        assertThat(obtained.getValue(), is(equalTo("null")));
    }

    @Test
    public void lexingNullLiteralWithLeadingAndTrailingWhitespacesGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\t\tnull\t\t");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_NULL)));
        assertThat(obtained.getValue(), is(equalTo("null")));
    }

    @Test
    public void lexingIntegerNumberGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("42");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("42")));
    }

    @Test
    public void lexingNegativeIntegerNumberGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("-42");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("-42")));
    }

    @Test
    public void lexingMinusSignWithoutSubsequentDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("-");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"-\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingIntegerNumberWithLeadingPlusThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("+42");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Unexpected literal \"+42\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithLeadingZeroThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("01234");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"01234\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithFractionPartGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("123.45");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("123.45")));
    }

    @Test
    public void lexingNegativeNumberWithFractionPartGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("-123.45");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("-123.45")));
    }

    @Test
    public void lexingNumberWithDecimalSeparatorAndNoSubsequentDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1234.");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"1234.\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithOnlyZerosInDecimalPartGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("123.00");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("123.00")));
    }

    @Test
    public void lexingNumberWithExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1e3");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1e3")));
    }

    @Test
    public void lexingNumberWithUpperCaseExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1E2");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1E2")));
    }

    @Test
    public void lexingNumberWithExplicitPositiveExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1e+5");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1e+5")));
    }

    @Test
    public void lexingNumberWithExplicitPositiveUpperCaseExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1E+5");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1E+5")));
    }

    @Test
    public void lexingNumberWithNegativeExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1e-2");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1e-2")));
    }

    @Test
    public void lexingNumberWithNegativeUpperCaseExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1E-2");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1E-2")));
    }

    @Test
    public void lexingNumberWithExponentAndNoDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1e");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"1e\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithUpperCaseExponentAndNoDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("2E");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"2E\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithExponentFollowedByPlusAndNoDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1e+");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"1e+\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithUpperCaseExponentFollowedByPlusAndNoDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("2E+");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"2E+\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithExponentFollowedByMinusAndNoDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1e-");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"1e-\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithUpperCaseExponentFollowedByMinusAndNoDigitsThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("2E-");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"2E-\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithFractionAndExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1.234e-2");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1.234e-2")));
    }

    @Test
    public void lexingNumberWithFractionAndUpperExponentGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1.25E-3");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1.25E-3")));
    }

    @Test
    public void lexingNumberWithDecimalSeparatorImmediatelyFollowedByExponentThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1.e-2");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"1.e-2\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingNumberWithDecimalSeparatorImmediatelyFollowedByUpperCaseExponentThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("1.E-5");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid number literal \"1.E-5\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"foobar\"");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("foobar")));
    }

    @Test
    public void lexingEmptyStringGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\"");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("")));
    }

    @Test
    public void lexingUnterminatedStringThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"foo");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Unterminated string literal \"foo\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringWithEscapedCharactersWorks() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0000\\u0001\\u0010\\n\\\"\\\\\\/\\b\\f\\n\\r\\t\"");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("\u0000\u0001\u0010\n\"\\/\b\f\n\r\t")));
    }

    @Test
    public void lexingStringWithInvalidEscapeSequenceThrowsException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"Hello \\a World!\'");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid escape sequence \"\\a\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingUnterminatedStringAfterEscapeSequenceThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"foo \\");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Unterminated string literal \"foo \"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringWithEscapedAsciiCharactersGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0048\\u0065\\u006c\\u006c\\u006f\\u0020\\u0057\\u006f\\u0072\\u006c\\u0064\\u0021\"");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("Hello World!")));
    }

    @Test
    public void lexingStringWithEscapedUpperCaseAsciiCharactersGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0048\\u0065\\u006C\\u006C\\u006F\\u0020\\u0057\\u006F\\u0072\\u006C\\u0064\\u0021\"");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("Hello World!")));
    }

    @Test
    public void lexingStringWithCharactersThatMustBeEscapedButAreNotThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\n\"");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid control character \"\\u000A\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringWithSurrogatePairGivesAppropriateToken() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0048\\u0065\\u006C\\u006C\\u006F\\u0020\\uD834\\uDD1E\\u0021\"");

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("Hello \ud834\udd1e!")));
    }

    @Test
    public void lexingStringWithHighSurrogateOnlyThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0048\\u0065\\u006C\\u006C\\u006F\\u0020\\uD834\\u0021\"");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid UTF-16 surrogate pair \"\\uD834\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringWithLowSurrogateOnlyThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0048\\u0065\\u006C\\u006C\\u006F\\u0020\\uDD1E\\u0021\"");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid UTF-16 surrogate pair \"\\uDD1E\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringWithNonHexCharacterInUnicodeEscapeSequenceThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u0048\\u0065\\u006C\\u006C\\u006F\\u0020\\uDDGE\\u0021\"");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid unicode escape sequence \"\\uDDG\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingStringWithTooShortUnicodeEscapeSequenceThrowsAnException() throws LexerException {
        // given
        JSONLexer target = new JSONLexer("\"\\u007\"");
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("Invalid unicode escape sequence \"\\u007\"\"");

        // when, then
        target.nextToken();
    }

    @Test
    public void lexingCompoundTokenStringGivesTokensInAppropriateOrder() throws LexerException {
        // given
        String json = "{\"asdf\": [1234.45e-3, \"a\", null, true, false] } ";
        JSONLexer target = new JSONLexer(json);

        // when
        JSONToken obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_BRACE)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("asdf")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COLON)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LEFT_SQUARE_BRACKET)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_NUMBER)));
        assertThat(obtained.getValue(), is(equalTo("1234.45e-3")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COMMA)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.VALUE_STRING)));
        assertThat(obtained.getValue(), is(equalTo("a")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COMMA)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_NULL)));
        assertThat(obtained.getValue(), is(equalTo("null")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COMMA)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_BOOLEAN)));
        assertThat(obtained.getValue(), is(equalTo("true")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.COMMA)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.LITERAL_BOOLEAN)));
        assertThat(obtained.getValue(), is(equalTo("false")));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_SQUARE_BRACKET)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getTokenType(), is(equalTo(JSONToken.TokenType.RIGHT_BRACE)));

        // and when
        obtained = target.nextToken();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void ioExceptionsAreCaughtAndTransformedToLexerExceptions() throws LexerException, IOException {
        // given
        Throwable t = new IOException("Does not work");
        Reader mockReader = mock(Reader.class);
        when(mockReader.read()).thenThrow(t);
        when(mockReader.read(Matchers.any(char[].class))).thenThrow(t);
        when(mockReader.read(Matchers.any(CharBuffer.class))).thenThrow(t);
        when(mockReader.read(Matchers.any(char[].class), Matchers.anyInt(), Matchers.anyInt())).thenThrow(t);

        expectedException.expect(LexerException.class);
        expectedException.expectMessage("IOException occurred");
        expectedException.expectCause(sameInstance(t));

        JSONLexer target = new JSONLexer(mockReader);

        // when, then
        target.nextToken();
    }

    @Test
    public void requestingNextTokenAfterIOExceptionHasBeenThrownThrowsAnException() throws LexerException, IOException {
        // given
        Throwable t = new IOException("Does not work");
        Reader mockReader = mock(Reader.class);
        when(mockReader.read()).thenThrow(t);
        when(mockReader.read(Matchers.any(char[].class))).thenThrow(t);
        when(mockReader.read(Matchers.any(CharBuffer.class))).thenThrow(t);
        when(mockReader.read(Matchers.any(char[].class), Matchers.anyInt(), Matchers.anyInt())).thenThrow(t);

        JSONLexer target = new JSONLexer(mockReader);

        // when requesting token first time, then
        try {
            target.nextToken();
            fail("Expected LexerException not thrown");
        } catch(LexerException e) {
            assertThat(e.getMessage(), is(equalTo("IOException occurred")));
            assertThat(e.getCause(), is(sameInstance(t)));
        }

        // and when requesting next token a second time, then
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("JSON Lexer is in erroneous state");
        target.nextToken();
    }

    @Test
    public void requestingNextTokenAfterLexerExceptionHasBeenThrownThrowsAnException() throws LexerException, IOException {
        // given
        JSONLexer target = new JSONLexer("1. 1.234");

        // when requesting token first time, then
        try {
            target.nextToken();
            fail("Expected LexerException not thrown");
        } catch(LexerException e) {
            assertThat(e.getMessage(), is(equalTo("Invalid number literal \"1.\"")));
        }

        // and when requesting next token a second time, then
        expectedException.expect(LexerException.class);
        expectedException.expectMessage("JSON Lexer is in erroneous state");
        target.nextToken();
    }
}
