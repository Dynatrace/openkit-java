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

import com.dynatrace.openkit.util.json.constants.JSONLiterals;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Lexical analyzer for JSON, based on RFC 8259 (see also https://tools.ietf.org/html/rfc8259).
 *
 * <p>
 *     Unlike traditional lexers, which are normally generated, this one is implemented by hand.
 *     A normal implementation would rely on a DFA, this one encapsulates substates in subroutines.
 * </p>
 */
public class JSONLexer implements Closeable {

    /** end of file character */
    private static final int EOF = -1;

    /** space character (0x20) */
    private static final char SPACE = ' ';
    /** horizontal tab character (0x09) */
    private static final char HORIZONTAL_TAB = '\t';
    /** line feed or new line character (0x0A) */
    private static final char LINE_FEED = '\n';
    /** carriage return character (0x0D) */
    private static final char CARRIAGE_RETURN = '\r';

    /** begin-array (left square bracket) */
    private static final char LEFT_SQUARE_BRACKET = '[';
    /** end-array (right square bracket) */
    private static final char RIGHT_SQUARE_BRACKET = ']';
    /** begin-object (left brace) */
    private static final char LEFT_BRACE = '{';
    /** end-object (right brace) */
    private static final char RIGHT_BRACE = '}';
    /** name-separator (colon) */
    private static final char COLON = ':';
    /** value-separator (comma) */
    private static final char COMMA = ',';
    /** escape character (reverse solidus, aka. backslash) */
    private static final char REVERSE_SOLIDUS = '\\';
    /** character that may be escaped */
    private static final char SOLIDUS = '/';
    /** start and end of JSON string (quotation mark) */
    private static final char QUOTATION_MARK = '"';
    /** first character of true literal */
    private static final char TRUE_LITERAL_START = 't';
    /** first character of false literal */
    private static final char FALSE_LITERAL_START = 'f';
    /** first character of null literal */
    private static final char NULL_LITERAL_START = 'n';
    /** backspace character */
    private static final char BACKSPACE = '\b';
    /** form feed character */
    private static final char FORM_FEED = '\f';

    /** The number of characters used, after an unicode escape sequence is encountered */
    private static final int NUM_UNICODE_CHARACTERS = 4;

    /** Lexer state */
    enum State {
        INITIAL,        // initial parsing state
        PARSING,        // parsing state
        EOF,            // EOF state
        ERROR           // erroneous state
    }
    private State lexerState = State.INITIAL;

    /** String builder storing all consumed characters, when parsing a JSON string */
    private StringBuilder stringValueBuilder;

    /** Reader from where to read input JSON. */
    private final BufferedReader reader;

    /**
     * Constructor taking the JSON string.
     *
     * @param input JSON string for lexical analysis.
     */
    public JSONLexer(String input) {
        this(new StringReader(input));
    }

    /**
     * Constructor taking a {@link Reader} from where to read the JSON data.
     *
     * @param input A {@link Reader} instance from where to read the JSON data.
     */
    public JSONLexer(Reader input) {
        reader = new BufferedReader(input);
    }

    /**
     * Get the next token, or {@code null} if no next token is available.
     *
     * @return The next {@link JSONToken} or {@code null} if there is no next token.
     */
    public JSONToken nextToken() throws LexerException {

        if (lexerState == State.ERROR) {
            throw new LexerException("JSON Lexer is in erroneous state");
        }
        if (lexerState == State.EOF) {
            return null;
        }

        // "Insignificant whitespace is allowed before or after any of the six
        //  structural characters." (https://tools.ietf.org/html/rfc8259#section-2)
        // Therefore consume all whitespace characters
        JSONToken nextToken;
        try {
            boolean isEndOfFileReached = consumeWhitespaceCharacters();
            if (isEndOfFileReached) {
                lexerState = State.EOF;
                nextToken = null;
            } else {
                lexerState = State.PARSING;
                nextToken = doParseNextToken();
            }
        } catch (LexerException e) {
            lexerState = State.ERROR;
            throw e;
        } catch (IOException e) {
            lexerState = State.ERROR;
            throw new LexerException("IOException occurred", e);
        }

        return nextToken;
    }

    /**
     * Parse token after all whitespace characters have been consumed.
     *
     * <p>
     *     It must be guaranteed that the first character read is
     *     <ul>
     *         <li>a non-whitespace character</li>
     *         <li>not EOF (int value -1)</li>
     *     </ul>
     * </p>
     *
     * @return The next token.
     *
     * @throws IOException In case an IOError was encountered.
     */
    private JSONToken doParseNextToken() throws IOException, LexerException {

        // parse the next character
        reader.mark(1);
        char nextChar = (char) reader.read();
        switch (nextChar) {
            case LEFT_SQUARE_BRACKET:
                return JSONToken.LEFT_SQUARE_BRACKET_TOKEN;
            case RIGHT_SQUARE_BRACKET:
                return JSONToken.RIGHT_SQUARE_BRACKET_TOKEN;
            case LEFT_BRACE:
                return JSONToken.LEFT_BRACE_TOKEN;
            case RIGHT_BRACE:
                return JSONToken.RIGHT_BRACE_TOKEN;
            case COLON:
                return JSONToken.COLON_TOKEN;
            case COMMA:
                return JSONToken.COMMA_TOKEN;
            case TRUE_LITERAL_START: // FALLTHROUGH
            case FALSE_LITERAL_START:
                // could be a boolean literal (true|false)
                reader.reset();
                return tryParseBooleanLiteral();
            case NULL_LITERAL_START:
                // could be null literal
                reader.reset();
                return tryParseNullLiteral();
            case QUOTATION_MARK:
                // string value - omit the beginning "
                return tryParseStringToken();
            default:
                // check if it's a number or a completely unknown token
                if (isDigitOrMinus(nextChar)) {
                    reader.reset();
                    return tryParseNumberToken();
                } else {
                    reader.reset();
                    String literalToken = parseLiteral();
                    throw new LexerException(unexpectedLiteralTokenMessage(literalToken));
                }
        }
    }

    /**
     * Try to parse a boolean literal, which is either true or false with all lower case characters.
     *
     * The definition of a boolean literal follows the specs from https://tools.ietf.org/html/rfc8259#section-3
     *
     * @return The parsed {@link JSONToken}.
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If token is not a valid boolean literal according to RFC 8259.
     */
    private JSONToken tryParseBooleanLiteral() throws IOException, LexerException {
        String literalToken = parseLiteral();
        if (literalToken.equals(JSONLiterals.BOOLEAN_TRUE_LITERAL)) {
            return JSONToken.BOOLEAN_TRUE_TOKEN;
        } else if (literalToken.equals(JSONLiterals.BOOLEAN_FALSE_LITERAL)) {
            return JSONToken.BOOLEAN_FALSE_TOKEN;
        }

        // not a valid boolean literal
        throw new LexerException(unexpectedLiteralTokenMessage(literalToken));
    }

    /**
     * Try to parse a null literal.
     *
     * The definition of a null literal follows the specs from https://tools.ietf.org/html/rfc8259#section-3
     *
     * @return The parsed {@link JSONToken}
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If token is not a valid null literal according to RFC 8259.
     */
    private JSONToken tryParseNullLiteral() throws IOException, LexerException {
        String literalToken = parseLiteral();
        if (literalToken.equals(JSONLiterals.NULL_LITERAL)) {
            // it's a valid null literal
            return JSONToken.NULL_TOKEN;
        }

        // not a valid null literal
        throw new LexerException(unexpectedLiteralTokenMessage(literalToken));
    }

    /**
     * Try to parse a string JSON string token.
     *
     * @return The parsed string token.
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If token is not a valid null literal according to RFC 8259.
     */
    private JSONToken tryParseStringToken() throws IOException, LexerException {

        stringValueBuilder = new StringBuilder();
        int nextChar = reader.read();
        while (nextChar != EOF && nextChar != QUOTATION_MARK) {
            if (isEscapeCharacter(nextChar)) {
                tryParseEscapeSequence();
            } else if (isCharacterThatNeedsEscaping(nextChar)) {
                stringValueBuilder = null;
                throw new LexerException("Invalid control character \"\\u" + String.format("%04X", nextChar) + "\"");
            } else {
                stringValueBuilder.append((char)nextChar);
            }

            nextChar = reader.read();
        }

        String stringValue = stringValueBuilder.toString();
        stringValueBuilder = null;

        if (nextChar != EOF) {
            // string is properly terminated
            return JSONToken.createStringToken(stringValue);
        }

        // string is not properly terminated, because EOF was reached
        throw new LexerException("Unterminated string literal \"" + stringValue + "\"");
    }

    /**
     * Try to parse an escape sequence.
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If token is not a valid escape sequence according to RFC 8259.
     */
    private void tryParseEscapeSequence() throws IOException, LexerException {
        int nextChar = reader.read();
        switch (nextChar) {
            case EOF:
                String stringValue = stringValueBuilder.toString();
                stringValueBuilder = null;
                throw new LexerException("Unterminated string literal \"" + stringValue + "\"");
            case QUOTATION_MARK:  // FALLTHROUGH
            case REVERSE_SOLIDUS:
            case SOLIDUS:
                stringValueBuilder.append((char)nextChar);
                break;
            case 'b':
                stringValueBuilder.append(BACKSPACE);
                break;
            case 'f':
                stringValueBuilder.append(FORM_FEED);
                break;
            case 'n':
                stringValueBuilder.append(LINE_FEED);
                break;
            case 'r':
                stringValueBuilder.append(CARRIAGE_RETURN);
                break;
            case 't':
                stringValueBuilder.append(HORIZONTAL_TAB);
                break;
            case 'u':
                tryParseUnicodeEscapeSequence();
                break;
            default:
                throw new LexerException("Invalid escape sequence \"\\" + (char)nextChar + "\"");
        }
    }

    /**
     * Try to parse a unicode escape sequence.
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If token is not a valid unicode escape sequence according to RFC 8259.
     */
    private void tryParseUnicodeEscapeSequence() throws IOException, LexerException {

        StringBuilder unicodeSequence = readUnicodeEscapeSequence();

        // parse out the hex character
        char parsedChar = (char)Integer.parseInt(unicodeSequence.toString(), 16);
        if (Character.isHighSurrogate(parsedChar)) {
            // try to parse subsequent low surrogate
            Character lowSurrogate = tryParseLowSurrogateChar();
            if (lowSurrogate == null || !Character.isLowSurrogate(lowSurrogate)) {
                throw new LexerException("Invalid UTF-16 surrogate pair \"\\u" + unicodeSequence + "\"");
            }
            // append both surrogate characters
            stringValueBuilder.append(parsedChar).append((char)lowSurrogate);
        } else if (Character.isLowSurrogate(parsedChar)) {
            // low surrogate character without previous high surrogate
            throw new LexerException("Invalid UTF-16 surrogate pair \"\\u" + unicodeSequence.toString() + "\"");
        } else {
            stringValueBuilder.append(parsedChar);
        }
    }

    /**
     * Try to parse a low surrogate UTF-16 character.
     *
     * @return The low surrogate character or {@code null} if the first two characters
     *         are not the start of a unicode escape sequence.
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If a unicode escape sequence cannot be parsed.
     */
    private Character tryParseLowSurrogateChar() throws IOException, LexerException {
        reader.mark(2);
        if (reader.read() == REVERSE_SOLIDUS && reader.read() == 'u') {
            StringBuilder unicodeSequence = readUnicodeEscapeSequence();
            return (char)Integer.parseInt(unicodeSequence.toString(), 16);
        } else {
            // the first two characters encountered were not the unicode escape sequence prefix
            reader.reset();
            return null;
        }
    }

    /**
     * Read a unicode escape sequence and return the read data.
     *
     * <p>
     *     A unicode escape sequence is a 4 character long sequence where each of those
     *     4 characters is a hex (base-16) character. The characters a-f may be lower, upper or mixed case.
     * </p>
     *
     * @return
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If a unicode escape sequence cannot be parsed.
     */
    private StringBuilder readUnicodeEscapeSequence() throws IOException, LexerException {
        StringBuilder sb = new StringBuilder(NUM_UNICODE_CHARACTERS);

        int nextChar = reader.read();
        while (nextChar != EOF && sb.length() < NUM_UNICODE_CHARACTERS) {
            if (!isHexCharacter(nextChar)) {
                throw new LexerException("Invalid unicode escape sequence \"\\u" + sb.toString() + (char)nextChar + "\"");
            }

            sb.append((char)nextChar);
            reader.mark(1);
            nextChar = reader.read();
        }

        if (nextChar == EOF) {
            // string is not properly terminated, because EOF was reached
            throw new LexerException("Unterminated string literal \"\\u" + sb.toString() + "\"");
        }


        // 4 hex characters were parsed
        reader.reset();

        return sb;
    }

    /**
     * Try to parse a number token.
     *
     * @return Returns the parsed number token.
     *
     * @throws IOException In case an IOError was encountered.
     * @throws LexerException If the number token is not valid according to the RFC specification.
     */
    private JSONToken tryParseNumberToken() throws IOException, LexerException {
        String literalToken = parseLiteral();
        if (JSONLiterals.NUMBER_PATTERN.matcher(literalToken).matches()) {
            return JSONToken.createNumberToken(literalToken);
        }

        // not a valid number literal
        throw new LexerException("Invalid number literal \"" + literalToken + "\"");
    }

    /**
     * Parse a literal.
     *
     * <p>
     *     A literal is considered to be terminated by either
     *     <ul>
     *         <li>One of the four whitespace characters</li>
     *         <li>One of the six structural characters</li>
     *     </ul>
     * </p>
     *
     * @return Returns the parsed literal as string.
     *
     * @throws IOException In case an IOError was encountered.
     */
    private String parseLiteral() throws IOException {
        StringBuilder literalTokenBuilder = new StringBuilder(16);

        reader.mark(1);
        int chr = reader.read();
        while (chr != EOF && !isJSONWhitespaceCharacter(chr) && !isJSONStructuralChar(chr)) {
            literalTokenBuilder.append((char) chr);
            reader.mark(1);
            chr = reader.read();
        }

        if (chr != EOF) {
            // reset read position
            // which might be a whitespace or structural character
            reader.reset();
        }

        return literalTokenBuilder.toString();
    }

    /**
     * Consume all whitespace character from the {@link BufferedReader} until first non-whitespace is encountered.
     *
     * @return {@code true} if EOF (end of file) is reached, {@code false} otherwise.
     *
     * @throws IOException In case an IOError was encountered.
     */
    private boolean consumeWhitespaceCharacters() throws IOException {
        int chr;
        do {
            reader.mark(1);
            chr = reader.read();
        } while (chr != EOF && isJSONWhitespaceCharacter(chr));

        if (chr != EOF) {
            // reset read position to the first non-whitespace character
            reader.reset();
            return false;
        }

        return true;
    }

    /**
     * Test if the given character is a JSON whitespace character according to RFC-8259.
     *
     * @param chr The character to test whether it's a whitespace character or not.
     *
     * @return {@code true} if it's a whitespace character, {@code false} otherwise.
     */
    private static boolean isJSONWhitespaceCharacter(int chr) {
        return chr == SPACE || chr == HORIZONTAL_TAB || chr == LINE_FEED || chr == CARRIAGE_RETURN;
    }

    /**
     * Test if the given character is a JSON structural character according to RFC-8259.
     *
     * @param chr The character to test whether it's a structural character or not.
     *
     * @return {@code true} if it's a structural character, {@code false} otherwise.
     */
    private static boolean isJSONStructuralChar(int chr) {
        return chr == LEFT_SQUARE_BRACKET || chr == RIGHT_SQUARE_BRACKET || chr == LEFT_BRACE || chr == RIGHT_BRACE || chr == COLON || chr == COMMA;
    }

    /**
     * Test if given character is a digit character or a minus character.
     *
     * @param chr The character to test.
     * @return {@code true} if it's a digit or minus character, {@code false} otherwise.
     */
    private boolean isDigitOrMinus(int chr) {
        return chr == '-'
            || (chr >= '0' && chr <= '9');
    }

    /**
     * Test if given character is the escape character to escape others in JSON string.
     *
     * @param chr The character to test.
     * @return {@code true} if it's the escape character, {@code false} otherwise.
     */
    private static boolean isEscapeCharacter(int chr) {
        return chr == REVERSE_SOLIDUS;
    }

    /**
     * Test if given character needs escaping (according to https://tools.ietf.org/html/rfc8259#section-7)
     * <p>
     *     Characters that need to be escaped according to RFC:
     *     <ul>
     *         <li>quotation mark</li>
     *         <li>reverse solidus</li>
     *         <li>control characters [U+0000, U+001F]</li>
     *     </ul>
     * </p>
     * @param chr The character to test.
     * @return {@code true} if the character needs to be escaped in JSON strings, {@code false} otherwise.
     */
    private static boolean isCharacterThatNeedsEscaping(int chr) {
        return chr == QUOTATION_MARK
            || chr == REVERSE_SOLIDUS
            || (chr >= 0 && chr <= 0x1F);
    }

    /**
     * Helper method to test if the given character is a hex character.
     *
     * <p>
     *     A character is considered to be a hex character, if
     *     <u>
     *         <li>It is in range 0-9</li>
     *         <li>It is in range a-f</li>
     *         <li>It is in range A-F</li>
     *     </u>
     * </p>
     *
     * @param chr The character to test.
     * @return {@code true} if the character is a hex character, {@code false} otherwise.
     */
    private static boolean isHexCharacter(int chr) {
        return (chr >= '0' && chr <= '9')
            || (chr >= 'a' && chr <= 'f')
            || (chr >= 'A' && chr <= 'F');
    }

    /**
     * Helper method to return the "unexpected literal" exception message.
     *
     * <p>
     *     This method is a helper to avoid code duplication.
     * </p>
     *
     * @param literalToken The unexpected literal token.
     * @return An error message for an exception.
     */
    private static String unexpectedLiteralTokenMessage(String literalToken) {
        return "Unexpected literal \"" + literalToken + "\"";
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
