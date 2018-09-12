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

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the action having some knowledge of the internals of action, rootaction and beacon.
 */
@SuppressWarnings("resource")
public class ActionImplTest {

    private Logger logger;

    private String actionName = "TestAction";

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
    }

    @Test
    public void reportEvent() {
        // given
        String eventName = "TestEvent";
        Beacon beacon = mock(Beacon.class);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportEvent(eventName);

        // verify that beacon within the action is called properly
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(sameInstance(target)));

        verify(beacon, times(1)).reportEvent(eq(target), eq(eventName));
    }

    @Test
    public void reportEventDoesNothingIfEventNameIsNull() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();
        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when executing the call
        Action obtained = target.reportEvent(null);

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportEvent: eventName must not be null or empty");
        verify(beacon, times(0)).reportEvent(org.mockito.Matchers.any(ActionImpl.class),
            anyString());
    }

    @Test
    public void reportEventDoesNothingIfEventNameIsEmpty() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();
        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when executing the call
        Action obtained = target.reportEvent("");

        // then
        assertThat(obtained, is(sameInstance((Action) target)));
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportEvent: eventName must not be null or empty"); //
        verify(beacon, times(0)).reportEvent(org.mockito.Matchers.any(ActionImpl.class),
            anyString());
    }

    @Test
    public void reportValueIntWithNullNameDoesNotReportValue() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        Action obtained = target.reportValue(null, 42);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyInt());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportValue (int): valueName must not be null or empty");
    }

    @Test
    public void reportValueIntWithEmptyNameDoesNotReportValue() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        Action obtained = target.reportValue("", 42);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyInt());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportValue (int): valueName must not be null or empty");
    }

    @Test
    public void reportValueIntWithValidValue() {
        // given
        String valueName = "IntegerValue";
        int value = 42;
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportValue(valueName, value);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(eq(target), eq(valueName), eq(value));
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl)obtained, is(sameInstance(target)));
    }


    @Test
    public void reportValueDoubleWithNullNameDoesNotReportValue() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        Action obtained = target.reportValue(null, 42.25);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyDouble());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportValue (double): valueName must not be null or empty");
    }

    @Test
    public void reportValueDoubleWithEmptyNameDoesNotReportValue() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        Action obtained = target.reportValue("", 42.12345);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyDouble());
        assertThat(obtained, is(sameInstance((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportValue (double): valueName must not be null or empty");
    }

    @Test
    public void reportValueDoubleWithValidValue() {
        // given\
        Beacon beacon = mock(Beacon.class);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportValue("DoubleValue", 12.3456);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(target, "DoubleValue", 12.3456);
        assertThat(obtained, is(sameInstance((Action) target)));
    }


    @Test
    public void reportValueStringWithValidValue() {
        // given
        String valueName = "StringValue";
        String value = "This is a string";
        Beacon beacon = mock(Beacon.class);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportValue(valueName, value);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(eq(target), eq(valueName), eq(value));
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));
    }

    @Test
    public void reportValueStringWithNullNameDoesNotReportValue() {
        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportValue(null, "value");

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyString());
        assertThat(obtained, is(equalTo((Action) target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportValue (String): valueName must not be null or empty");
    }

    @Test
    public void reportValueStringWithValueNull() {
        // given
        Beacon beacon = mock(Beacon.class);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportValue("StringValue", null);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(target, "StringValue", null);
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));
    }

    @Test
    public void reportErrorWithAllValuesSet() {
        // given
        String errorName = "FATAL ERROR";
        int errorCode = 0x8005037;
        String reason = "Some reason for this fatal error";
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportError(errorName, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportError(eq(target), eq(errorName), eq(errorCode), eq(reason));
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));
    }

    @Test
    public void reportErrorWithNullErrorNameDoesNotReportTheError() {
        // given
        int errorCode = 0x8005037;
        String reason = "Some reason for this fatal error";
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportError(null, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportError(target, null, errorCode, reason);
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportError: errorName must not be null or empty");
    }

    @Test
    public void reportErrorWithEmptyErrorNameDoesNotReportTheError() {
        // given
        int errorCode = 0x8005037;
        String reason = "Some reason for this fatal error";
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportError(null, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportError(target, "", errorCode, reason);
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] reportError: errorName must not be null or empty");
    }

    @Test
    public void reportErrorWithEmptyNullErrorReasonDoesReport() {
        // given
        Beacon beacon = mock(Beacon.class);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        Action obtained = target.reportError("errorName", 42, null);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportError(target, "errorName", 42, null);
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));
    }

    @Test
    public void traceWebRequestWithValidUrlStringGivesAppropriateTracer() {

        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerStringURL.class)));
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {

        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest((String) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] traceWebRequest (String): url must not be null or empty");
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {

        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] traceWebRequest (String): url must not be null or empty");
    }

    @Test
    public void tracingAStringWebRequestWithInvalidURLIsNotAllowed() {

        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest("foobar/://");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] traceWebRequest (String): url \"foobar/://\" does not have a valid scheme");
    }

    @Test
    public void traceWebRequestWithValidURLConnectionGivesAppropriateTracer() throws MalformedURLException {

        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        URLConnection mockURLConnection = mock(URLConnection.class);
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com"));

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest(mockURLConnection);

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerURLConnection.class)));
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {

        // given
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning(
            "ActionImpl [sn=17, id=0, name=TestAction, pa=no parent] traceWebRequest (URLConnection): connection must not be null");
    }

    // TODO stefan.eberl - 2018-09-10 - Check the following enter/leave tests
    // The number of things done is way too high and needs refactoring

    @Test
    public void actionsEnteredAndLeft() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        Beacon beacon = createTestBeacon();
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create a new parent action
        ActionImpl parent = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent.getID(), is(1));
        assertThat(parent.getParentID(), is(0));
        assertThat(actions.toArrayList().size(), is(1));

        // create child
        ActionImpl child = new ActionImpl(logger, beacon, actionName, parent, actions);
        assertThat(child, is(instanceOf(ActionImpl.class)));
        assertThat(child.getID(), is(2));
        assertThat(child.getParentID(), is(1));
        assertThat(actions.toArrayList().size(), is(2));
        assertThat(actions.toArrayList().get(0), is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) actions.toArrayList().get(0), is(parent));
        assertThat(actions.toArrayList().get(0), is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) actions.toArrayList().get(1), is(child));

        // leave child
        Action retAction = child.leaveAction();
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(parent));

        assertThat(actions.toArrayList().size(), is(1));
        assertThat(actions.toArrayList().get(0), is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) actions.toArrayList().get(0), is(parent));

        // leave parent
        retAction = parent.leaveAction();
        assertThat(retAction, is(nullValue()));
        assertThat(actions.toArrayList().size(), is(0));
    }

    @Test
    public void leaveAction() {
        // create test environment
        Beacon beacon = mock(Beacon.class);
        when(beacon.getSessionNumber()).thenReturn(17);
        long ts = System.currentTimeMillis();
        when(beacon.getCurrentTimestamp()).thenReturn(ts);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call: simulate a few reportValues and then leaveAction
        ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(action.getStartTime(), is(ts));
        assertThat(action.getEndTime(), is(-1L)); // not ended yet
        assertThat(action.getEndSequenceNo(), is(-1)); // not ended yet
        action.reportValue("DoubleValue", 3.141592654);
        action.reportValue("IntValue", 42);
        action.reportValue("StringValue", "nice value!");
        Action retAction = action.leaveAction();

        // verify
        assertThat(retAction, is(nullValue())); // no parent action -> null
        assertThat(action.getStartTime(), is(ts));
        assertThat(action.getEndTime(), is(ts)); // now the action has ended
        assertThat(action.getEndSequenceNo(), not(-1));
    }

    @Test
    public void leaveActionTwice() {
        // create test environment
        Beacon beacon = mock(Beacon.class);
        long ts = System.currentTimeMillis();
        when(beacon.getCurrentTimestamp()).thenReturn(ts);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call: simulate a few reportValues and then leaveAction
        ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        Action retAction1 = action.leaveAction();
        assertThat(retAction1, is(nullValue())); // no parent action -> null
        Action retAction2 = action.leaveAction();
        assertThat(retAction2, is(nullValue())); // no parent action -> null
    }

    @Test
    public void verifySequenceNumbersParents() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        Beacon beacon = createTestBeacon();
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create two parent action1
        ActionImpl parent1 = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent1.getStartSequenceNo(), is(1));
        assertThat(parent1.getEndSequenceNo(), is(-1));
        ActionImpl parent2 = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent2.getStartSequenceNo(), is(2));
        assertThat(parent2.getEndSequenceNo(), is(-1));

        // leave parents (parent1 leaves first) -> this shall set the end sequence numbers
        parent1.leaveAction();
        assertThat(parent1.getStartSequenceNo(), is(1));
        assertThat(parent1.getEndSequenceNo(), is(3));
        parent2.leaveAction();
        assertThat(parent2.getStartSequenceNo(), is(2));
        assertThat(parent2.getEndSequenceNo(), is(4));
    }

    @Test
    public void verifySequenceNumbersParents2() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        Beacon beacon = createTestBeacon();
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create two parent actions
        ActionImpl parent1 = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent1.getStartSequenceNo(), is(1));
        assertThat(parent1.getEndSequenceNo(), is(-1));
        ActionImpl parent2 = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent2.getStartSequenceNo(), is(2));
        assertThat(parent2.getEndSequenceNo(), is(-1));

        // leave parents (parent2 leaves first) -> this shall set the end sequence numbers
        parent2.leaveAction();
        assertThat(parent2.getStartSequenceNo(), is(2));
        assertThat(parent2.getEndSequenceNo(), is(3));
        parent1.leaveAction();
        assertThat(parent1.getStartSequenceNo(), is(1));
        assertThat(parent1.getEndSequenceNo(), is(4));
    }

    @Test
    public void verifySequenceNumbersParentWithTwoChildren() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        Beacon beacon = createTestBeacon();
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create parent action "1"
        ActionImpl parent = new RootActionImpl(logger, beacon, "1", actions);
        assertThat(parent.getStartSequenceNo(), is(1));
        assertThat(parent.getEndSequenceNo(), is(-1));

        // create child "1.1"
        ActionImpl child1 = new ActionImpl(logger, beacon, "1.1", parent, actions);
        assertThat(child1.getStartSequenceNo(), is(2));
        assertThat(child1.getEndSequenceNo(), is(-1));

        // create child "1.2"
        ActionImpl child2 = new ActionImpl(logger, beacon, "1.2", parent, actions);
        assertThat(child2.getStartSequenceNo(), is(3));
        assertThat(child2.getEndSequenceNo(), is(-1));

        // child "1.1" leaves first
        child1.leaveAction();
        assertThat(child1.getStartSequenceNo(), is(2));
        assertThat(child1.getEndSequenceNo(), is(4));

        // child "1.2" leaves
        child2.leaveAction();
        assertThat(child2.getStartSequenceNo(), is(3));
        assertThat(child2.getEndSequenceNo(), is(5));

        // parent leaves
        parent.leaveAction();
        assertThat(parent.getStartSequenceNo(), is(1));
        assertThat(parent.getEndSequenceNo(), is(6));
    }

    @Test
    public void verifySequenceNumbersParentWithTwoChildrenParentLeavesFirst() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        Beacon beacon = createTestBeacon();
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create parent action "1"
        RootActionImpl parent = new RootActionImpl(logger, beacon, "1", actions);
        assertThat(parent.getStartSequenceNo(), is(1));
        assertThat(parent.getEndSequenceNo(), is(-1));

        // create child "1.1"
        Action child1 = parent.enterAction("1.1");
        assertThat(child1, is(instanceOf(ActionImpl.class)));
        ActionImpl child1impl = (ActionImpl) child1;
        assertThat(child1impl.getStartSequenceNo(), is(2));
        assertThat(child1impl.getEndSequenceNo(), is(-1));

        // create child "1.2"
        Action child2 = parent.enterAction("1.2");
        assertThat(child2, is(instanceOf(ActionImpl.class)));
        ActionImpl child2impl = (ActionImpl) child2;
        assertThat(child2impl.getStartSequenceNo(), is(3));
        assertThat(child2impl.getEndSequenceNo(), is(-1));

        // parent leaves first => this also leaves the children
        parent.leaveAction();
        assertThat(parent.getStartSequenceNo(), is(1));
        assertThat(child1impl.getStartSequenceNo(), is(2));
        assertThat(child2impl.getStartSequenceNo(), is(3));
        assertThat(child1impl.getEndSequenceNo(), is(4));
        assertThat(child2impl.getEndSequenceNo(), is(5));
        assertThat(parent.getEndSequenceNo(), is(6));
    }

    @Test
    public void verifyGetters() {
        // create test environment
        Beacon beacon = mock(Beacon.class);
        int sequenceNr = 13;
        int id = 42;
        when(beacon.createSequenceNumber()).thenReturn(sequenceNr);
        when(beacon.createID()).thenReturn(id);
        SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call: simulate a few reportValues and then leaveAction
        ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);

        assertThat(action.getID(), is(id));
        assertThat(action.getName(), is(actionName));
        assertThat(action.getParentID(), is(0));
    }

    @Test
    public void aNewlyCreatedActionIsNotLeft() {

        // given
        ActionImpl target = new ActionImpl(logger, createTestBeacon(), "test", new SynchronizedQueue<Action>());

        // then
        assertThat(target.isActionLeft(), is(false));
    }

    @Test
    public void afterLeavingAnActionItIsLeft() {

        // given
        ActionImpl target = new ActionImpl(logger, createTestBeacon(), "test", new SynchronizedQueue<Action>());

        // when
        target.leaveAction();

        // then
        assertThat(target.isActionLeft(), is(true));
    }

    @Test
    public void reportEventDoesNothingIfActionIsLeft() {

        // given
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
        target.leaveAction();
        beacon.clearData();

        // when
        Action obtained = target.reportEvent("eventName");

        // then
        assertThat(beacon.isEmpty(), is(true));
        assertThat(obtained, is(sameInstance((Action) target)));
    }

    @Test
    public void reportIntValueDoesNothingIfActionIsLeft() {

        // given
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
        target.leaveAction();
        beacon.clearData();

        // when
        int value = 42;
        Action obtained = target.reportValue("intValue", value);

        // then
        assertThat(beacon.isEmpty(), is(true));
        assertThat(obtained, is(sameInstance((Action) target)));
    }

    @Test
    public void reportDoubleValueDoesNothingIfActionIsLeft() {

        // given
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
        target.leaveAction();
        beacon.clearData();

        // when
        double value = 42.0;
        Action obtained = target.reportValue("doubleValue", value);

        // then
        assertThat(beacon.isEmpty(), is(true));
        assertThat(obtained, is(sameInstance((Action) target)));
    }

    @Test
    public void reportStringValueDoesNothingIfActionIsLeft() {

        // given
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
        target.leaveAction();
        beacon.clearData();

        // when
        String value = "42";
        Action obtained = target.reportValue("stringValue", value);

        // then
        assertThat(beacon.isEmpty(), is(true));
        assertThat(obtained, is(sameInstance((Action) target)));
    }

    @Test
    public void reportErrorDoesNothingIfActionIsLeft() {

        // given
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
        target.leaveAction();
        beacon.clearData();

        // when
        Action obtained = target.reportError("teapot", 418, "I'm a teapot");

        // then
        assertThat(beacon.isEmpty(), is(true));
        assertThat(obtained, is(sameInstance((Action) target)));
    }

    @Test
    public void traceWebRequestWithURLConnectionArgumentGivesNullTracerIfActionIsLeft() {

        // given
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
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
        Beacon beacon = createTestBeacon();
        ActionImpl target = new ActionImpl(logger, beacon, "test", new SynchronizedQueue<Action>());
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
        Beacon mockBeacon = mock(Beacon.class);
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        SynchronizedQueue<Action> queue = new SynchronizedQueue<Action>();
        ActionImpl target = new ActionImpl(logger, mockBeacon, actionName, queue);

        // when
        target.close();

        // then
        assertThat(target.getEndTime(), is(equalTo(1234L)));
        assertThat(target.getEndSequenceNo(), is(equalTo(42)));

        verify(mockBeacon, times(1)).addAction(target);
        verify(mockBeacon, times(3)).getCurrentTimestamp();
        verify(mockBeacon, times(2)).createSequenceNumber();
    }

    private Beacon createTestBeacon() {
        Logger logger = mock(Logger.class);
        BeaconCacheImpl beaconCache = new BeaconCacheImpl(logger);
        Configuration configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn("appID");
        when(configuration.getApplicationName()).thenReturn("appName");
        when(configuration.getDeviceID()).thenReturn("deviceID");
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.isCapture()).thenReturn(true);
        BeaconConfiguration mockBeaconConfiguration = mock(BeaconConfiguration.class);
        when(mockBeaconConfiguration.getMultiplicity()).thenReturn(1);
        when(mockBeaconConfiguration.getDataCollectionLevel()).thenReturn(DataCollectionLevel.USER_BEHAVIOR);
        when(mockBeaconConfiguration.getCrashReportingLevel()).thenReturn(CrashReportingLevel.OPT_IN_CRASHES);
        when(mockBeaconConfiguration.isCapturingAllowed()).thenReturn(true);
        when(configuration.getBeaconConfiguration()).thenReturn(mockBeaconConfiguration);
        String clientIPAddress = "127.0.0.1";
        ThreadIDProvider threadIDProvider = mock(ThreadIDProvider.class);
        TimingProvider timingProvider = mock(TimingProvider.class);
        return new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider);
    }

}
