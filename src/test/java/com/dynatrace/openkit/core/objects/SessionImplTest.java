/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.objects.NullRootAction;
import com.dynatrace.openkit.core.objects.NullWebRequestTracer;
import com.dynatrace.openkit.core.objects.RootActionImpl;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.core.objects.WebRequestTracerStringURL;
import com.dynatrace.openkit.core.objects.WebRequestTracerURLConnection;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URLConnection;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the session implementation having some knowledge of the internals of beacon and beacon cache.
 */
@SuppressWarnings("resource")
public class SessionImplTest {

    private Logger mockLogger;
    private Beacon mockBeacon;
    private BeaconSender mockBeaconSender;

    @Before
    public void setUp() {

        // mock Logger
        mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        when(mockLogger.isDebugEnabled()).thenReturn(true);

        // mock Beacon
        mockBeacon = mock(Beacon.class);

        // mock BeaconSender
        mockBeaconSender = mock(BeaconSender.class);
    }

    @Test
    public void defaultEndTimeIsSetToMinusOne() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // then
        assertThat(target.getEndTime(), is(equalTo(-1L)));
    }

    @Test
    public void enterActionWithNullActionNameGivesNullRootActionObject() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        RootAction obtained = target.enterAction(null);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] enterAction: actionName must not be null or empty");
    }

    @Test
    public void enterActionWithEmptyActionNameGivesNullRootActionObject() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        RootAction obtained = target.enterAction("");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] enterAction: actionName must not be null or empty");
    }

    @Test
    public void enterActionWithNonEmptyNamesGivesRootAction() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        RootAction obtained = target.enterAction("Some action");

        // then
        assertThat(obtained, is(instanceOf(RootActionImpl.class)));
    }

    @Test
    public void enterActionAlwaysGivesANewInstance() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        RootAction obtainedOne = target.enterAction("Some action");
        RootAction obtainedTwo = target.enterAction("Some action");

        // then
        assertThat(obtainedOne, is(notNullValue()));
        assertThat(obtainedTwo, is(notNullValue()));
        assertThat(obtainedOne, is(not(sameInstance(obtainedTwo))));
    }


    @Test
    public void identifyUserWithNullTagDoesNothing() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.identifyUser(null);

        // then
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] identifyUser: userTag must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserWithEmptyTagDoesNothing() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.identifyUser("");

        // then
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] identifyUser: userTag must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserWithNonEmptyTagReportsUser() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.identifyUser("user");

        // then
        verify(mockLogger, times(0)).warning(anyString());
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).identifyUser("user");
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserMultipleTimesAlwaysCallsBeacon() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.identifyUser("user");
        target.identifyUser("user");

        // then
        verify(mockLogger, times(0)).warning(anyString());
        verify(mockBeacon, times(2)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(2)).identifyUser("user");
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void reportingCrashWithNullErrorNameDoesNotReportAnything() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when reporting a crash, passing null values
        target.reportCrash(null, "some reason", "some stack trace");

        // then verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: errorName must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyZeroInteractions(mockBeacon);
    }

    @Test
    public void reportingCrashWithEmptyErrorNameDoesNotReportAnything() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when reporting a crash, passing empty errorName
        target.reportCrash("", "some reason", "some stack trace");

        // verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: errorName must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyZeroInteractions(mockBeacon);
    }

    @Test
    public void reportingCrashWithNullReasonAndStacktraceWorks() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when reporting a crash, passing null values
        target.reportCrash("errorName", null, null);

        // then verify the correct methods being called
        verify(mockBeacon, times(1)).reportCrash("errorName", null, null);
    }

    @Test
    public void reportingCrashWithEmptyReasonAndStacktraceStringWorks() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when reporting a crash, passing null values
        target.reportCrash("errorName", "", "" );

        // then verify the correct methods being called
        verify(mockBeacon, times(1)).reportCrash("errorName", "", "");
    }

    @Test
    public void reportingCrashWithSameDataMultipleTimesForwardsEachCallToBeacon() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        String errorName = "error name";
        String reason = "error reason";
        String stacktrace = "the stacktrace causing the error";

        // when
        target.reportCrash(errorName, reason, stacktrace);
        target.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(mockBeacon, times(2)).reportCrash(errorName, reason, stacktrace);
    }

    @Test
    public void endSessionSetsTheSessionsEndTime() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.end();

        // then
        assertThat(target.getEndTime(), is(equalTo(1234L)));
        verify(mockBeacon, times(1)).getCurrentTimestamp();
    }

    @Test
    public void endSessionFinishesSessionOnBeacon() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.end();

        // then
        verify(mockBeacon, times(1)).endSession(target);
    }

    @Test
    public void endSessionFinishesSessionOnBeaconSender() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.end();

        // then
        verify(mockBeaconSender, times(1)).finishSession(target);
    }

    @Test
    public void endingAnAlreadyEndedSessionDoesNothing() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L, 4242L);
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when ending a session twice
        target.end();
        target.end();

        // then
        assertThat(target.getEndTime(), is(equalTo(1234L)));
        verify(mockBeacon, times(2)).getCurrentTimestamp();

        verify(mockBeacon, times(1)).endSession(target);
        verify(mockBeaconSender, times(1)).finishSession(target);
    }

    @Test
    public void endingASessionImplicitlyClosesAllOpenRootActions() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        RootActionImpl rootActionOne = (RootActionImpl)target.enterAction("Action 1");
        RootActionImpl rootActionTwo = (RootActionImpl)target.enterAction("Action 2");

        // when
        target.end();

        // then
        verify(mockBeacon, times(1)).addAction(rootActionOne);
        verify(mockBeacon, times(1)).addAction(rootActionTwo);
    }

    @Test
    public void sendBeaconForwardsCallToBeacon() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        HTTPClientProvider clientProvider = mock(HTTPClientProvider.class);

        // when
        target.sendBeacon(clientProvider);

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).send(clientProvider);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void clearCapturedDataForwardsCallToBeacon() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.clearCapturedData();

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).clearData();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void isEmptyForwardsCallToBeacon() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.isEmpty();

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).isEmpty();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void setBeaconConfigurationForwardsCallToBeacon() {
        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        BeaconConfiguration mockBeaconConfiguration = mock(BeaconConfiguration.class);

        // when
        target.setBeaconConfiguration(mockBeaconConfiguration);

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).setBeaconConfiguration(mockBeaconConfiguration);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void getBeaconConfigurationForwardsCallToBeacon() {
        // given
        BeaconConfiguration mockBeaconConfiguration = mock(BeaconConfiguration.class);
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        when(target.getBeaconConfiguration()).thenReturn(mockBeaconConfiguration);

        // when
        BeaconConfiguration obtained = target.getBeaconConfiguration();

        // then verify obtained value
        assertThat(obtained, is(sameInstance(mockBeaconConfiguration)));

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).getBeaconConfiguration();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void aNewlyConstructedSessionIsNotEnded() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when, then
        assertThat(target.isSessionEnded(), is(false));
    }

    @Test
    public void aSessionIsEndedIfEndIsCalled() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when end is called
        target.end();

        // then the session is ended
        assertThat(target.isSessionEnded(), is(true));
    }

    @Test
    public void enterActionGivesNullRootActionIfSessionIsAlreadyEnded() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        target.end();

        // when entering an action on already ended session
        RootAction obtained = target.enterAction("Test");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
    }

    @Test
    public void identifyUserDoesNothingIfSessionIsEnded() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        target.end();

        // when trying to identify a user on an ended session
        target.identifyUser("Jane Doe");

        // then
        verify(mockBeacon, times(0)).identifyUser(anyString());
    }

    @Test
    public void reportCrashDoesNothingIfSessionIsEnded() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        target.end();

        // when trying to identify a user on an ended session
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        verify(mockBeacon, times(0)).reportCrash(anyString(), anyString(), anyString());
    }

    @Test
    public void closeSessionEndsTheSession() {

        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(4321L);
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        target.close();

        // then
        assertThat(target.getEndTime(), is(equalTo(4321L)));
        verify(mockBeacon, times(1)).endSession(target);
        verify(mockBeaconSender, times(1)).finishSession(target);
    }

    @Test
    public void traceWebRequestWithValidUrlStringGivesAppropriateTracer() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerStringURL.class)));
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest((String) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] traceWebRequest (String): url must not be null or empty");
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] traceWebRequest (String): url must not be null or empty");
    }

    @Test
    public void tracingAStringWebRequestWithInvalidURLIsNotAllowed() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("foobar/://");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
            "SessionImpl [sn=0] traceWebRequest (String): url \"foobar/://\" does not have a valid scheme");
    }

    @Test
    public void traceWebRequestWithValidURLConnectionGivesAppropriateTracer() throws MalformedURLException {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerURLConnection.class)));
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
            "SessionImpl [sn=0] traceWebRequest (URLConnection): connection must not be null");
    }

    @Test
    public void traceWebRequestWithURLConnectionArgumentGivesNullTracerIfSessionIsEnded() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void traceWebRequestWithStringArgumentGivesNullTracerIfSessionIsEnded() {

        // given
        SessionImpl target = new SessionImpl(mockLogger, mockBeaconSender, mockBeacon);
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest("http://www.google.com");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }
}
