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

package com.dynatrace.openkit.util.json.objects;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class JSONObjectValueTest {

    private static final Map<String, JSONValue> EMPTY_MAP = Collections.emptyMap();

    @Test
    public void isNullReturnsFalse() {
        // then
        assertThat(JSONObjectValue.fromMap(EMPTY_MAP).isNull(), is(false));
    }

    @Test
    public void isBooleanReturnsFalse() {
        // then
        assertThat(JSONObjectValue.fromMap(EMPTY_MAP).isBoolean(), is(false));
    }

    @Test
    public void isNumberReturnsFalse() {
        // then
        assertThat(JSONObjectValue.fromMap(EMPTY_MAP).isNumber(), is(false));
    }

    @Test
    public void isStringReturnsFalse() {
        // then
        assertThat(JSONObjectValue.fromMap(EMPTY_MAP).isString(), is(false));
    }

    @Test
    public void isArrayReturnsFalse() {
        // then
        assertThat(JSONObjectValue.fromMap(EMPTY_MAP).isArray(), is(false));
    }

    @Test
    public void isObjectReturnsTrue() {
        // then
        assertThat(JSONObjectValue.fromMap(EMPTY_MAP).isObject(), is(true));
    }

    @Test
    public void fromMapGivesNullIfArgumentIsNull() {
        // when constructed with null, then
        assertThat(JSONObjectValue.fromMap(null), is(nullValue()));
    }

    @Test
    public void keySetDelegatesTheCallToTheUnderlyingMap() {
        // given
        @SuppressWarnings("unchecked") Map<String, JSONValue> jsonObjectMap = mock(Map.class);
        when(jsonObjectMap.keySet()).thenReturn(Collections.singleton("foobar"));
        JSONObjectValue target = JSONObjectValue.fromMap(jsonObjectMap);

        // when
        Set<String> obtained = target.keySet();

        // then
        assertThat(obtained, is(notNullValue()));
        verify(jsonObjectMap, times(1)).keySet();
        verifyNoMoreInteractions(jsonObjectMap);
    }

    @Test
    public void sizeDelegatesTheCallToTheUnderlyingMap() {
        // given
        @SuppressWarnings("unchecked") Map<String, JSONValue> jsonObjectMap = mock(Map.class);
        when(jsonObjectMap.size()).thenReturn(42);
        JSONObjectValue target = JSONObjectValue.fromMap(jsonObjectMap);

        // when
        int obtained = target.size();

        // then
        assertThat(obtained, is(42));
        verify(jsonObjectMap, times(1)).size();
        verifyNoMoreInteractions(jsonObjectMap);
    }

    @Test
    public void containsKeyDelegatesTheCallToTheUnderlyingMap() {
        // given
        @SuppressWarnings("unchecked") Map<String, JSONValue> jsonObjectMap = mock(Map.class);
        when(jsonObjectMap.containsKey(anyString())).thenReturn(true);
        JSONObjectValue target = JSONObjectValue.fromMap(jsonObjectMap);

        // when
        boolean obtained = target.containsKey("foo");

        // then
        assertThat(obtained, is(true));
        verify(jsonObjectMap, times(1)).containsKey("foo");
        verifyNoMoreInteractions(jsonObjectMap);

        // and when
        obtained = target.containsKey("bar");

        // then
        assertThat(obtained, is(true));
        verify(jsonObjectMap, times(1)).containsKey("bar");
        verifyNoMoreInteractions(jsonObjectMap);
    }

    @Test
    public void getDelegatesTheCallToTheUnderlyingMap() {
        // given
        @SuppressWarnings("unchecked") Map<String, JSONValue> jsonObjectMap = mock(Map.class);
        when(jsonObjectMap.get(anyString())).thenReturn(mock(JSONValue.class));
        JSONObjectValue target = JSONObjectValue.fromMap(jsonObjectMap);

        // when
        JSONValue obtained = target.get("foo");

        // then
        assertThat(obtained, is(notNullValue()));
        verify(jsonObjectMap, times(1)).get("foo");
        verifyNoMoreInteractions(jsonObjectMap);

        // and when
        obtained = target.get("bar");

        // then
        assertThat(obtained, is(notNullValue()));
        verify(jsonObjectMap, times(1)).get("bar");
        verifyNoMoreInteractions(jsonObjectMap);
    }
}
