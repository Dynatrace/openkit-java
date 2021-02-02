/**
 *   Copyright 2018-2021 Dynatrace LLC
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

import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTracer;
import org.junit.Test;

import java.net.URLConnection;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class NullSessionTest {

    @Test
    public void enterActionReturnsNullRootAction() {
        // given
        NullSession target = NullSession.INSTANCE;

        // when
        RootAction obtained = target.enterAction("action name");

        // then
        assertThat(obtained, instanceOf(NullRootAction.class));
        assertThat((NullRootAction)obtained, is(sameInstance(NullRootAction.INSTANCE)));
    }

    @Test
    public void traceWebRequestWithConnectionUrlReturnsNullWebRequestTracer() {
        // given
        NullSession target = NullSession.INSTANCE;

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(NullWebRequestTracer.INSTANCE)));
    }

    @Test
    public void traceWebRequestWithUrlStringReturnsNullWebRequestTracer() {
        // given
        NullSession target = NullSession.INSTANCE;

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://localhost");

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(NullWebRequestTracer.INSTANCE)));
    }
}
