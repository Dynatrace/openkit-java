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
import com.dynatrace.openkit.util.json.objects.JSONArrayValue;
import com.dynatrace.openkit.util.json.objects.JSONBooleanValue;
import com.dynatrace.openkit.util.json.objects.JSONNullValue;
import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONObjectValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import com.dynatrace.openkit.util.json.parser.JSONParserState;
import com.dynatrace.openkit.util.json.parser.ParserException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

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
        // ensure that the parser is in erroneous state
        assertThat(target.getState(), is(JSONParserState.ERROR));

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

        // and the parser is in end state
        assertThat(target.getState(), is(JSONParserState.END));
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

        // and the parser is in end state
        assertThat(target.getState(), is(JSONParserState.END));
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

        // and the parser is in end state
        assertThat(target.getState(), is(JSONParserState.END));
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

        // and the parser is in end state
        assertThat(target.getState(), is(JSONParserState.END));
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

        // and the parser is in end state
        assertThat(target.getState(), is(JSONParserState.END));
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

        // and the parser is in end state
        assertThat(target.getState(), is(JSONParserState.END));
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
    public void parsingMultipleJSONValuesFails() {
        // given
        JSONParser target = new JSONParser(JSONLiterals.NULL_LITERAL + " " + JSONLiterals.BOOLEAN_TRUE_LITERAL);

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=BOOLEAN, value=true}\" at end of input")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingRightSquareBracketAsFirstTokenThrowsAnException() {
        // given
        JSONParser target = new JSONParser("]");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=], value=null}\" at start of input")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingRightBraceAsFirstTokenThrowsAnException() {
        // given
        JSONParser target = new JSONParser("}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=}, value=null}\" at start of input")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingCommaAsFirstTokenThrowsAnException() {
        // given
        JSONParser target = new JSONParser(",");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=,, value=null}\" at start of input")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingColonAsFirstTokenThrowsAnException() {
        // given
        JSONParser target = new JSONParser(":");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=:, value=null}\" at start of input")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingUnterminatedEmptyArrayThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON array")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingEmptyArrayWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(0));
    }

    @Test
    public void parsingArrayWithSingleNullValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[" + JSONLiterals.NULL_LITERAL + "]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONNullValue.class)));
    }

    @Test
    public void parsingArrayWithSingleTrueValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[" + JSONLiterals.BOOLEAN_TRUE_LITERAL + "]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(0)).getValue(), is(true));
    }

    @Test
    public void parsingArrayWithSingleFalseValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[" + JSONLiterals.BOOLEAN_FALSE_LITERAL + "]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(0)).getValue(), is(false));
    }

    @Test
    public void parsingArrayWithSingleNumberValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[" + Math.PI + "]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)((JSONArrayValue)obtained).get(0)).getDoubleValue(), is(Math.PI));
    }

    @Test
    public void parsingArrayWithSingleStringValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[\"foobar\"]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONStringValue.class)));
        assertThat(((JSONStringValue)((JSONArrayValue)obtained).get(0)).getValue(), is("foobar"));
    }

    @Test
    public void parsingArrayWithMultipleSimpleValuesWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[" + JSONLiterals.NULL_LITERAL
            + ", " + JSONLiterals.BOOLEAN_TRUE_LITERAL
            + ", " + JSONLiterals.BOOLEAN_FALSE_LITERAL
            + ", " + Math.E
            + ", \"Hello World!\"]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(5));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONNullValue.class)));
        assertThat(((JSONArrayValue)obtained).get(1), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(1), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(1)).getValue(), is(true));
        assertThat(((JSONArrayValue)obtained).get(2), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(2), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(2)).getValue(), is(false));
        assertThat(((JSONArrayValue)obtained).get(3), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(3), is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)((JSONArrayValue)obtained).get(3)).getDoubleValue(), is(Math.E));
        assertThat(((JSONArrayValue)obtained).get(4), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(4), is(instanceOf(JSONStringValue.class)));
        assertThat(((JSONStringValue)((JSONArrayValue)obtained).get(4)).getValue(), is(equalTo("Hello World!")));
    }

    @Test
    public void parsingCommaRightAfterArrayStartThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[,");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=,, value=null}\" at beginning of array")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingColonRightAfterArrayStartThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[:");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=:, value=null}\" at beginning of array")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingUnterminatedArrayAfterValueThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[42");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON array")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingUnterminatedArrayAfterValueDelimiterThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[42, ");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON array")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingMultipleArrayValuesWithoutDelimiterThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[42 45]");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=NUMBER, value=45}\" in array after value has been parsed")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingValueDelimiterAsLastArrayTokenThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[42, 45,]");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=], value=null}\" in array after delimiter")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingNestedEmptyArrayWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[[]]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)((JSONArrayValue)obtained).get(0)).size(), is(0));
    }

    @Test
    public void parsingMultipleNestedEmptyArraysWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[[], []]");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(2));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)((JSONArrayValue)obtained).get(0)).size(), is(0));
        assertThat(((JSONArrayValue)obtained).get(1), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(1), is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)((JSONArrayValue)obtained).get(1)).size(), is(0));
    }

    @Test
    public void parsingNestedArrayWithValuesWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[[" + JSONLiterals.NULL_LITERAL
            + ", " + JSONLiterals.BOOLEAN_TRUE_LITERAL
            + ", " + JSONLiterals.BOOLEAN_FALSE_LITERAL
            + ", " + Math.E
            + ", \"Hello World!\"]]");

        // when
        JSONValue obtained = target.parse();

        // then ensure that there is a nested array
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(1));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONArrayValue.class)));
        obtained = ((JSONArrayValue)obtained).get(0);

        // then ensure that the nested array contains the values
        assertThat(((JSONArrayValue)obtained).size(), is(5));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONNullValue.class)));
        assertThat(((JSONArrayValue)obtained).get(1), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(1), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(1)).getValue(), is(true));
        assertThat(((JSONArrayValue)obtained).get(2), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(2), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(2)).getValue(), is(false));
        assertThat(((JSONArrayValue)obtained).get(3), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(3), is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)((JSONArrayValue)obtained).get(3)).getDoubleValue(), is(Math.E));
        assertThat(((JSONArrayValue)obtained).get(4), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(4), is(instanceOf(JSONStringValue.class)));
        assertThat(((JSONStringValue)((JSONArrayValue)obtained).get(4)).getValue(), is(equalTo("Hello World!")));
    }

    @Test
    public void parsingMultipleNestedArrayWithValuesWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("[" + JSONLiterals.NULL_LITERAL
            + ", [" + JSONLiterals.BOOLEAN_TRUE_LITERAL
            + ", [" + JSONLiterals.BOOLEAN_FALSE_LITERAL
            + ", " + Math.E
            + ", \"Hello World!\"]]]");

        // when
        JSONValue obtained = target.parse();

        // then ensure the values of the outermost array
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONArrayValue.class)));
        assertThat(((JSONArrayValue)obtained).size(), is(2));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONNullValue.class)));
        assertThat(((JSONArrayValue)obtained).get(1), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(1), is(instanceOf(JSONArrayValue.class)));
        obtained = ((JSONArrayValue)obtained).get(1);

        // then ensure the values of the next nested array
        assertThat(((JSONArrayValue)obtained).size(), is(2));
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(0)).getValue(), is(true));
        assertThat(((JSONArrayValue)obtained).get(1), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(1), is(instanceOf(JSONArrayValue.class)));
        obtained = ((JSONArrayValue)obtained).get(1);

        // then ensure values of innermost array
        assertThat(((JSONArrayValue)obtained).get(0), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(0), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONArrayValue)obtained).get(0)).getValue(), is(false));
        assertThat(((JSONArrayValue)obtained).get(1), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(1), is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)((JSONArrayValue)obtained).get(1)).getDoubleValue(), is(Math.E));
        assertThat(((JSONArrayValue)obtained).get(2), is(notNullValue()));
        assertThat(((JSONArrayValue)obtained).get(2), is(instanceOf(JSONStringValue.class)));
        assertThat(((JSONStringValue)((JSONArrayValue)obtained).get(2)).getValue(), is(equalTo("Hello World!")));
    }

    @Test
    public void parsingUnterminatedNestedArrayThrowsAnException() {
        // given
        JSONParser target = new JSONParser("[[" + JSONLiterals.NULL_LITERAL
            + ", " + JSONLiterals.BOOLEAN_TRUE_LITERAL
            + ", " + JSONLiterals.BOOLEAN_FALSE_LITERAL
            + ", " + Math.E
            + ", \"Hello World!\"]");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");} catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON array")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingUnterminatedEmptyObjectThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON object")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingEmptyObjectWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("{}");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONObjectValue.class)));
        assertThat(((JSONObjectValue)obtained).size(), is(0));
    }

    @Test
    public void parsingObjectWithNullLiteralAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{null: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected object key token \"JSONToken {tokenType=null, value=null}\"")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithBooleanTrueLiteralAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{true: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected object key token \"JSONToken {tokenType=BOOLEAN, value=true}\"")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithBooleanFalseLiteralAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{false: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected object key token \"JSONToken {tokenType=BOOLEAN, value=false}\"")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithNumberAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{42: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected object key token \"JSONToken {tokenType=NUMBER, value=42}\"")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithArrayAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{[]: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected object key token \"JSONToken {tokenType=[, value=null}\"")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithObjectAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{{}: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected object key token \"JSONToken {tokenType={, value=null}\"")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithoutKeyTokenThrowsThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{: \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=:, value=null}\" encountered - key expected")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithCommaAsKeyThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{, \"foo\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=,, value=null}\" encountered - key expected")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithValidKeyButNoKeyValueDelimiterThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{\"foo\" \"bar\"}");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch(ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unexpected token \"JSONToken {tokenType=STRING, value=bar}\" encountered - key-value delimiter expected")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingObjectWithStringKeyAndNullValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("{\"bar\": null}");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONObjectValue.class)));
        assertThat(((JSONObjectValue)obtained).keySet(), is(equalTo(Collections.singleton("bar"))));
        assertThat(((JSONObjectValue)obtained).get("bar"), is(notNullValue()));
        assertThat(((JSONObjectValue)obtained).get("bar"), is(instanceOf(JSONNullValue.class)));
    }

    @Test
    public void parsingObjectWithStringKeyAndBooleanTrueValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("{\"foo\": true}");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONObjectValue.class)));
        assertThat(((JSONObjectValue)obtained).keySet(), is(equalTo(Collections.singleton("foo"))));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(notNullValue()));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONObjectValue)obtained).get("foo")).getValue(), is(true));
    }

    @Test
    public void parsingObjectWithStringKeyAndBooleanFalseValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("{\"foo\": false}");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONObjectValue.class)));
        assertThat(((JSONObjectValue)obtained).keySet(), is(equalTo(Collections.singleton("foo"))));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(notNullValue()));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(instanceOf(JSONBooleanValue.class)));
        assertThat(((JSONBooleanValue)((JSONObjectValue)obtained).get("foo")).getValue(), is(false));
    }

    @Test
    public void parsingObjectWithStringKeyAndNumericValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("{\"foo\": 42}");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONObjectValue.class)));
        assertThat(((JSONObjectValue)obtained).keySet(), is(equalTo(Collections.singleton("foo"))));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(notNullValue()));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(instanceOf(JSONNumberValue.class)));
        assertThat(((JSONNumberValue)((JSONObjectValue)obtained).get("foo")).getIntValue(), is(42));
    }

    @Test
    public void parsingObjectWithStringKeyAndStringValueWorks() throws ParserException {
        // given
        JSONParser target = new JSONParser("{\"foo\": \"bar\"}");

        // when
        JSONValue obtained = target.parse();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(JSONObjectValue.class)));
        assertThat(((JSONObjectValue)obtained).keySet(), is(equalTo(Collections.singleton("foo"))));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(notNullValue()));
        assertThat(((JSONObjectValue)obtained).get("foo"), is(instanceOf(JSONStringValue.class)));
        assertThat(((JSONStringValue)((JSONObjectValue)obtained).get("foo")).getValue(), is("bar"));
    }

    @Test
    public void parsingUnterminatedObjectAfterKeyHasBeenParsedThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{\"foo\"");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON object")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingUnterminatedObjectAfterKeyValueDelimiterHasBeenParsedThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{\"foo\": ");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON object")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }

    @Test
    public void parsingUnterminatedObjectAfterValueHasBeenParsedThrowsAnException() {
        // given
        JSONParser target = new JSONParser("{\"foo\": \"bar\"");

        // when, then
        try {
            target.parse();
            fail("Expected ParserException not thrown");
        } catch (ParserException e) {
            assertThat(e.getMessage(), is(equalTo("Unterminated JSON object")));
            assertThat(e.getCause(), is(nullValue()));
        }

        // and also check transition into error state
        assertThat(target.getState(), is(JSONParserState.ERROR));
    }
}
