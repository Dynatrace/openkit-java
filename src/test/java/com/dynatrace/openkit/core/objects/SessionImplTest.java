/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.ConnectionType;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.protocol.AdditionalQueryParameters;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.util.json.objects.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.URLConnection;
import java.util.*;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the session implementation having some knowledge of the internals of beacon and beacon cache.
 */
@SuppressWarnings("resource")
public class SessionImplTest {

    private Logger mockLogger;
    private OpenKitComposite mockParent;
    private Beacon mockBeacon;
    private AdditionalQueryParameters mockAdditionalParameters;
    private SupplementaryBasicData mockSupplementaryData;

    @Before
    public void setUp() {

        // mock Logger
        mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        when(mockLogger.isDebugEnabled()).thenReturn(true);

        mockParent = mock(OpenKitComposite.class);

        // mock Beacon
        mockBeacon = mock(Beacon.class);

        mockAdditionalParameters = mock(AdditionalQueryParameters.class);
        mockSupplementaryData = mock(SupplementaryBasicData.class);
    }

    @Test
    public void enterActionWithNullActionNameGivesNullRootActionObject() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction(null);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] enterAction: actionName must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionWithEmptyActionNameGivesNullRootActionObject() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction("");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] enterAction: actionName must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionWithNonEmptyNamesGivesRootAction() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction("Some action");

        // then
        assertThat(obtained, is(instanceOf(RootActionImpl.class)));
    }

    @Test
    public void enterActionAlwaysGivesANewInstance() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtainedOne = target.enterAction("Some action");
        RootAction obtainedTwo = target.enterAction("Some action");

        // then
        assertThat(obtainedOne, is(notNullValue()));
        assertThat(obtainedTwo, is(notNullValue()));
        assertThat(obtainedOne, is(not(sameInstance(obtainedTwo))));
    }

    @Test
    public void enterActionAddsNewlyCreatedActionToTheListOfChildObjects() {
        // given
        SessionImpl target = createSession().build();

        // when entering the first time
        RootAction obtainedOne = target.enterAction("Some action");

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject) obtainedOne))));

        // and when entering a second time
        RootAction obtainedTwo = target.enterAction("Some action");

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Arrays.asList((OpenKitObject) obtainedOne, (OpenKitObject) obtainedTwo))));
    }

    @Test
    public void enterActionLogsInvocation() {
        // given
        String actionName = "Some action";
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction(actionName);

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] enterAction(" + actionName + ")");
        verify(mockLogger, times(1)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void identifyUserWithNullTagReportsUser() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.identifyUser(null);

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] identifyUser(null)");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).identifyUser(null);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserWithEmptyTagReportsUser() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.identifyUser("");

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] identifyUser()");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).identifyUser("");
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserWithNonEmptyTagReportsUser() {
        // given
        SessionImpl target = createSession().build();

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
        SessionImpl target = createSession().build();

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
    public void identifyUserLogsInvocation() {
        // given
        String userTag = "user";
        SessionImpl target = createSession().build();

        // when
        target.identifyUser(userTag);

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] identifyUser(" + userTag + ")");
        verify(mockLogger, times(1)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashWithNullErrorNameDoesNotReportAnything() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash(null, "some reason", "some stack trace");

        // then verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: errorName must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashWithEmptyErrorNameDoesNotReportAnything() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing empty errorName
        target.reportCrash("", "some reason", "some stack trace");

        // verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: errorName must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashWithNullReasonAndStacktraceWorks() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash("errorName", null, null);

        // then verify the correct methods being called
        verify(mockBeacon, times(1)).reportCrash("errorName", null, null);
    }

    @Test
    public void reportingCrashWithEmptyReasonAndStacktraceStringWorks() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash("errorName", "", "");

        // then verify the correct methods being called
        verify(mockBeacon, times(1)).reportCrash("errorName", "", "");
    }

    @Test
    public void reportingCrashWithSameDataMultipleTimesForwardsEachCallToBeacon() {
        // given
        SessionImpl target = createSession().build();

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
    public void reportCrashLogsInvocation() {
        // given
        SessionImpl target = createSession().build();

        String errorName = "error name";
        String reason = "error reason";
        String stacktrace = "the stacktrace causing the error";

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportCrash(" + errorName + ", " + reason + ", " + stacktrace + ")");
    }

    @Test
    public void reportingCrashThrowableWithNullThrowableDoesNotReportAnything() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash(null);

        // then verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: throwable must not be null");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashThrowableWithSameDataMultipleTimesForwardsEachCallToBeacon() {
        // given
        SessionImpl target = createSession().build();

        Throwable crash = new NullPointerException();

        // when
        target.reportCrash(crash);
        target.reportCrash(crash);

        // verify the correct methods being called
        verify(mockBeacon, times(2)).reportCrash(crash);
    }

    @Test
    public void reportCrashThrowableLogsInvocation() {
        // given
        SessionImpl target = createSession().build();

        Throwable crash = new NullPointerException("damn it!");

        // when
        target.reportCrash(crash);

        // verify the correct methods being called
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportCrash(" + crash + ")");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// report mutable supplementary basic data tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportEmptyNetworkTechnology() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportNetworkTechnology("");

        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] reportNetworkTechnology (String): technology must be null or non-empty string");
        verify(mockSupplementaryData, never()).setNetworkTechnology(anyString());
    }

    @Test
    public void reportNullNetworkTechnology() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportNetworkTechnology(null);

        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportNetworkTechnology (String) (null)");
        verify(mockSupplementaryData, times(1)).setNetworkTechnology(null);
    }

    @Test
    public void reportValidNetworkTechnology() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportNetworkTechnology("Test");

        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportNetworkTechnology (String) (Test)");
        verify(mockSupplementaryData, times(1)).setNetworkTechnology("Test");
    }

    @Test
    public void reportEmptyCarrier() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportCarrier("");

        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] reportCarrier (String): carrier must be null or non-empty string");
        verify(mockSupplementaryData, never()).setCarrier(anyString());
    }

    @Test
    public void reportNullCarrier() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportCarrier(null);

        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportCarrier (String) (null)");
        verify(mockSupplementaryData, times(1)).setCarrier(null);
    }

    @Test
    public void reportValidCarrier() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportCarrier("Test");

        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportCarrier (String) (Test)");
        verify(mockSupplementaryData, times(1)).setCarrier("Test");
    }

    @Test
    public void reportNullConnectionType() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportConnectionType(null);

        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportConnectionType (ConnectionType) (null)");
        verify(mockSupplementaryData, times(1)).setConnectionType(null);
    }

    @Test
    public void reportValidConnectionType() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.reportConnectionType(ConnectionType.Lan);

        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportConnectionType (ConnectionType) (Lan)");
        verify(mockSupplementaryData, times(1)).setConnectionType(ConnectionType.Lan);
    }


    @Test
    public void sendBizEventWithNullEventType() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.sendBizEvent(null, new HashMap<String, JSONValue>());

        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] sendBizEvent (String, Map): type must not be null or empty");
        verify(mockBeacon, never()).sendBizEvent(anyString(), ArgumentMatchers.<String, JSONValue>anyMap());
    }

    @Test
    public void sendBizEventWithEmptyEventType() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.sendBizEvent("", new HashMap<String, JSONValue>());

        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] sendBizEvent (String, Map): type must not be null or empty");
        verify(mockBeacon, never()).sendBizEvent(anyString(), ArgumentMatchers.<String, JSONValue>anyMap());
    }

    @Test
    public void sendBizEventWithValidPayload() {
        // given
        SessionImpl target = createSession().build();

        // when
        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("value", JSONStringValue.fromString("MyCustomValue"));
        attributes.put("name", JSONStringValue.fromString("EventName"));

        target.sendBizEvent("EventType", attributes);

        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] sendBizEvent(EventType" + ", " + attributes.toString() + ")");
        verify(mockBeacon, times(1)).sendBizEvent(anyString(), eq(attributes));
    }

    @Test
    public void sendBizEventWithNullArrayValuesInPayload() {
        // given
        SessionImpl target = createSession().build();

        // when
        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("value", JSONStringValue.fromString("MyCustomValue"));
        attributes.put("name", JSONStringValue.fromString("EventName"));

        ArrayList<JSONValue> jsonArray = new ArrayList<>();
        jsonArray.add(JSONNullValue.NULL);
        jsonArray.add(JSONStringValue.fromString("Hello"));
        jsonArray.add(JSONNullValue.NULL);

        attributes.put("arrayWithNull", JSONArrayValue.fromList(jsonArray));

        target.sendBizEvent("EventType", attributes);

        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] sendBizEvent(EventType" + ", " + attributes.toString() + ")");
        verify(mockBeacon, times(1)).sendBizEvent(eq("EventType"),
                eq(attributes));
    }

    @Test
    public void sendBizEventWithNullObjectValuesInPayload() {
        // given
        SessionImpl target = createSession().build();

        // when
        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("value", JSONStringValue.fromString("MyCustomValue"));
        attributes.put("name", JSONStringValue.fromString("EventName"));
        attributes.put("nullValue", JSONNullValue.NULL);

        target.sendBizEvent("EventType", attributes);

        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] sendBizEvent(EventType" + ", " + attributes.toString() + ")");
        verify(mockBeacon, times(1)).sendBizEvent(anyString(), eq(attributes));
    }

    @Test
    public void sendEventWithNullEventName() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.sendEvent(null, new HashMap<String, JSONValue>());

        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] sendEvent (String, Map): name must not be null or empty");
        verify(mockBeacon, never()).sendEvent(anyString(), ArgumentMatchers.<String, JSONValue>anyMap());
    }

    @Test
    public void sendEventWithEmptyEventName() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.sendEvent("", new HashMap<String, JSONValue>());

        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] sendEvent (String, Map): name must not be null or empty");
        verify(mockBeacon, never()).sendEvent(anyString(), ArgumentMatchers.<String, JSONValue>anyMap());
    }

    @Test
    public void sendEventWithValidPayload() {
        // given
        SessionImpl target = createSession().build();

        // when
        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("value", JSONStringValue.fromString("MyCustomValue"));
        attributes.put("name", JSONStringValue.fromString("EventName"));

        target.sendEvent("EventName", attributes);

        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] sendEvent(EventName" + ", " + attributes.toString() + ")");
        verify(mockBeacon, times(1)).sendEvent(anyString(), eq(attributes));
    }

    @Test
    public void sendEventWithNullArrayValuesInPayload() {
        // given
        SessionImpl target = createSession().build();

        // when
        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("value", JSONStringValue.fromString("MyCustomValue"));
        attributes.put("name", JSONStringValue.fromString("EventName"));

        ArrayList<JSONValue> jsonArray = new ArrayList<>();
        jsonArray.add(JSONNullValue.NULL);
        jsonArray.add(JSONStringValue.fromString("Hello"));
        jsonArray.add(JSONNullValue.NULL);

        attributes.put("arrayWithNull", JSONArrayValue.fromList(jsonArray));

        target.sendEvent("EventName", attributes);

        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] sendEvent(EventName" + ", " + attributes.toString() + ")");
        verify(mockBeacon, times(1)).sendEvent(eq("EventName"),
                eq(attributes));
    }

    @Test
    public void sendEventWithNullObjectValuesInPayload() {
        // given
        SessionImpl target = createSession().build();

        // when
        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("value", JSONStringValue.fromString("MyCustomValue"));
        attributes.put("name", JSONStringValue.fromString("EventName"));
        attributes.put("nullValue", JSONNullValue.NULL);

        target.sendEvent("EventName", attributes);

        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] sendEvent(EventName" + ", " + attributes.toString() + ")");
        verify(mockBeacon, times(1)).sendEvent(anyString(), eq(attributes));
    }

    @Test
    public void endSessionFinishesSessionOnBeacon() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        SessionImpl target = spy(createSession().build());

        // when
        target.end();

        // then
        verify(mockBeacon, times(1)).endSession();
        verify(target, times(1)).end(true);
    }

    @Test
    public void endSessionDoesNotFinishSessionOnBeacon() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        SessionImpl target = spy(createSession().build());

        // when
        target.end(false);

        // then
        verify(mockBeacon, times(0)).endSession();
    }

    @Test
    public void endingAnAlreadyEndedSessionDoesNothing() {
        // given
        SessionImpl target = createSession().build();

        // when ending a session twice
        target.end();

        // then
        verify(mockBeacon, times(1)).endSession();
        assertThat(target.getState().isFinished(), is(true));

        reset(mockBeacon);

        // and when
        target.end();

        // then
        verify(mockBeacon, times(0)).endSession();
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void endingASessionImplicitlyClosesAllOpenChildObjects() throws IOException {
        // given
        final OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        SessionImpl target = createSession().build();

        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.end();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();
        verifyNoMoreInteractions(childObjectOne, childObjectTwo);
    }

    @Test
    public void ifChildObjectThrowsIOExceptionWhileBeingClosedExceptionIsLogged() throws IOException {
        // given
        IOException exception = new IOException("oops");
        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectOne).close();
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectTwo).close();

        SessionImpl target = createSession().build();
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.end();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();

        verify(mockLogger, times(2)).error(contains("Caught IOException while closing OpenKitObject"), eq(exception));
    }

    @Test
    public void endLogsInvocation() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.end();

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] end()");
    }

    @Test
    public void tryEndEndsSessionIfNoMoreChildObjects() {
        // given
        SessionImpl target = createSession().build();
        RootAction action = target.enterAction("action");
        WebRequestTracer tracer = target.traceWebRequest("https://localhost");

        // when
        boolean obtained = target.tryEnd();

        // then
        assertThat(obtained, is(false));
        verify(mockBeacon, times(0)).endSession();

        // and when
        action.leaveAction();
        obtained = target.tryEnd();

        // then
        assertThat(obtained, is(false));
        verify(mockBeacon, times(0)).endSession();

        // and when
        tracer.stop(200);
        obtained = target.tryEnd();

        // then
        assertThat(obtained, is(true));
        verify(mockBeacon, times(0)).endSession(); // no end session event is sent
        verify(mockParent, times(1)).onChildClosed(target);

    }

    @Test
    public void tryEndReturnsTrueIfSessionAlreadyEnded() {
        // given
        SessionImpl target = createSession().build();
        target.end();

        // when
        boolean obtained = target.tryEnd();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void tryEndMarksSessionStateAsWasTriedForEndingIfSessionNotClosable() {
        // given
        SessionImpl target = createSession().build();
        target.enterAction("action");

        // when
        boolean obtained = target.tryEnd();

        // then
        assertThat(obtained, is(false));
        assertThat(target.getState().wasTriedForEnding(), is(true));
        assertThat(target.getState().isFinished(), is(false));
    }

    @Test
    public void tryEndDoesNotMarkSessionStateAsWasTriedForEndingIfSessionIsClosable() {
        // given
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.tryEnd();

        // then
        assertThat(obtained, is(true));
        assertThat(target.getState().wasTriedForEnding(), is(false));
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void sendBeaconForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();
        HTTPClientProvider clientProvider = mock(HTTPClientProvider.class);

        // when
        target.sendBeacon(clientProvider, mockAdditionalParameters);

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).send(clientProvider, mockAdditionalParameters);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void clearCapturedDataForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();

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
        SessionImpl target = createSession().build();

        // when
        target.isEmpty();

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).isEmpty();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void initializeServerConfigurationForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();
        ServerConfiguration mockServerConfiguration = mock(ServerConfiguration.class);

        // when
        target.initializeServerConfiguration(mockServerConfiguration);

        // then
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).initializeServerConfiguration(mockServerConfiguration);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void updateServerConfigurationForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();
        ServerConfiguration mockServerConfiguration = mock(ServerConfiguration.class);

        // when
        target.updateServerConfiguration(mockServerConfiguration);

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).updateServerConfiguration(mockServerConfiguration);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void aNewlyCreatedSessionIsNotFinished() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isFinished(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
    }

    @Test
    public void aNewlyCreatedSessionIsNotInStateConfigured() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isConfigured(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
    }

    @Test
    public void aNewlyCreatedSessionIsNotInStateAsWasTriedForEnding() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().wasTriedForEnding(), is(false));
    }

    @Test
    public void aNotConfiguredNotFinishedSessionHasCorrectState() {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isConfigured(), is(false));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isFinished(), is(false));
    }

    @Test
    public void aConfiguredNotFinishedSessionHasCorrectState() {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isConfigured(), is(true));
        assertThat(target.getState().isConfiguredAndOpen(), is(true));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isFinished(), is(false));
    }

    @Test
    public void aNotConfiguredFinishedSessionHasCorrectState() {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();
        target.end();

        // when, then
        assertThat(target.getState().isConfigured(), is(false));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void aConfiguredFinishedSessionHasCorrectState() {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);
        SessionImpl target = createSession().build();
        target.end();

        // then
        assertThat(target.getState().isConfigured(), is(true));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(true));
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void aSessionIsFinishedIfEndIsCalled() {
        // given
        SessionImpl target = createSession().build();

        // when end is called
        target.end();

        // then the session is ended
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void enterActionGivesNullRootActionIfSessionIsAlreadyEnded() {
        // given
        SessionImpl target = createSession().build();
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
        SessionImpl target = createSession().build();
        target.end();

        // when trying to identify a user on an ended session
        target.identifyUser("Jane Doe");

        // then
        verify(mockBeacon, times(0)).identifyUser(anyString());
    }

    @Test
    public void reportCrashDoesNothingIfSessionIsEnded() {
        // given
        SessionImpl target = createSession().build();
        target.end();

        // when trying to identify a user on an ended session
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        verify(mockBeacon, times(0)).reportCrash(anyString(), anyString(), anyString());
    }

    @Test
    public void sendEventDoesNothingIfSessionIsEnded() {
        // given
        SessionImpl target = createSession().build();
        target.end();

        // when trying to identify a user on an ended session
        target.sendEvent("eventName", new HashMap<String, JSONValue>());

        // then
        verify(mockBeacon, times(0)).sendEvent(anyString(), ArgumentMatchers.<String, JSONValue>anyMap());
    }

    @Test
    public void closeSessionEndsTheSession() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(4321L);
        SessionImpl target = spy(createSession().build());

        // when
        target.close();

        // then
        verify(target, times(1)).end();
        verify(mockBeacon, times(1)).endSession();
    }

    @Test
    public void traceWebRequestWithValidUrlStringGivesAppropriateTracer() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerStringURL.class)));
    }

    @Test
    public void traceWebRequestWithValidUrlStringAddsTracerToListOfChildren() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject) obtained))));
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest((String) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] traceWebRequest (String): url must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] traceWebRequest (String): url must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void tracingAStringWebRequestWithInvalidURLIsNotAllowed() {
        // given
        SessionImpl target = createSession().build();

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
    public void traceWebRequestWithStringLogsInvocation() {
        // given
        String url = "https://localhost";
        SessionImpl target = createSession().build();

        // when
        target.traceWebRequest(url);

        // then
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] traceWebRequest (String) (" + url + ")");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void traceWebRequestWithUrlConnectionLogsInvocation() {
        // given
        String connectionString = "connection";
        URLConnection connection = mock(URLConnection.class);
        when(connection.toString()).thenReturn(connectionString);
        SessionImpl target = createSession().build();

        // when
        target.traceWebRequest(connection);

        // then
        verify(mockLogger, times(2)).isDebugEnabled(); // 1 on invocation + 1 in getTag when setting tag on connection
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] traceWebRequest (URLConnection) (" + connection + ")");
    }

    @Test
    public void traceWebRequestWithValidURLConnectionGivesAppropriateTracer() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerURLConnection.class)));
    }

    @Test
    public void traceWebRequestWithValidURLConnectionAddsTracerToListOfChildren() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject) obtained))));
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {

        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
                "SessionImpl [sn=0] traceWebRequest (URLConnection): connection must not be null");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void traceWebRequestWithURLConnectionArgumentGivesNullTracerIfSessionIsEnded() {

        // given
        SessionImpl target = createSession().build();
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
        SessionImpl target = createSession().build();
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest("http://www.google.com");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void onChildClosedRemovesChildFromList() {
        // given
        SessionImpl target = createSession().build();
        OpenKitObject childObject = mock(OpenKitObject.class);
        target.storeChildInList(childObject);

        // when child gets closed
        target.onChildClosed(childObject);

        // then
        assertThat(target.getCopyOfChildObjects(), is(empty()));
    }

    @Test
    public void onChildClosedEndsSessionWithoutChildrenIfInStateWasTriedForEnding() {
        // given
        SessionImpl target = createSession().build();
        OpenKitObject childObject = mock(OpenKitObject.class);
        target.storeChildInList(childObject);

        boolean wasClosed = target.tryEnd();
        assertThat(wasClosed, is(false));
        SessionState state = target.getState();
        assertThat(state.wasTriedForEnding(), is(true));
        assertThat(state.isFinished(), is(false));

        // when
        target.onChildClosed(childObject);

        // then
        assertThat(state.isFinished(), is(true));
        verify(mockParent, times(1)).onChildClosed(target);
    }

    @Test
    public void onChildClosedDoesNotEndSessionWithChildrenIfInStateWasTriedForEnding() {
        // given
        SessionImpl target = createSession().build();
        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        boolean wasClosed = target.tryEnd();
        assertThat(wasClosed, is(false));
        SessionState state = target.getState();
        assertThat(state.wasTriedForEnding(), is(true));
        assertThat(state.isFinished(), is(false));

        // when
        target.onChildClosed(childObjectOne);

        // then
        assertThat(state.isFinished(), is(false));
        verify(mockParent, times(0)).onChildClosed(any(OpenKitObject.class));
    }

    @Test
    public void onChildClosedDoesNotEndSessionIfNotInStateWasTriedForEnding() {
        // given
        SessionImpl target = createSession().build();
        OpenKitObject childObject = mock(OpenKitObject.class);
        target.storeChildInList(childObject);

        // when
        target.onChildClosed(childObject);

        // then
        assertThat(target.getState().isFinished(), is(false));
        verify(mockParent, times(0)).onChildClosed(any(OpenKitObject.class));
    }

    @Test
    public void toStringReturnsAppropriateResult() {
        // given
        when(mockBeacon.getSessionNumber()).thenReturn(21);

        SessionImpl target = createSession().build();

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is(equalTo("SessionImpl [sn=21] ")));
    }

    @Test
    public void aNewSessionCanSendNewSessionRequests() {
        // given
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.canSendNewSessionRequest();

        // then
        assertThat(obtained, is(equalTo(true)));
    }

    @Test
    public void canSendNewSessionRequestIsFalseIfAllRequestsAreUsedUp() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        for (int i = SessionImpl.MAX_NEW_SESSION_REQUESTS; i > 0; i--) {
            assertThat(target.canSendNewSessionRequest(), is(true));

            target.decreaseNumRemainingSessionRequests();
        }

        // then
        assertThat(target.canSendNewSessionRequest(), is(false));
    }

    @Test
    public void isDataSendingAllowedReturnsTrueForConfiguredAndDataCaptureEnabledSession() {
        // given
        when(mockBeacon.isDataCapturingEnabled()).thenReturn(true);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);

        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(true)));
    }

    @Test
    public void isDataSendingAllowedReturnsFalseForNotConfiguredSession() {
        // given
        when(mockBeacon.isDataCapturingEnabled()).thenReturn(true);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void isDataSendingAllowedReturnsFalseForDataCaptureDisabledSession() {
        // given
        when(mockBeacon.isDataCapturingEnabled()).thenReturn(false);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void isDataSendingAllowedReturnsFalseForNotConfiguredAndDataCaptureDisabledSession() {
        // given
        when(mockBeacon.isDataCapturingEnabled()).thenReturn(false);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void enableCaptureDelegatesToBeacon() {
        // given
        SessionImpl target = createSession().build();
        reset(mockBeacon);

        // when
        target.enableCapture();

        // then
        verify(mockBeacon, times(1)).enableCapture();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void disableCaptureDelegatesToBeacon() {
        // given
        SessionImpl target = createSession().build();
        reset(mockBeacon);

        // when
        target.disableCapture();

        // then
        verify(mockBeacon, times(1)).disableCapture();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void getSplitByEventsGracePeriodEndTimeInMillisReturnsMinusOneByDefault() {
        // given
        SessionImpl target = createSession().build();

        // then
        assertThat(target.getSplitByEventsGracePeriodEndTimeInMillis(), is(-1L));
    }

    @Test
    public void getSplitByEventsGracePeriodEndTimeInMillisReturnsPreviouslySetValue() {
        // given
        long endTime = 1234;
        SessionImpl target = createSession().build();

        // when
        target.setSplitByEventsGracePeriodEndTimeInMillis(endTime);

        // then
        assertThat(target.getSplitByEventsGracePeriodEndTimeInMillis(), is(endTime));
    }

    private SessionBuilder createSession() {
        SessionBuilder builder = new SessionBuilder();
        builder.logger = mockLogger;
        builder.parent = mockParent;
        builder.beacon = mockBeacon;
        builder.supplementaryBasicData = mockSupplementaryData;

        return builder;
    }

    private static class SessionBuilder {
        private Logger logger;
        private OpenKitComposite parent;
        private Beacon beacon;
        private SupplementaryBasicData supplementaryBasicData;

        private SessionBuilder with(Logger logger) {
            this.logger = logger;
            return this;
        }

        private SessionBuilder with(OpenKitComposite parent) {
            this.parent = parent;
            return this;
        }

        private SessionBuilder with(Beacon beacon) {
            this.beacon = beacon;
            return this;
        }

        private SessionBuilder with(SupplementaryBasicData supplementaryBasicData) {
            this.supplementaryBasicData = supplementaryBasicData;
            return this;
        }

        private SessionImpl build() {
            return new SessionImpl(logger, parent, beacon, supplementaryBasicData);
        }
    }
}
