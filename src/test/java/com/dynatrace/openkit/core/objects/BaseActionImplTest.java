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

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the action having some knowledge of the internals of action, rootaction and beacon.
 */
@SuppressWarnings("resource")
public class BaseActionImplTest {

    private static final String ACTION_NAME = "TestAction";
    private static final int ID_BASE_OFFSET = 1234;

    private Logger logger;
    private Beacon beacon;
    private int nextBeaconId;
    private OpenKitComposite openKitComposite;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);

        nextBeaconId = ID_BASE_OFFSET;
        beacon = mock(Beacon.class);
        when(beacon.createID()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return nextBeaconId++;
            }
        });
        when(beacon.getSessionNumber()).thenReturn(17);

        openKitComposite = mock(OpenKitComposite.class);
    }

    @Test
    public void reportEvent() {
        // given
        String eventName = "TestEvent";
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportEvent(eventName);

        // verify that beacon within the action is called properly
        assertThat(obtained, is(instanceOf(BaseActionImpl.class)));
        assertThat((BaseActionImpl) obtained, is(sameInstance(target)));

        verify(beacon, times(1)).reportEvent(eq(ID_BASE_OFFSET), eq(eventName));
    }

    @Test
    public void reportEventDoesNothingIfEventNameIsNull() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when executing the call
        Action obtained = target.reportEvent(null);

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(logger, times(1)).warning(endsWith("reportEvent: eventName must not be null or empty"));
        verify(beacon, times(0)).reportEvent(anyInt(), anyString());
    }

    @Test
    public void reportEventDoesNothingIfEventNameIsEmpty() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when executing the call
        Action obtained = target.reportEvent("");

        // then
        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(logger, times(1)).warning(endsWith("reportEvent: eventName must not be null or empty"));
        verify(beacon, times(0)).reportEvent(anyInt(), anyString());
    }

    @Test
    public void reportValueIntWithNullNameDoesNotReportValue() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when reporting integer value
        Action obtained = target.reportValue(null, 42);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyInt());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportValue (int): valueName must not be null or empty"));
    }

    @Test
    public void reportValueIntWithEmptyNameDoesNotReportValue() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when reporting integer value
        Action obtained = target.reportValue("", 42);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyInt());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportValue (int): valueName must not be null or empty"));
    }

    @Test
    public void reportValueIntWithValidValue() {
        // given
        String valueName = "IntegerValue";
        int value = 42;

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportValue(valueName, value);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(eq(ID_BASE_OFFSET), eq(valueName), eq(value));
        assertThat(obtained, is(sameInstance((Action)target)));
    }

    @Test
    public void reportValueDoubleWithNullNameDoesNotReportValue() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when reporting integer value
        Action obtained = target.reportValue(null, 42.25);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyDouble());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportValue (double): valueName must not be null or empty"));
    }

    @Test
    public void reportValueDoubleWithEmptyNameDoesNotReportValue() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when reporting integer value
        Action obtained = target.reportValue("", 42.12345);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyDouble());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportValue (double): valueName must not be null or empty"));
    }


    @Test
    public void reportValueDoubleWithValidValue() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportValue("DoubleValue", 12.3456);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(ID_BASE_OFFSET, "DoubleValue", 12.3456);
        assertThat(obtained, is(sameInstance((Action) target)));
    }


    @Test
    public void reportValueStringWithValidValue() {
        // given
        String valueName = "StringValue";
        String value = "This is a string";

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportValue(valueName, value);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(eq(ID_BASE_OFFSET), eq(valueName), eq(value));
        assertThat(obtained, is(sameInstance((Action)target)));
    }

    @Test
    public void reportValueStringWithNullNameDoesNotReportValue() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportValue(null, "value");

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyString());
        assertThat(obtained, is(equalTo((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportValue (String): valueName must not be null or empty"));
    }

    @Test
    public void reportValueStringWithValueNull() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportValue("StringValue", null);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(ID_BASE_OFFSET, "StringValue", null);
        assertThat(obtained, is(sameInstance((Action)target)));
    }

    @Test
    public void reportErrorWithAllValuesSet() {
        // given
        String errorName = "FATAL ERROR";
        int errorCode = 0x8005037;
        String reason = "Some reason for this fatal error";

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportError(errorName, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportError(eq(ID_BASE_OFFSET), eq(errorName), eq(errorCode), eq(reason));
        assertThat(obtained, is(sameInstance((Action)target)));
    }

    @Test
    public void reportErrorWithNullErrorNameDoesNotReportTheError() {
        // given
        int errorCode = 0x8005037;
        String reason = "Some reason for this fatal error";

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportError(null, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportError(ID_BASE_OFFSET, null, errorCode, reason);
        assertThat(obtained, is(sameInstance((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportError: errorName must not be null or empty"));
    }

    @Test
    public void reportErrorWithEmptyErrorNameDoesNotReportTheError() {
        // given
        int errorCode = 0x8005037;
        String reason = "Some reason for this fatal error";

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportError(null, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportError(ID_BASE_OFFSET, "", errorCode, reason);
        assertThat(obtained, is(sameInstance((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(endsWith("reportError: errorName must not be null or empty"));
    }

    @Test
    public void reportErrorWithEmptyNullErrorReasonDoesReport() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        Action obtained = target.reportError("errorName", 42, null);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportError(ID_BASE_OFFSET, "errorName", 42, null);
        assertThat(obtained, is(sameInstance((Action)target)));
    }

    @Test
    public void traceWebRequestWithValidUrlStringGivesAppropriateTracer() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerStringURL.class)));
    }

    @Test
    public void traceWebRequestWithValidUrlStringAttachesWebRequestTracerAsChildObject() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        List<OpenKitObject> childObjects = target.getCopyOfChildObjects();
        assertThat(childObjects, is(equalTo(Collections.singletonList((OpenKitObject)obtained))));
    }


    @Test
    public void onChildClosedRemovesChildFromList() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        OpenKitObject childObject = mock(OpenKitObject.class);
        target.storeChildInList(childObject);

        // when child gets closed
        target.onChildClosed(childObject);

        // then
        assertThat(target.getCopyOfChildObjects(), is(empty()));
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest((String) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(endsWith("traceWebRequest (String): url must not be null or empty"));
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(endsWith("traceWebRequest (String): url must not be null or empty"));
    }

    @Test
    public void tracingAStringWebRequestWithInvalidURLIsNotAllowed() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest("foobar/://");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(endsWith("traceWebRequest (String): url \"foobar/://\" does not have a valid scheme"));
    }

    @Test
    public void traceWebRequestWithValidURLConnectionGivesAppropriateTracer() throws MalformedURLException {
        // given
        URLConnection mockURLConnection = mock(URLConnection.class);
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com"));

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest(mockURLConnection);

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerURLConnection.class)));
    }

    @Test
    public void traceWebRequestWithValidURLConnectionAttachesWebRequestTracerAsChildObject() throws MalformedURLException {
        // given
        URLConnection mockURLConnection = mock(URLConnection.class);
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com"));

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest(mockURLConnection);

        // then
        List<OpenKitObject> childObjects = target.getCopyOfChildObjects();
        assertThat(childObjects, is(equalTo(Collections.singletonList((OpenKitObject)obtained))));
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(endsWith("traceWebRequest (URLConnection): connection must not be null"));
    }

    @Test
    public void parentActionIDIsInitializedInTheConstructor() {
        // given
        when(openKitComposite.getActionID()).thenReturn(666);

        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getParentID(), is(666));
    }

    @Test
    public void idIsInitializedInTheConstructor() {
        // given
        when(beacon.createID()).thenReturn(777);

        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getID(), is(777));
    }

    @Test
    public void nameIsInitializedInTheConstructor() {
        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getName(), is(equalTo(ACTION_NAME)));
    }

    @Test
    public void startTimeIsInitializedInTheConstructor() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(123456L);

        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getStartTime(), is(equalTo(123456L)));
        verify(beacon, times(1)).getCurrentTimestamp();
    }

    @Test
    public void endTimeIsMinusOneForNewlyCreatedAction() {
        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getEndTime(), is(-1L));
    }

    @Test
    public void startSequenceNumberIsInitializedInTheConstructor() {
        // given
        when(beacon.createSequenceNumber()).thenReturn(1234);

        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getStartSequenceNo(), is(equalTo(1234)));
        verify(beacon, times(1)).createSequenceNumber();
    }

    @Test
    public void endSequenceNumberIsMinusOneForNewlyCreatedAction() {
        // when
        BaseActionImpl obtained = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(obtained.getEndSequenceNo(), is(-1));
    }

    @Test
    public void aNewlyCreatedActionIsNotLeft() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // then
        assertThat(target.isActionLeft(), is(false));
    }

    @Test
    public void afterLeavingAnActionItIsLeft() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        target.leaveAction();

        // then
        assertThat(target.isActionLeft(), is(true));
    }

    @Test
    public void leavingAnActionSetsTheEndTime() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L, 5678L, 9012L);

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        target.leaveAction();

        // then
        assertThat(target.getEndTime(), is(5678L));
        verify(beacon, times(2)).getCurrentTimestamp();
    }

    @Test
    public void leavingAnActionSetsTheEndSequenceNumber() {
        // given
        when(beacon.createSequenceNumber()).thenReturn(1, 10, 20);

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        target.leaveAction();

        // then
        assertThat(target.getEndSequenceNo(), is(10));
        verify(beacon, times(2)).createSequenceNumber();
    }

    @Test
    public void leavingAnActionSerializesItself() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        target.leaveAction();

        // then
        verify(beacon, times(1)).addAction(target);
    }

    @Test
    public void leavingAnActionClosesAllChildObjects() throws IOException {
        // given
        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.leaveAction();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();
    }

    @Test
    public void ifChildObjectThrowsIOExceptionWhileBeingClosedExceptionIsLogged() throws IOException {
        // given
        IOException exception = new IOException("oops");
        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectOne).close();
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectTwo).close();

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.leaveAction();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();

        verify(logger, times(2)).error(contains("Caught IOException while closing OpenKitObject"), eq(exception));
    }

    @Test
    public void leavingAnActionNotifiesTheParentCompositeObject() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        reset(openKitComposite);

        // when
        target.leaveAction();

        // then
        verify(openKitComposite, times(1)).onChildClosed(target);
        verifyNoMoreInteractions(openKitComposite);
    }

    @Test
    public void leavingAnActionReturnsTheParentAction() {
        // given
        Action parentAction = mock(Action.class);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon, parentAction);

        // when
        Action obtained = target.leaveAction();

        // then
        assertThat(obtained, is(sameInstance(parentAction)));
    }

    @Test
    public void leavingAnAlreadyLeftActionReturnsTheParentAction() {
        // given
        Action parentAction = mock(Action.class);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon, parentAction);
        target.leaveAction(); // leaving the first time

        // when leaving a second time
        Action obtained = target.leaveAction();

        // then
        assertThat(obtained, is(sameInstance(parentAction)));
    }

    @Test
    public void leavingAnAlreadyLeftActionReturnsImmediately() {
        // given
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction(); // leaving the first time
        reset(beacon, openKitComposite);

        // when
        target.leaveAction();

        // then
        verifyZeroInteractions(beacon, openKitComposite);
    }

    @Test
    public void reportEventDoesNothingIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        Action obtained = target.reportEvent("eventName");

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(beacon, times(0)).reportEvent(anyInt(), anyString());
    }

    @Test
    public void reportIntValueDoesNothingIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        int value = 42;
        Action obtained = target.reportValue("intValue", value);

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyInt());
    }

    @Test
    public void reportDoubleValueDoesNothingIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        double value = 42.0;
        Action obtained = target.reportValue("doubleValue", value);

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyDouble());
    }

    @Test
    public void reportStringValueDoesNothingIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        String value = "42";
        Action obtained = target.reportValue("stringValue", value);

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(beacon, times(0)).reportValue(anyInt(), anyString(), anyString());
    }

    @Test
    public void reportErrorDoesNothingIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        Action obtained = target.reportError("teapot", 418, "I'm a teapot");

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(beacon, times(0)).reportError(anyInt(), anyString(), anyInt(), anyString());
    }

    @Test
    public void traceWebRequestWithURLConnectionArgumentGivesNullTracerIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void traceWebRequestWithStringArgumentGivesNullTracerIfActionIsLeft() {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);
        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);
        target.leaveAction();

        // when
        WebRequestTracer obtained = target.traceWebRequest("http://www.google.com");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void closeActionLeavesTheAction() throws IOException {
        // given
        when(beacon.getCurrentTimestamp()).thenReturn(1234L);
        when(beacon.createSequenceNumber()).thenReturn(42);

        BaseActionImpl target = new StubBaseActionImpl(logger, openKitComposite, ACTION_NAME, beacon);

        // when
        target.close();

        // then
        assertThat(target.getEndTime(), is(equalTo(1234L)));
        assertThat(target.getEndSequenceNo(), is(equalTo(42)));

        verify(beacon, times(1)).addAction(target);
        verify(beacon, times(2)).getCurrentTimestamp();
        verify(beacon, times(2)).createSequenceNumber();
    }

    private static final class StubBaseActionImpl extends BaseActionImpl {

        private final Action parentAction;

        StubBaseActionImpl(Logger logger, OpenKitComposite parent, String name, Beacon beacon) {
            this(logger, parent, name, beacon, null);
        }

        StubBaseActionImpl(Logger logger, OpenKitComposite parent, String name, Beacon beacon, Action parentAction) {
            super(logger, parent, name, beacon);
            this.parentAction = parentAction;
        }

        @Override
        protected Action getParentAction() {
            return parentAction;
        }
    }
}
