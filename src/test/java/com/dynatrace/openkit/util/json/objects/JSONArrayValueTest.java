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

package com.dynatrace.openkit.util.json.objects;

import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class JSONArrayValueTest {

    private static final List<JSONValue> EMPTY_LIST = Collections.emptyList();

    @Test
    public void isNullReturnsFalse() {
        // then
        assertThat(JSONArrayValue.fromList(EMPTY_LIST).isNull(), is(false));
    }

    @Test
    public void isBooleanReturnsFalse() {
        // then
        assertThat(JSONArrayValue.fromList(EMPTY_LIST).isBoolean(), is(false));
    }

    @Test
    public void isNumberReturnsFalse() {
        // then
        assertThat(JSONArrayValue.fromList(EMPTY_LIST).isNumber(), is(false));
    }

    @Test
    public void isStringReturnsFalse() {
        // then
        assertThat(JSONArrayValue.fromList(EMPTY_LIST).isString(), is(false));
    }

    @Test
    public void isArrayReturnsTrue() {
        // then
        assertThat(JSONArrayValue.fromList(EMPTY_LIST).isArray(), is(true));
    }

    @Test
    public void isObjectReturnsFalse() {
        // then
        assertThat(JSONArrayValue.fromList(EMPTY_LIST).isObject(), is(false));
    }

    @Test
    public void fromListGivesNullIfArgumentIsNull() {
        // when constructed with null, then
        assertThat(JSONArrayValue.fromList(null), is(nullValue()));
    }

    @Test
    public void sizeDelegatesTheCallToTheUnderlyingList() {
        // given
        @SuppressWarnings("unchecked")
        List<JSONValue> jsonValues = mock(List.class);
        when(jsonValues.size()).thenReturn(42);
        JSONArrayValue target = JSONArrayValue.fromList(jsonValues);

        // when
        int obtained = target.size();

        // then
        assertThat(obtained, is(42));
        verify(jsonValues, times(1)).size();
        verifyNoMoreInteractions(jsonValues);
    }

    @Test
    public void getDelegatesTheCallToTheUnderlyingList() {
        // given
        @SuppressWarnings("unchecked")
        List<JSONValue> jsonValues = mock(List.class);
        when(jsonValues.get(anyInt())).thenReturn(mock(JSONValue.class));
        JSONArrayValue target = JSONArrayValue.fromList(jsonValues);

        // when get is called
        JSONValue obtained = target.get(0);

        // then
        assertThat(obtained, is(notNullValue()));
        verify(jsonValues, times(1)).get(0);
        verifyNoMoreInteractions(jsonValues);

        // and when get is called with another index
        obtained = target.get(1234);

        // then
        assertThat(obtained, is(notNullValue()));
        verify(jsonValues, times(1)).get(1234);
        verifyNoMoreInteractions(jsonValues);
    }

    @Test
    public void iteratorDelegatesTheCallToTheUnderlyingList() {
        // given
        @SuppressWarnings("unchecked")
        List<JSONValue> jsonValues = mock(List.class);
        when(jsonValues.iterator()).thenReturn(mockIterator());
        JSONArrayValue target = JSONArrayValue.fromList(jsonValues);

        // when
        Iterator<JSONValue> obtained = target.iterator();

        // then
        assertThat(obtained, is(notNullValue()));
        verify(jsonValues, times(1)).iterator();
        verifyNoMoreInteractions(jsonValues);
    }

    @SuppressWarnings("unchecked")
    private static Iterator<JSONValue> mockIterator() {
        return mock(Iterator.class);
    }
}
