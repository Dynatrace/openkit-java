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
import com.dynatrace.openkit.util.json.objects.JSONBooleanValue;
import com.dynatrace.openkit.util.json.objects.JSONNullValue;
import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import com.dynatrace.openkit.util.json.parser.JSONParserState;
import com.dynatrace.openkit.util.json.parser.ParserException;

import java.util.LinkedList;

/**
 * JSON parser class for parsing a JSON input string.
 */
public class JSONParser {

    /** Lexical analyzer */
    private final JSONLexer lexer;
    /** Current parser state */
    private JSONParserState state = JSONParserState.INIT;
    /** Parsed JSON value object */
    private JSONValue parsedValue = null;

    /** stack storing JSON values (keep in mind there are nested values) */
    private LinkedList<JSONValue> valueStack = new LinkedList<JSONValue>();

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
                case END:
                    parseEndState(token);
                    break;
                case ERROR:
                    throw new ParserException("Unexpected token \"" + token + "\" in error state");
                default:
                    throw new IllegalStateException("JSONParserState state = " + state);
            }
        } while (token != null);

        return valueStack.getFirst();
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
        if (token == null) {
            // end of input reached in the init state
            // this means there is nothing to decode
            state = JSONParserState.ERROR;
            throw new ParserException("No JSON object could be decoded");
        }

        // parse the token
        switch (token.getTokenType()) {
            case LITERAL_NULL:
                valueStack.add(JSONNullValue.NULL);
                break;
            case LITERAL_BOOLEAN:
                valueStack.add(JSONBooleanValue.fromLiteral(token.getValue()));
                break;
            case VALUE_STRING:
                valueStack.add(JSONStringValue.fromString(token.getValue()));
                break;
            case VALUE_NUMBER:
                valueStack.add(JSONNumberValue.fromNumberLiteral(token.getValue()));
                break;
            default:
                throw new IllegalStateException("Not implemented");
        }

        // also perform state transition
        state = JSONParserState.END;
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
}
