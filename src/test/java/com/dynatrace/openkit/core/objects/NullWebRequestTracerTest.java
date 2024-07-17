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

import com.dynatrace.openkit.api.WebRequestTracer;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class NullWebRequestTracerTest {

    @Test
    public void getTagReturnsEmptyString() {
        // given
        NullWebRequestTracer target = NullWebRequestTracer.INSTANCE;

        // when
        String obtained = target.getTag();

        // then
        assertThat(target.getTag(), is(""));
    }

    @Test
    public void setBytesSentReturnsSelf() {
        // given
        NullWebRequestTracer target = NullWebRequestTracer.INSTANCE;

        // when
        WebRequestTracer obtained = target.setBytesSent(37);

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(target)));
    }

    @Test
    public void setBytesSentLongReturnsSelf() {
        // given
        NullWebRequestTracer target = NullWebRequestTracer.INSTANCE;

        // when
        WebRequestTracer obtained = target.setBytesSent(37L);

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(target)));
    }

    @Test
    public void setBytesReceivedReturnsSelf() {
        // given
        NullWebRequestTracer target = NullWebRequestTracer.INSTANCE;

        // when
        WebRequestTracer obtained = target.setBytesReceived(73);

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(target)));
    }

    @Test
    public void setBytesReceivedLongReturnsSelf() {
        // given
        NullWebRequestTracer target = NullWebRequestTracer.INSTANCE;

        // when
        WebRequestTracer obtained = target.setBytesReceived(73L);

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(target)));
    }

    @Test
    public void startReturnsSelf() {
        // given
        NullWebRequestTracer target = NullWebRequestTracer.INSTANCE;

        // when
        WebRequestTracer obtained = target.start();

        // then
        assertThat(obtained, instanceOf(NullWebRequestTracer.class));
        assertThat((NullWebRequestTracer)obtained, is(sameInstance(target)));
    }
}
