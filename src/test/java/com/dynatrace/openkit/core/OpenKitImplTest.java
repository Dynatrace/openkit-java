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

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link OpenKitImpl} implementation having some knowledge of the sessions.
 */
@SuppressWarnings("resource")
public class OpenKitImplTest {

    private static final String APP_ID = "appID";
    private static final String APP_NAME = "appName";

    private Logger logger;
    private Configuration config;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);

        config = mock(Configuration.class);
        when(config.getApplicationID()).thenReturn(APP_ID);
        when(config.getApplicationName()).thenReturn(APP_NAME);
        when(config.getDevice()).thenReturn(new Device("", "", ""));
        when(config.isCapture()).thenReturn(true);
        final HTTPClientConfiguration httpClientConfig = mock(HTTPClientConfiguration.class);
        when(httpClientConfig.getBaseURL()).thenReturn("http://example.com/");
        when(httpClientConfig.getApplicationID()).thenReturn(APP_ID);
        when(config.getHttpClientConfig()).thenReturn(httpClientConfig);
        final BeaconConfiguration beaconConfigMock = mock(BeaconConfiguration.class);
        when(beaconConfigMock.getDataCollectionLevel()).thenReturn(BeaconConfiguration.DEFAULT_DATA_COLLECTION_LEVEL);
        when(beaconConfigMock.getCrashReportingLevel()).thenReturn(BeaconConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);
        when(beaconConfigMock.getMultiplicity()).thenReturn(BeaconConfiguration.DEFAULT_MULITPLICITY);
        when(config.getBeaconConfiguration()).thenReturn(beaconConfigMock);
    }

    @Test
    public void constructor() {
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        assertThat(openKit.getConfiguration(), is(equalTo(config)));
    }

    @Test
    public void initialize() {
        // create test environment
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        assertThat(openKit.isInitialized(), is(false));

        // now initialize it
        openKit.initialize();

        // shut it down
        openKit.shutdown();
        assertThat(openKit.isInitialized(), is(false));
    }

    @Test(timeout = 3000)
    public void waitForInitCompletionWithZeroTimeout() {
        // create test environment
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
    public void waitForInitCompletionWithNegativeTimeout() {
        // create test environment
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
    @Ignore("Check this test, since it's not a unit test")
    public void canShutdownWhileWaitingToInitialize() throws InterruptedException {
        // create test environment
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
    public void createSession() {
        // create test environment
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);
        openKit.initialize();

        // create a valid session
        final Session session = openKit.createSession("127.0.0.1");
        assertThat(session, notNullValue());
        assertThat(session, instanceOf(SessionImpl.class));
        final SessionImpl sessionImpl = (SessionImpl) session;
        assertThat(sessionImpl.isEmpty(), is(true));
        assertThat(sessionImpl.getEndTime(), is(-1L));
    }

    @Test
    public void createSessionWhenNotInitialized() {
        // create test environment
        final OpenKitImpl openKit = new OpenKitImpl(logger, config);

        // User has forgotten to initialize => shall not throw an exception
        final Session session = openKit.createSession("127.0.0.1");
        assertThat(session, notNullValue());
        assertThat(session, instanceOf(SessionImpl.class));
    }

    @Test
    public void createMultipleSessions() {
        // create test environment
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
        assertThat(session1impl, not(sameInstance(session2impl)));
    }

    @Test
    public void anAlreadyShutdownOpenKitCreatesANullSession() {

        // given
        OpenKitImpl target = new OpenKitImpl(logger, config);
        target.initialize();
        target.shutdown();

        // when
        Session obtained = target.createSession("127.0.0.1");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullSession.class)));
    }
}
