/**
 *   Copyright 2018-2020 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.WebRequestTracer;
import org.junit.Before;
import org.junit.Test;

import java.net.URLConnection;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NullActionTest {

    private Action mockParent;

    @Before
    public void setUp() {
        mockParent = mock(Action.class);
    }

    @Test
    public void createNewInstance() {
        // given, when
        createNullAction();

        // then
        verifyZeroInteractions(mockParent);
    }

    @Test
    public void reportEventReturnsSelf() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.reportEvent("event name");

        // then
        assertThat(obtained, is(instanceOf(NullAction.class)));
        assertThat((NullAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportIntValueReturnsSelf() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.reportValue("value name", 12);

        // then
        assertThat(obtained, is(instanceOf(NullAction.class)));
        assertThat((NullAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportLongValueReturnsSelf() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.reportValue("value name", Long.MAX_VALUE);

        // then
        assertThat(obtained, is(instanceOf(NullAction.class)));
        assertThat((NullAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportDoubleValueReturnsSelf() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.reportValue("value name", 37.73);

        // then
        assertThat(obtained, is(instanceOf(NullAction.class)));
        assertThat((NullAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportStringValueReturnsSelf() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.reportValue("value name", "value");

        // then
        assertThat(obtained, is(instanceOf(NullAction.class)));
        assertThat((NullAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportErrorReturnsSelf() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.reportError("error name", 1337, "something bad");

        // then
        assertThat(obtained, is(instanceOf(NullAction.class)));
        assertThat((NullAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void traceWebRequestWithUrlConnectionReturnsNullWebRequestTracer () {
        // given
        NullAction target = createNullAction();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(NullWebRequestTracer.INSTANCE)));
    }

    @Test
    public void traceWebRequestWithStringUrlReturnsNullWebRequestTracer () {
        // given
        NullAction target = createNullAction();

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://localhost");

        // then
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(NullWebRequestTracer.INSTANCE)));
    }

    @Test
    public void leaveActionReturnsParentAction() {
        // given
        NullAction target = createNullAction();

        // when
        Action obtained = target.leaveAction();

        // then
        assertThat(obtained, is(instanceOf(Action.class)));
        assertThat((Action)obtained, is(sameInstance(mockParent)));
    }

    @Test
    public void leaveActionWithNullParent() {
        // given
        NullAction target = new NullAction(null);

        // when
        Action obtained = target.leaveAction();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void closeDoesNothing() {
        // given
        NullAction target = createNullAction();

        // when
        target.close();

        // then
        verifyZeroInteractions(mockParent);
    }

    private NullAction createNullAction() {
        return new NullAction(mockParent);
    }

}
