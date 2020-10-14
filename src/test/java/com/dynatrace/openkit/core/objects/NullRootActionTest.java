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
import org.junit.Test;

import java.net.URLConnection;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class NullRootActionTest {

    @Test
    public void enterActionReturnsNewNullAction() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.enterAction("action name");

        // then
        assertThat(obtained, instanceOf(NullAction.class));
    }

    @Test
    public void enteredActionHasNullRootActionAsParent() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action action = target.enterAction("action name");
        Action obtained = action.leaveAction();

        // then
        assertThat(obtained, instanceOf(NullRootAction.class));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportEventReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportEvent("event name");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportIntValueReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportValue("value name", 12);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportLongValueReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportValue("value name", Long.MIN_VALUE);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportDoubleValueReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportValue("value name", 37.73);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportStringValueReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportValue("value name", "value");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void deprecatedReportErrorReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportError("error name", 1337, "something bad");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportErrorReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportError("error name", 1337);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportErrorCauseReturnSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportError("error name", "error cause", "error description", "stacktrace");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void reportErrorThrowableReturnsSelf() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.reportError("error name", new IllegalArgumentException());

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
        assertThat((NullRootAction)obtained, is(sameInstance(target)));
    }

    @Test
    public void traceWebRequestWithUrlConnectionReturnsNullWebRequestTracer () {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(NullWebRequestTracer.INSTANCE)));
    }

    @Test
    public void traceWebRequestWithStringUrlReturnsNullWebRequestTracer () {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://localhost");

        // then
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(NullWebRequestTracer.INSTANCE)));
    }

    @Test
    public void leaveActionReturnsNull() {
        // given
        NullRootAction target = NullRootAction.INSTANCE;

        // when
        Action obtained = target.leaveAction();

        // then
        assertThat(obtained, is(nullValue()));
    }
}
