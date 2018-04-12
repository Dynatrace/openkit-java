/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.providers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultThreadIDProviderTest {
    @Test
    public void currentThreadIDIsReturned() {
        // given
        ThreadIDProvider provider = new DefaultThreadIDProvider();

        long threadID64 = Thread.currentThread().getId();
        int threadHash = (int)((threadID64 ^ (threadID64 >>> 32)) & 0x7fffffff );
        // then
        assertThat(provider.getThreadID(), is(equalTo(threadHash)));
    }
}
