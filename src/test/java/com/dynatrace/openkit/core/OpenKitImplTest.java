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

package com.dynatrace.openkit.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;

/**
 * Tests the {@link OpenKitImpl} implementation having some knowledge of the sessions.
 */
public class OpenKitImplTest {

    @Test
    public void testConstructors() {
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();

        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        assertThat(openKit.getConfiguration(), is(equalTo(config)));
    }

    @Test
    public void testInitialize() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        assertThat(openKit.isInitialized(), is(false));

        // now initialize it
        openKit.initialize();

        // shut it down
        openKit.shutdown();
        assertThat(openKit.isInitialized(), is(false));
    }

    @Ignore
    @Test(timeout = 1000)
    public void testWaitForInitCompletion() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // walk through minimum life cycle
        openKit.initialize();
        openKit.waitForInitCompletion(); // blocking call
        openKit.shutdown();
    }

    @Test(timeout = 3000)
    public void testWaitForInitCompletionWithTimeout() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // walk through minimum life cycle
        openKit.initialize();
        final long timeoutMs = 1000;
        final long t1 = System.currentTimeMillis();
        openKit.waitForInitCompletion(timeoutMs);
        final long waitTime = System.currentTimeMillis() - t1;
        // timeout should roughly be matched (within 100ms)
        assertThat(Math.abs(waitTime - timeoutMs), is(lessThan(100L)));
        openKit.shutdown();

        assertThat(openKit.isInitialized(), is(false));
    }

    @Test(timeout = 3000)
    public void testWaitForInitCompletionWithTimeoutZero() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // walk through minimum life cycle with zero time
        openKit.initialize();
        final long timeoutMs = 0;
        final long t1 = System.currentTimeMillis();
        openKit.waitForInitCompletion(timeoutMs);
        final long waitTime = System.currentTimeMillis() - t1;

        // expected behavior is, that we were not waiting at all (tested with 100ms accept time span)
        assertThat(waitTime, is(lessThan(100L)));
        openKit.shutdown();

        assertThat(openKit.isInitialized(), is(false));
    }

    @Test(timeout = 3000)
    public void testWaitForInitCompletionWithTimeoutNegative() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // walk through minimum life cycle with a negative time
        openKit.initialize();
        final long timeoutMs = -100;
        final long t1 = System.currentTimeMillis();
        openKit.waitForInitCompletion(timeoutMs);
        final long waitTime = System.currentTimeMillis() - t1;

        // expected behavior is, that we were not waiting at all (tested with 100ms accept time span)
        assertThat(waitTime, is(lessThan(100L)));
        openKit.shutdown();

        assertThat(openKit.isInitialized(), is(false));
    }

    /**
     * Asynchronously invokes the OpenKit initialization, let it run in a blocking wait and then trigger the shutdown.
     */
    @Test
    public void testWaitForInitCompletionWithShuttingDown() throws InterruptedException {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // asynchronously invokes the OpenKit initialization
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new OpenKitRunner(openKit));

        // shut it down after a second
        Thread.sleep(1000);
        openKit.shutdown();

        assertThat(openKit.isInitialized(), is(false));
    }

    private class OpenKitRunner implements Callable<Void> {
        private final OpenKitImpl openKit;

        public OpenKitRunner(OpenKitImpl openKit) {
            this.openKit = openKit;
        }

        @Override
        public Void call() throws Exception {
            openKit.initialize();
            openKit.waitForInitCompletion(); // blocking call
            return null;
        }
    }

    @Test
    public void testCreateSession() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        openKit.initialize();

        // create a valid session
        final Session session = openKit.createSession("127.0.0.1");
        assertThat(session, not(nullValue()));
        assertThat(session, instanceOf(SessionImpl.class));
        final SessionImpl sessionImpl = (SessionImpl) session;
        assertThat(sessionImpl.isEmpty(), is(true));
        assertThat(sessionImpl.getEndTime(), is(-1L));
    }

    @Test
    public void testCreateSessionWhenNotInitialized() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // User has forgotten to initialize => shall not throw an exception
        final Session session = openKit.createSession("127.0.0.1");
        assertThat(session, not(nullValue()));
        assertThat(session, instanceOf(SessionImpl.class));
    }

    @Test
    public void testCreateSessionMultiple() {
        // create test environment
        final Logger logger = mock(Logger.class);
        final Configuration config = createTestConfig();
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        openKit.initialize();

        // create two valid sessions
        final Session session1 = openKit.createSession("127.0.0.1");
        final Session session2 = openKit.createSession("127.0.0.1");
        assertThat(session1, instanceOf(SessionImpl.class));
        assertThat(session2, instanceOf(SessionImpl.class));
        final SessionImpl session1impl = (SessionImpl)session1;
        final SessionImpl session2impl = (SessionImpl)session2;

        // verify that the two sessions exist and are not the same
        assertThat(session1impl, not(session2impl));
    }

    private Configuration createTestConfig() {
        final String appID = "appID";
        final String appName = "appName";
        final Configuration config = mock(Configuration.class);
        when(config.getApplicationID()).thenReturn(appID);
        when(config.getApplicationName()).thenReturn(appName);
        when(config.getDevice()).thenReturn(new Device("", "", ""));
        when(config.isCapture()).thenReturn(true);
        final HTTPClientConfiguration httpClientConfig = mock(HTTPClientConfiguration.class);
        when(httpClientConfig.getBaseURL()).thenReturn("http://example.com/");
        when(httpClientConfig.getApplicationID()).thenReturn(appID);
        when(config.getHttpClientConfig()).thenReturn(httpClientConfig);
        return config;
    }
}
