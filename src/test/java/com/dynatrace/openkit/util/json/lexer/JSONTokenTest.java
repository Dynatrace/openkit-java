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
}
