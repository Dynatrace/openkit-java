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

import com.dynatrace.openkit.util.json.lexer.JSONLexer;
import com.dynatrace.openkit.util.json.lexer.JSONToken;
import com.dynatrace.openkit.util.json.lexer.LexerException;
import com.dynatrace.openkit.util.json.objects.JSONArrayValue;
import com.dynatrace.openkit.util.json.objects.JSONBooleanValue;
import com.dynatrace.openkit.util.json.objects.JSONNullValue;
import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import com.dynatrace.openkit.util.json.parser.JSONParserState;
import com.dynatrace.openkit.util.json.parser.ParserException;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON parser class for parsing a JSON input string.
 */
public class JSONParser {

    /** ERROR message used for exception, when a JSON array is not terminated */
    private static final String UNTERMINATED_JSON_ARRAY_ERROR = "Unterminated JSON array";

    /** Lexical analyzer */
    private final JSONLexer lexer;
    /** Current parser state */
    private JSONParserState state = JSONParserState.INIT;
    /** Parsed JSON value object */
    private JSONValue parsedValue = null;

    /** stack storing JSON values (keep in mind there are nested values) */
    private LinkedList<JSONValueContainer> valueContainerStack = new LinkedList<JSONValueContainer>();
    /** stack storing state. This is required to parse nested objects */
    private LinkedList<JSONParserState> stateStack = new LinkedList<JSONParserState>();

    /**
     * Constructor taking the JSON input string.
     *
     * @param input JSON input string.
     */
    public JSONParser(String input) {
        this(new JSONLexer(input));
    }

    /**
     * Internal constructor taking the lexical analyzer.
     *
     * <p>
     *     This ctor can be used in tests when mocking the lexer is needed.
     * </p>
     *
     * @param lexer Lexical
     */
    JSONParser(JSONLexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Parse the JSON string passed in the constructor and return the parsed JSON value object.
     *
     * @return Parsed JSON value object.
     *
     * @throws ParserException If there is an error while parsing the input string.
     */
    public JSONValue parse() throws ParserException {
        // return the already parsed object, if parse has been called before
        if (state == JSONParserState.END) {
            return parsedValue;
        }

        // throw an exception if parser is in erroneous state
        if (state == JSONParserState.ERROR) {
            throw new ParserException("JSON parser is in erroneous state");
        }

        // do parse input string
        try {
            parsedValue = doParse();
        } catch (LexerException e) {
            state = JSONParserState.ERROR;
            throw new ParserException("Caught exception from lexical analysis", e);
        }

        return parsedValue;
    }

    /**
     * Method for retrieving current parser state.
     *
     * <p>
     *     This method is only intended for unit tests to properly retrieve the current state.
     * </p>
     *
     * @return Returns current parser state.
     */
    JSONParserState getState() {
        return state;
    }

    /**
     * Parse the JSON string passed in the constructor and return the parsed JSON value object.
     *
     * @return Parsed JSON value object.
     *
     * @throws LexerException If there is an error while during lexical analysis if the JSON string.
     */
    private JSONValue doParse() throws LexerException, ParserException {
        JSONToken token;
        do {
            token = lexer.nextToken();
            switch (state) {
                case INIT:
                    parseInitState(token);
                    break;
                case IN_ARRAY_START:
                    parseInArrayStartState(token);
                    break;
                case IN_ARRAY_VALUE:
                    parseInArrayValueState(token);
                    break;
                case IN_ARRAY_DELIMITER:
                    parseInArrayDelimiterState(token);
                    break;
                case END:
                    parseEndState(token);
                    break;
                case ERROR:
                    // this should never be reached, since whenever there is a transition into
                    // error state, an exception is thrown right afterwards
                    // this is just a precaution
                    throw new ParserException("Unexpected token \"" + token + "\" in error state");
                default:
                    // precaution: a new state has been added, a transition is
                    throw new ParserException("Internal parser error: Unexpected JSONParserState " + state);
            }
        } while (token != null);

        if (valueContainerStack.isEmpty()) {
            // sanity check, which cannot happen, unless there is a programming error
            throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack is empty");
        }

        return valueContainerStack.getFirst().jsonValue;
    }

    /**
     * Parse token in init state.
     *
     * <p>
     *     This state is the state right after starting parsing the JSON string.
     *     Valid and expected tokens in this state are simple value tokens or start of a compound value.
     * </p>
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInitState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, "No JSON object could be decoded");

        // parse the token
        switch (token.getTokenType()) {
            case LITERAL_NULL:    // FALLTHROUGH
            case LITERAL_BOOLEAN: // FALLTHROUGH
            case VALUE_STRING:    // FALLTHROUGH
            case VALUE_NUMBER:
                valueContainerStack.addFirst(new JSONValueContainer(tokenToSimpleJSONValue(token)));
                state = JSONParserState.END;
                break;
            case LEFT_SQUARE_BRACKET:
                List<JSONValue> jsonValueList = new LinkedList<JSONValue>();
                valueContainerStack.addFirst(new JSONValueContainer(JSONArrayValue.fromList(jsonValueList), jsonValueList));
                state = JSONParserState.IN_ARRAY_START;
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException("Unexpected token \"" + token + "\" at start of input");
        }
    }

    /**
     * Parse token in state when arrays has been started.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInArrayStartState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_ARRAY_ERROR);

        switch (token.getTokenType()) {
            case LITERAL_NULL:    // FALLTHROUGH
            case LITERAL_BOOLEAN: // FALLTHROUGH
            case VALUE_STRING:    // FALLTHROUGH
            case VALUE_NUMBER:
                if (valueContainerStack.isEmpty()) {
                    // sanity check, which cannot happen, unless there is a programming error
                    throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack is empty");
                }
                if (valueContainerStack.getFirst().backingList == null) {
                    // precaution to ensure we did not do something wrong
                    throw new ParserException("Internal parser error: [state=" + state + "] backing list is null");
                }
                valueContainerStack.getFirst().backingList.add(tokenToSimpleJSONValue(token));
                state = JSONParserState.IN_ARRAY_VALUE;
                break;
            case LEFT_SQUARE_BRACKET:
                // start nested array as first element in array
                // The nested array is just another value, therefore we push that state to the stack
                stateStack.push(JSONParserState.IN_ARRAY_VALUE);
                List<JSONValue> jsonValueList = new LinkedList<JSONValue>();
                valueContainerStack.addFirst(new JSONValueContainer(JSONArrayValue.fromList(jsonValueList), jsonValueList));
                state = JSONParserState.IN_ARRAY_START;
                break;
            case RIGHT_SQUARE_BRACKET:
                if (valueContainerStack.isEmpty()) {
                    // sanity check, which cannot happen, unless there is a programming error
                    throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack is empty");
                }
                if (valueContainerStack.size() != stateStack.size() + 1) {
                    // sanity check, which cannot happen, unless there is a programming error
                    throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack and stateStack sizes mismatch");
                }
                if (stateStack.isEmpty()) {
                    // the outermost array is terminated
                    // do not remove anything from the stack
                    state = JSONParserState.END;
                } else {
                    // a nested array is terminated
                    JSONValue currentValue = valueContainerStack.removeFirst().jsonValue;
                    if (valueContainerStack.getFirst().backingList == null) {
                        // precaution to ensure we did not do something wrong
                        throw new ParserException("Internal parser error: [state=" + state + "] backing list is null");
                    }
                    valueContainerStack.getFirst().backingList.add(currentValue);
                    state = stateStack.removeFirst();
                }
                break;

            default:
                state = JSONParserState.ERROR;
                throw new ParserException("Unexpected token \"" + token + "\" at beginning of array");
        }
    }

    /**
     * Parse token in state when in array and a value has been parsed previously.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInArrayValueState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_ARRAY_ERROR);

        switch (token.getTokenType()) {
            case COMMA:
                state = JSONParserState.IN_ARRAY_DELIMITER;
                break;
            case RIGHT_SQUARE_BRACKET:
                if (valueContainerStack.isEmpty()) {
                    // sanity check, which cannot happen, unless there is a programming error
                    throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack is empty");
                }
                if (valueContainerStack.size() != stateStack.size() + 1) {
                    // sanity check, which cannot happen, unless there is a programming error
                    throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack and stateStack sizes mismatch");
                }
                if (stateStack.isEmpty()) {
                    // the outermost array is terminated
                    // do not remove anything from the stack
                    state = JSONParserState.END;
                } else {
                    // a nested array is terminated
                    JSONValue currentValue = valueContainerStack.removeFirst().jsonValue;
                    if (valueContainerStack.getFirst().backingList == null) {
                        // precaution to ensure we did not do something wrong
                        throw new ParserException("Internal parser error: [state=" + state + "] backing list is null");
                    }
                    valueContainerStack.getFirst().backingList.add(currentValue);
                    state = stateStack.removeFirst();
                }
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException("Unexpected token \"" + token + "\" in array after value has been parsed");
        }
    }

    /**
     * Parse token in state when in array and a delimiter has been parsed previously.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInArrayDelimiterState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_ARRAY_ERROR);

        switch (token.getTokenType()) {
            case LITERAL_NULL:    // FALLTHROUGH
            case LITERAL_BOOLEAN: // FALLTHROUGH
            case VALUE_STRING:    // FALLTHROUGH
            case VALUE_NUMBER:
                if (valueContainerStack.isEmpty()) {
                    // sanity check, which cannot happen, unless there is a programming error
                    throw new ParserException("Internal parser error: [state=\" + state + \"] valueContainerStack is empty");
                }
                if (valueContainerStack.getFirst().backingList == null) {
                    throw new ParserException("Internal parser error: [state=" + state + "] backing list is null");
                }
                valueContainerStack.getFirst().backingList.add(tokenToSimpleJSONValue(token));
                state = JSONParserState.IN_ARRAY_VALUE;
                break;
            case LEFT_SQUARE_BRACKET:
                // start nested array as element in array
                // The nested array is just another value, therefore we push that state to the stack
                stateStack.push(JSONParserState.IN_ARRAY_VALUE);
                List<JSONValue> jsonValueList = new LinkedList<JSONValue>();
                valueContainerStack.addFirst(new JSONValueContainer(JSONArrayValue.fromList(jsonValueList), jsonValueList));
                state = JSONParserState.IN_ARRAY_START;
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException("Unexpected token \"" + token + "\" in array after delimiter");
        }
    }


    /**
     * Parse token when already in end state.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseEndState(JSONToken token) throws ParserException {
        if (token == null) {
            // end of input, as expected in regular terminal state
            return;
        }

        // unexpected token when end of input was already expected
        state = JSONParserState.ERROR;
        throw new ParserException("Unexpected token \"" + token + "\" at end of input");
    }

    /**
     * Helper method for converting a JSON token to a JSON value.
     *
     * <p>
     *     Only simple JSON values are supported.
     * </p>
     *
     * @param token The token to convert to a JSON value.
     * @return Converted JSON value.
     */
    private static JSONValue tokenToSimpleJSONValue(JSONToken token) throws ParserException {
        switch (token.getTokenType()) {
            case LITERAL_NULL:
                return JSONNullValue.NULL;
            case LITERAL_BOOLEAN:
                return JSONBooleanValue.fromLiteral(token.getValue());
            case VALUE_STRING:
                return JSONStringValue.fromString(token.getValue());
            case VALUE_NUMBER:
                return JSONNumberValue.fromNumberLiteral(token.getValue());
            default:
                throw new ParserException("Internal parser error: Unexpected JSON token \"" + token + "\"");
        }
    }

    /**
     * Ensure that given {@code token} is not {@code null}.
     *
     * <p>
     *     If {@code token} is {@code null}, then {@link ParserException} is thrown.
     * </p>
     *
     * @param token The token to check whether it's null or not
     * @param exceptionMessage The message passed to {@link ParserException}.
     * @throws ParserException Thrown if token is {@code null}.
     */
    private void ensureTokenIsNotNull(JSONToken token, String exceptionMessage) throws ParserException {
        if (token == null) {
            state = JSONParserState.ERROR;
            throw new ParserException(exceptionMessage);
        }
    }

    /**
     * Helper class storing the {@link JSONValue} and the appropriate backing container class, if it is a composite object.
     */
    private static final class JSONValueContainer {
        /** The JSON value to store. */
        private final JSONValue jsonValue;
        /** Backing list, which is non-null if and only if {@code jsonValue} is a {@link JSONArrayValue} */
        private final List<JSONValue> backingList;

        /**
         * Construct a {@link JSONValueContainer} with a JSON value.
         *
         * <p>
         *     Any backing container classes are set to null, therefore only use this ctor with simple values.
         * </p>
         *
         * @param jsonValue A simple {@link JSONValue} to initialize this container with.
         */
        JSONValueContainer(JSONValue jsonValue) {
            this.jsonValue = jsonValue;
            backingList = null;
        }

        /**
         * Construct a {@link JSONValueContainer} with a JSON array value.
         *
         * @param jsonArrayValue The JSON array value.
         * @param backingList The backing list for the JSON array value.
         */
        JSONValueContainer(JSONArrayValue jsonArrayValue, List<JSONValue> backingList) {
            jsonValue = jsonArrayValue;
            this.backingList = backingList;
        }
    }
}
