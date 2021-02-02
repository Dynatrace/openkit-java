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
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultHttpClientProviderTest {

    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
    }

    @Test
    public void createClientReturnsNewHttpClient() {
        // given
        HTTPClientConfiguration configuration = mock(HTTPClientConfiguration.class);
        when(configuration.getBaseURL()).thenReturn("https://localhost:9999/1");
        when(configuration.getApplicationID()).thenReturn("some cryptic appID");

        DefaultHTTPClientProvider target = new DefaultHTTPClientProvider(mockLogger);

        // when
        HTTPClient obtained = target.createClient(configuration);

        // then
        assertThat(obtained, is(notNullValue()));
    }
}
