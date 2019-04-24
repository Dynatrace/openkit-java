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

package com.dynatrace.openkit.util.json;

import com.dynatrace.openkit.util.json.constants.JSONLiterals;
import com.dynatrace.openkit.util.json.lexer.JSONLexer;
import com.dynatrace.openkit.util.json.lexer.LexerException;
import com.dynatrace.openkit.util.json.objects.JSONBooleanValue;
import com.dynatrace.openkit.util.json.objects.JSONNullValue;
import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import com.dynatrace.openkit.util.json.parser.JSONParserState;
import com.dynatrace.openkit.util.json.parser.ParserException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONParserTest {

    private JSONLexer mockLexer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        mockLexer = mock(JSONLexer.class);
    }

    @Test
    public void aLexerExceptionIsCaughtAndRethrownAsParserException() throws LexerException, ParserException {
        // given
        LexerException lexerException = new LexerException("dummy");
        when(mockLexer.nextToken()).thenThrow(lexerException);
        JSONParser target = new JSONParser(mockLexer);

        expectedException.expect(ParserException.class);
        expectedException.expectMessage("Caught exception from lexical analysis");
        expectedException.expectCause(is(sameInstance(lexerException)));

        // when, then
        target.parse();
    }

    @Test
    public void aJSONParserInErroneousStateThrowsExceptionIfParseIsCalledAgain() throws LexerException, ParserException {
        // given
        LexerException lexerException = new LexerException("dummy");
        when(mockLexer.nextToken()).thenThrow(lexerException);
        JSONParser target = new JSONParser(mockLexer);

        // when parse is called the first time, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Caught exception from lexical analysis")));
            assertThat(e.getCause(), is(sameInstance((Throwable)lexerException)));
        }

        // and when called a second time, then
        expectedException.expect(ParserException.class);
        expectedException.expectMessage("JSON parser is in erroneous state");
        expectedException.expectCause(is(nullValue(Throwable.class)));
        target.parse();
    }

    @Test
    public void parsingAnEmptyJSONInputStringThrowsAnException() {
        // given
        JSONParser target = new JSONParser("");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("No JSON object could be decoded")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and the parser is also in error state
        assertThat(target.getState(), is(equalTo(JSONParserState.ERROR)));
    }

    @Test
    public void parsingNullLiteralValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser(JSONLiterals.NULL_LITERAL);

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONNullValue.class)));
    }

    @Test
    public void parsingBooleanFalseLiteralValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser(JSONLiterals.BOOLEAN_FALSE_LITERAL);

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)obtained).getValue(), is(false));
    }

    @Test
    public void parsingBooleanTrueLiteralValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser(JSONLiterals.BOOLEAN_TRUE_LITERAL);

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)obtained).getValue(), is(true));
    }

    @Test
    public void parsingStringValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("\"foobar\"");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONStringValue.class)));
        assertThat(((JSONStringValue)obtained).getValue(), is("foobar"));
    }

    @Test
    public void parsingIntegerNumberValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("1234567890");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)obtained).isInteger(), is(true));
        assertThat(((JSONNumberValue)obtained).getLongValue(), is(1234567890L));
    }

    @Test
    public void parsingFloatingPointNumberValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("5.125");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)obtained).isInteger(), is(false));
        assertThat(((JSONNumberValue)obtained).getDoubleValue(), is(5.125));
    }

    @Test
    public void callingParseSubsequentlyReturnsAlreadyParsedObject() throws ParserException {
        // given
        JSONParser target = new JSONParser(JSONLiterals.NULL_LITERAL);

        // when
        JSONValue obtainedOne = target.parse();
        JSONValue obtainedTwo = target.parse();

        // then
        assertThat(obtainedOne, is(sameInstance(obtainedTwo)));
    }

    @Test
    public void parsingMultipleJSONValuesFails() throws ParserException {
        // given
        expectedException.expect(ParserException.class);
        expectedException.expectMessage("Unexpected token \"JSONToken {tokenType=BOOLEAN, value=true}\" at end of input");
        expectedException.expectCause(is(nullValue(Throwable.class)));

        JSONParser target = new JSONParser(JSONLiterals.NULL_LITERAL + " " + JSONLiterals.BOOLEAN_TRUE_LITERAL);

        // when, then
        target.parse();
    }
}
