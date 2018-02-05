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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

/**
 * Tests the session implementation having some knowledge of the internals of beacon and beacon cache.
 */
public class SessionImplTest {

    @Test
    public void testConstructors() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();

        // test the constructor call
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // verify default values
        assertThat(session, not(nullValue()));
        assertThat(session.getEndTime(), is(-1L));
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void testEnterActionNull() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // add/enter "null-action"
        final RootAction rootAction = session.enterAction(null);

        // verify that a "null-action" is still valid
        assertThat(rootAction, not(nullValue()));
        rootAction.leaveAction();

        // verify that also if a "null-action" is closed, it is moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
    }

    @Test
    public void testEnterActionNotClosed() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // add/enter one action
        final RootAction rootAction = session.enterAction("Some action");
        assertThat(rootAction, not(nullValue()));

        // verify that (because the actions is still active) it is not in the beacon cache (thus the cache is empty)
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void testEnterActionSingle() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // add/enter one action
        final RootAction rootAction = session.enterAction("Some action");
        rootAction.leaveAction();

        // verify that the action is closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
    }

    @Test
    public void testEnterActionMultiple() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // add/enter two actions
        final RootAction rootAction1 = session.enterAction("Some action 1");
        rootAction1.leaveAction();
        final RootAction rootAction2 = session.enterAction("Some action 2");
        rootAction2.leaveAction();

        // verify that the actions are closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
    }

    @Test
    public void testEnterActionSame() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // add/enter two actions
        final RootAction rootAction1 = session.enterAction("Some action");
        rootAction1.leaveAction();
        final RootAction rootAction2 = session.enterAction("Some action");
        rootAction2.leaveAction();

        // verify that the actions are closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
        // verify that multiple actions with same name are possible
        assertThat(rootAction1, not(rootAction2));
    }

    @Test
    public void testIdentifyUserNull() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // identify a "null-user" must be possible
        session.identifyUser(null);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).identifyUser(isNull(String.class));
    }

    @Test
    public void testIdentifyUserSingle() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // identify a single user
        final String userTag = "Some user";
        session.identifyUser(userTag);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).identifyUser(eq(userTag));
    }

    @Test
    public void testIdentifyUserMultiple() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // identify multiple users
        final String userTag1 = "Some user";
        final String userTag2 = "Some other user";
        final String userTag3 = "Yet another user";
        session.identifyUser(userTag1);
        session.identifyUser(userTag2);
        session.identifyUser(userTag3);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).identifyUser(eq(userTag1));
        verify(beacon, times(1)).identifyUser(eq(userTag2));
        verify(beacon, times(1)).identifyUser(eq(userTag3));
    }

    @Test
    public void testIdentifyUserSame() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // identify the same user twice
        final String userTag = "Some user";
        session.identifyUser(userTag);
        session.identifyUser(userTag);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(2)).identifyUser(eq(userTag));
    }

    @Test
    public void testReportCrashNull() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // report a crash, passing null values
        session.reportCrash(null, null, null);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).reportCrash(isNull(String.class), isNull(String.class), isNull(String.class));
    }

    @Test
    public void testReportCrashSingle() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // report a single crash
        final String errorName = "error name";
        final String reason = "error reason";
        final String stacktrace = "the stacktrace causing the error";
        session.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).reportCrash(eq(errorName), eq(reason), eq(stacktrace));
    }

    @Test
    public void testReportCrashMultiple() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // report multiple crashs
        final String errorName1 = "error name 1";
        final String reason1 = "error reason 1";
        final String stacktrace1 = "the stacktrace causing the error 1";
        session.reportCrash(errorName1, reason1, stacktrace1);
        final String errorName2 = "error name 2";
        final String reason2 = "error reason 2";
        final String stacktrace2 = "the stacktrace causing the error 2";
        session.reportCrash(errorName2, reason2, stacktrace2);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).reportCrash(eq(errorName1), eq(reason1), eq(stacktrace1));
        verify(beacon, times(1)).reportCrash(eq(errorName2), eq(reason2), eq(stacktrace2));
    }

    @Test
    public void testReportCrashSame() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // report the same cache twice
        final String errorName = "error name";
        final String reason = "error reason";
        final String stacktrace = "the stacktrace causing the error";
        session.reportCrash(errorName, reason, stacktrace);
        session.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(2)).reportCrash(eq(errorName), eq(reason), eq(stacktrace));
    }

    @Test
    public void testEnd() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // end the session
        session.end();

        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).getCurrentTimestamp();
        verify(beacon, times(1)).endSession(eq(session));
        verify(beaconSender, times(1)).finishSession(eq(session));
        assertThat(session.getEndTime(), not(-1L));
    }

    @Test
    public void testEndTwice() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // end the session twice
        session.end();
        session.end();

        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).getCurrentTimestamp();
        verify(beacon, times(1)).endSession(eq(session));
        verify(beaconSender, times(1)).finishSession(eq(session));
        assertThat(session.getEndTime(), not(-1L));
    }

    @Test
    public void testEndWithOpenRootActions() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // end the session containing open (=not left) actions
        session.enterAction("Some action 1");
        session.enterAction("Some action 2");
        session.end();

        // mock a valid status response via the HTTPClient to be sure the beacon cache is empty
        final HTTPClient httpClient = mock(HTTPClient.class);
        final StatusResponse statusResponse = new StatusResponse("", 200);
        when(httpClient.sendBeaconRequest(isA(String.class), any(byte[].class))).thenReturn(statusResponse);
        final HTTPClientProvider clientProvider = mock(HTTPClientProvider.class);
        when(clientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        session.sendBeacon(clientProvider);
        // verify that the actions if the action is still active, it is not in the beacon cache (thus cache is empty)
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void testSendBeacon() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(beaconSender, beacon);
        final HTTPClientProvider clientProvider = mock(HTTPClientProvider.class);

        session.sendBeacon(clientProvider);

        // verify the proper methods being called
        verify(beaconSender, times(1)).startSession(eq(session));
        verify(beacon, times(1)).send(eq(clientProvider));
    }

    @Test
    public void testClearCapturedData() {
        // create test environment
        final BeaconSender beaconSender = mock(BeaconSender.class);
        final Beacon beacon = createTestBeacon();
        final SessionImpl session = new SessionImpl(beaconSender, beacon);

        // end the session containing closed actions (moved to the beacon cache)
        final RootAction rootAction1 = session.enterAction("Some action 1");
        rootAction1.leaveAction();
        final RootAction rootAction2 = session.enterAction("Some action 2");
        rootAction2.leaveAction();
        // verify that the actions are closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));

        // clear the captured data
        session.clearCapturedData();

        // verify that the cached items are cleared and the cache is empty
        assertThat(session.isEmpty(), is(true));
    }

    private Beacon createTestBeacon() {
        final Logger logger = mock(Logger.class);
        final BeaconCacheImpl beaconCache = new BeaconCacheImpl();
        final Configuration configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn("appID");
        when(configuration.getApplicationName()).thenReturn("appName");
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.isCapture()).thenReturn(true);
        when(configuration.getMaxBeaconSize()).thenReturn(30 * 1024); // 30kB=default size
        final String clientIPAddress = "127.0.0.1";
        final ThreadIDProvider threadIDProvider = mock(ThreadIDProvider.class);
        final TimingProvider timingProvider = mock(TimingProvider.class);
        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(System.currentTimeMillis());
        return new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider);
    }
}
