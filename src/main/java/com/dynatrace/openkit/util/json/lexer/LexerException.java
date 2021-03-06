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

/**
 * Exception class thrown by the lexical analyzer in case of error.
 */
public class LexerException extends Exception {

    /**
     * Constructor taking an exception message.
     *
     * @param message The message describing the cause of this exception.
     */
    public LexerException(String message) {
        super(message);
    }

    /**
     * Constructor taking an exception message and a nested {@link Throwable}.
     *
     * @param message The message describing the cause of this exception.
     * @param throwable The nested {@link Throwable} originally causing this exception.
     */
    public LexerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
