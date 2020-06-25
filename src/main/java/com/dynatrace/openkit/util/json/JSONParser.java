/**
 * Copyright 2018-2020 Dynatrace LLC
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
import com.dynatrace.openkit.util.json.objects.JSONObjectValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import com.dynatrace.openkit.util.json.parser.JSONParserState;
import com.dynatrace.openkit.util.json.parser.ParserException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * JSON parser class for parsing a JSON input string.
 */
public class JSONParser {

    /** error message used for exception, when a JSON array is not terminated */
    private static final String UNTERMINATED_JSON_ARRAY_ERROR = "Unterminated JSON array";
    /** error message used for exception, when a JSON object is not terminated */
    private static final String UNTERMINATED_JSON_OBJECT_ERROR = "Unterminated JSON object";

    /** Lexical analyzer */
    private final JSONLexer lexer;
    /** Current parser state */
    private JSONParserState state = JSONParserState.INIT;
    /** Parsed JSON value object */
    private JSONValue parsedValue = null;

    /** stack storing JSON values (keep in mind there are nested values) */
    private final LinkedList<JSONValueContainer> valueContainerStack = new LinkedList<JSONValueContainer>();
    /** stack storing state. This is required to parse nested objects */
    private final LinkedList<JSONParserState> stateStack = new LinkedList<JSONParserState>();

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
     * @param lexer Lexical analyzer
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
     * @throws LexerException If there is an error during lexical analysis of the JSON string.
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
                case IN_OBJECT_START:
                    parseInObjectStartState(token);
                    break;
                case IN_OBJECT_KEY:
                    parseInObjectKeyState(token);
                    break;
                case IN_OBJECT_COLON:
                    parseInObjectColonState(token);
                    break;
                case IN_OBJECT_VALUE:
                    parseInObjectValueState(token);
                    break;
                case IN_OBJECT_DELIMITER:
                    parseInObjectDelimiterState(token);
                    break;
                case END:
                    parseEndState(token);
                    break;
                case ERROR:
                    // this should never be reached, since whenever there is a transition into
                    // error state, an exception is thrown right afterwards
                    // this is just a precaution
                    throw new ParserException(unexpectedTokenErrorMessage(token, "in error state"));
                default:
                    // precaution: a new state has been added, a transition is
                    throw new ParserException(internalParserErrorMessage(state, "Unexpected JSONParserState"));
            }
        } while (token != null);

        ensureValueContainerStackIsNotEmpty();

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
            case LEFT_BRACE:
                Map<String, JSONValue> jsonObjectMap = new HashMap<String, JSONValue>();
                valueContainerStack.addFirst(new JSONValueContainer(JSONObjectValue.fromMap(jsonObjectMap), jsonObjectMap));
                state = JSONParserState.IN_OBJECT_START;
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException(unexpectedTokenErrorMessage(token, "at start of input"));
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
                ensureTopLevelElementIsAJSONArray();
                valueContainerStack.peekFirst().backingList.add(tokenToSimpleJSONValue(token));
                state = JSONParserState.IN_ARRAY_VALUE;
                break;
            case LEFT_SQUARE_BRACKET:
                // start nested array as first element in array
                parseStartOfNestedArray();
                break;
            case LEFT_BRACE:
                // start nested object as first element in array
                parseStartOfNestedObject();
                break;
            case RIGHT_SQUARE_BRACKET:
                closeCompositeJSONValueAndRestoreState();
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException(unexpectedTokenErrorMessage(token, "at beginning of array"));
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
                closeCompositeJSONValueAndRestoreState();
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException(unexpectedTokenErrorMessage(token, "in array after value has been parsed"));
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
                ensureTopLevelElementIsAJSONArray();
                valueContainerStack.peekFirst().backingList.add(tokenToSimpleJSONValue(token));
                state = JSONParserState.IN_ARRAY_VALUE;
                break;
            case LEFT_SQUARE_BRACKET:
                // start nested array as element in array
                parseStartOfNestedArray();
                break;
            case LEFT_BRACE:
                // start nested object as first element in array
                parseStartOfNestedObject();
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException(unexpectedTokenErrorMessage(token, "in array after delimiter"));
        }
    }

    /**
     * Utility method to parse start of nested object.
     *
     * <p>
     *     This method is called if the left brace token is encountered.
     * </p>
     */
    private void parseStartOfNestedObject() {
        stateStack.push(JSONParserState.IN_ARRAY_VALUE);
        Map<String, JSONValue> jsonObjectMap = new HashMap<String, JSONValue>();
        valueContainerStack.addFirst(new JSONValueContainer(JSONObjectValue.fromMap(jsonObjectMap), jsonObjectMap));
        state = JSONParserState.IN_OBJECT_START;
    }

    /**
     * Utility method to parse start of nested array.
     *
     * <p>
     *     This method is called if the left square bracket token is encountered.
     * </p>
     */
    private void parseStartOfNestedArray() {
        stateStack.push(JSONParserState.IN_ARRAY_VALUE);
        List<JSONValue> jsonValueList = new LinkedList<JSONValue>();
        valueContainerStack.addFirst(new JSONValueContainer(JSONArrayValue.fromList(jsonValueList), jsonValueList));
        state = JSONParserState.IN_ARRAY_START;
    }

    /**
     * Parse token in state right after a JSON object has been started.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInObjectStartState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_OBJECT_ERROR);
        ensureTopLevelElementIsAJSONObject();

        switch (token.getTokenType()) {
            case RIGHT_BRACE:
                // object is closed, right after it was started
                closeCompositeJSONValueAndRestoreState();
                break;
            case VALUE_STRING:
                valueContainerStack.peekFirst().lastParsedObjectKey = token.getValue();
                state = JSONParserState.IN_OBJECT_KEY;
                break;
            default:
                state = JSONParserState.ERROR;
                throw new ParserException(unexpectedTokenErrorMessage(token, "encountered - object key expected"));
        }
    }

    /**
     * Parse token in state right after a JSON key token has been parsed.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInObjectKeyState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_OBJECT_ERROR);

        if (token.getTokenType() == JSONToken.TokenType.COLON) {// got key-value delimiter as expected
            state = JSONParserState.IN_OBJECT_COLON;
        } else {// expected key-value delimiter (":"), but got something different instead
            state = JSONParserState.ERROR;
            throw new ParserException(unexpectedTokenErrorMessage(token, "encountered - key-value delimiter expected"));
        }
    }

    /**
     * Parse token in state right after a JSON key-value delimiter (":") has been parsed.
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInObjectColonState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_OBJECT_ERROR);

        ensureTopLevelElementIsAJSONObject();

        switch (token.getTokenType()) {
            case VALUE_NUMBER:     // FALLTHROUGH
            case VALUE_STRING:     // FALLTHROUGH
            case LITERAL_BOOLEAN:  // FALLTHROUGH
            case LITERAL_NULL:
                // simple JSON value as object value
                valueContainerStack.peekFirst().lastParsedObjectValue = tokenToSimpleJSONValue(token);
                state = JSONParserState.IN_OBJECT_VALUE;
                break;
            case LEFT_BRACE:
                // value is an object
                Map<String, JSONValue> jsonObjectMap = new HashMap<String, JSONValue>();
                valueContainerStack.addFirst(new JSONValueContainer(JSONObjectValue.fromMap(jsonObjectMap), jsonObjectMap));
                stateStack.addFirst(JSONParserState.IN_OBJECT_VALUE);
                state = JSONParserState.IN_OBJECT_START;
                break;
            case LEFT_SQUARE_BRACKET:
                // value is an array
                List<JSONValue> jsonValueList = new LinkedList<JSONValue>();
                valueContainerStack.addFirst(new JSONValueContainer(JSONArrayValue.fromList(jsonValueList), jsonValueList));
                stateStack.addFirst(JSONParserState.IN_OBJECT_VALUE);
                state = JSONParserState.IN_ARRAY_START;
                break;
            default:
                // anything other token
                throw new ParserException(unexpectedTokenErrorMessage(token, "after key-value pair encountered"));
        }
    }

    /**
     * Parse token in state right after a JSON object value has been parsed.
     *
     * <p>
     *     Note for now: An object can have more than one key-value pair, but the value must be simple type.
     * </p>
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInObjectValueState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_OBJECT_ERROR);
        ensureTopLevelElementIsAJSONObject();

        switch (token.getTokenType()) {
            case RIGHT_BRACE:
                // object is closed, right after some value
                // push last parsed key/value into the map
                ensureKeyValuePairWasParsed();
                String key = valueContainerStack.peekFirst().lastParsedObjectKey;
                JSONValue value = valueContainerStack.peekFirst().lastParsedObjectValue;
                valueContainerStack.peekFirst().backingMap.put(key, value);
                closeCompositeJSONValueAndRestoreState();
                break;
            case COMMA:
                // expecting more entries in the current object, push existing and make state transition
                putLastParsedKeyValuePairIntoObject();
                state = JSONParserState.IN_OBJECT_DELIMITER;
                break;
            default:
                // any other token, which is illegal in this case
                state = JSONParserState.ERROR;
                throw new ParserException(unexpectedTokenErrorMessage(token, "after key-value pair encountered"));
        }
    }

    /**
     * Parse token in state right after a delimiter has been parsed.
     *
     * <p>
     *     Note for now: Not supported yet.
     * </p>
     *
     * @param token The token to parse.
     * @throws ParserException In case parsing fails.
     */
    private void parseInObjectDelimiterState(JSONToken token) throws ParserException {
        ensureTokenIsNotNull(token, UNTERMINATED_JSON_OBJECT_ERROR);
        ensureTopLevelElementIsAJSONObject();

        if (token.getTokenType() == JSONToken.TokenType.VALUE_STRING) {
            valueContainerStack.peekFirst().lastParsedObjectKey = token.getValue();
            state = JSONParserState.IN_OBJECT_KEY;
        } else {
            state = JSONParserState.ERROR;
            throw new ParserException(unexpectedTokenErrorMessage(token, "encountered - object key expected"));
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
        throw new ParserException(unexpectedTokenErrorMessage(token, "at end of input"));
    }

    /**
     * Helper method to remove the top level JSON value from the value stack and also the state.
     *
     * @throws ParserException In case of an exception.
     */
    private void closeCompositeJSONValueAndRestoreState() throws ParserException {
        ensureValueContainerStackIsNotEmpty();

        if (valueContainerStack.size() != stateStack.size() + 1) {
            // sanity check, which cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "valueContainerStack and stateStack sizes mismatch"));
        }
        if (valueContainerStack.size() == 1) {
            // the outermost array is terminated
            // do not remove anything from the stack
            state = JSONParserState.END;
            return;
        }

        JSONValue currentValue = valueContainerStack.removeFirst().jsonValue;

        // ensure that there is a new top level element which is a composite value (object or array)
        ensureValueContainerStackIsNotEmpty();
        if (valueContainerStack.peekFirst().jsonValue.isArray()) {
            if (valueContainerStack.peekFirst().backingList == null) {
                // precaution to ensure we did not do something wrong
                throw new ParserException(internalParserErrorMessage(state, "backing list is null"));
            }
            valueContainerStack.peekFirst().backingList.add(currentValue);
        } else if (valueContainerStack.peekFirst().jsonValue.isObject()) {
            valueContainerStack.peekFirst().lastParsedObjectValue = currentValue;
        } else {
            // unexpected top level object - this should not happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "not a composite top level object"));
        }

        state = stateStack.removeFirst();
    }

    /**
     * Put the last parsed key value pair into the top level stack element.
     *
     * <p>
     *     Some sanity checks are performed to ensure consistency.
     * </p>
     *
     * @throws ParserException If anything is inconsistent.
     */
    private void putLastParsedKeyValuePairIntoObject() throws ParserException {
        ensureKeyValuePairWasParsed();
        ensureTopLevelElementIsAJSONObject();

        String key = valueContainerStack.peekFirst().lastParsedObjectKey;
        JSONValue value = valueContainerStack.peekFirst().lastParsedObjectValue;
        valueContainerStack.peekFirst().backingMap.put(key, value);

        valueContainerStack.peekFirst().lastParsedObjectKey = null;
        valueContainerStack.peekFirst().lastParsedObjectValue = null;
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
     * Ensure that value container stack's top element is ok such that a JSON array can be parsed.
     *
     * @throws ParserException If something is invalid.
     */
    private void ensureTopLevelElementIsAJSONArray() throws ParserException {
        ensureValueContainerStackIsNotEmpty();

        if (!valueContainerStack.peekFirst().jsonValue.isArray()) {
            // sanity check, cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "top level element is not a JSON array"));
        }
        if (valueContainerStack.peekFirst().backingList == null) {
            throw new ParserException(internalParserErrorMessage(state, "backing list is null"));
        }
    }

    /**
     * Ensure that value container stack's top element is ok such that a JSON object can be parsed.
     *
     * @throws ParserException If something is invalid.
     */
    private void ensureTopLevelElementIsAJSONObject() throws ParserException {
        ensureValueContainerStackIsNotEmpty();

        if (!valueContainerStack.peekFirst().jsonValue.isObject()) {
            // sanity check, cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "top level element is not a JSON object"));
        }
        if (valueContainerStack.peekFirst().backingMap == null) {
            // sanity check, cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "backing map is null"));
        }
    }

    /**
     * Ensure that previously a key-value pair was parsed.
     *
     * @throws ParserException If something is invalid.
     */
    private void ensureKeyValuePairWasParsed() throws ParserException {
        ensureValueContainerStackIsNotEmpty();

        if (valueContainerStack.peekFirst().lastParsedObjectKey == null) {
            // sanity check, cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "lastParsedObjectKey is null"));
        }
        if (valueContainerStack.peekFirst().lastParsedObjectValue == null) {
            // sanity check, cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "lastParsedObjectValue is null"));
        }
    }

    /**
     * Helper function to ensure that value container stack is not empty.
     *
     * @throws ParserException If {@link this#valueContainerStack} is empty.
     */
    private void ensureValueContainerStackIsNotEmpty() throws ParserException {
        if (valueContainerStack.isEmpty()) {
            // sanity check, which cannot happen, unless there is a programming error
            throw new ParserException(internalParserErrorMessage(state, "valueContainerStack is empty"));
        }
    }

    /**
     * Helper method for creating internal parser error text.
     *
     * @param state Current parser state.
     * @param suffix The suffix to append to the message.
     */
    private static String internalParserErrorMessage(JSONParserState state, String suffix) {
        return "Internal parser error: [state=\"" + state + "\"] " + suffix;
    }

    /**
     * Helper method for creating unexpected token error text.
     *
     * @param token The unexpected token
     * @param suffix The suffix to append to the message.
     */
    private static String unexpectedTokenErrorMessage(JSONToken token, String suffix) {
        return "Unexpected token \"" + token + "\" " + suffix;
    }

    /**
     * Helper class storing the {@link JSONValue} and the appropriate backing container class, if it is a composite object.
     */
    private static final class JSONValueContainer {
        /** The JSON value to store. */
        private final JSONValue jsonValue;
        /** Backing list, which is non-null if and only if {@code jsonValue} is a {@link JSONArrayValue} */
        private final List<JSONValue> backingList;
        /** Backing map, which is non-null if and only if {@code jsonValue} is a {@link JSONObjectValue} */
        private final Map<String, JSONValue> backingMap;
        /** Field to store the last parsed key of an object */
        private String lastParsedObjectKey = null;
        /** Field to store the last parsed value of an object */
        private JSONValue lastParsedObjectValue = null;

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
            backingMap = null;
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
            backingMap = null;
        }

        /**
         * Construct a {@link JSONValueContainer} with a JSON object value.
         *
         * @param jsonObjectValue The JSON object value.
         * @param backingMap The backing map for the JSON object value.
         */
        JSONValueContainer(JSONObjectValue jsonObjectValue, Map<String, JSONValue> backingMap) {
            jsonValue = jsonObjectValue;
            backingList = null;
            this.backingMap = backingMap;
        }
    }
}
