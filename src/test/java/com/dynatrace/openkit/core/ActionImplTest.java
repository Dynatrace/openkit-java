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

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the action having some knowledge of the internals of action, rootaction and beacon.
 */
public class ActionImplTest {

    private Logger logger;

    private final String actionName = "TestAction";

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
    }

    @Test
    public void reportEvent() {
        // create test environment
        final String eventName = "TestEvent";
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final Action retAction = action.reportEvent(eventName);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportEvent(eq(action), eq(eventName));
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(action)));
    }

    @Test
    public void reportEventDoesNothingIfEventNameIsNull() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();
        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when executing the call
        Action obtained = target.reportEvent(null);

        // then
        assertThat(obtained,is(sameInstance((Action)target)));
        verify(logger, times(1)).warning("Action.reportEvent: eventName must not be null or empty");
        verify(beacon, times(0)).reportEvent(org.mockito.Matchers.any(ActionImpl.class),
            anyString());
    }

    @Test
    public void reportEventDoesNothingIfEventNameIsEmpty() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();
        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when executing the call
        Action obtained = target.reportEvent("");

        // then
        assertThat(obtained,is(sameInstance((Action)target)));
        verify(logger, times(1)).warning("Action.reportEvent: eventName must not be null or empty");
        verify(beacon, times(0)).reportEvent(org.mockito.Matchers.any(ActionImpl.class),
            anyString());
    }

    @Test
    public void reportValueIntWithNullNameDoesNotReportValue() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        final Action retAction = target.reportValue(null, 42);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyInt());
        assertThat(retAction, is(sameInstance((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportValue (int): valueName must not be null or empty");
    }

    @Test
    public void reportValueIntWithEmptyNameDoesNotReportValue() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        final Action retAction = target.reportValue("", 42);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyInt());
        assertThat(retAction, is(sameInstance((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportValue (int): valueName must not be null or empty");
    }

    @Test
    public void reportValueIntWithValidValue() {
        // create test environment
        final String valueName = "IntegerValue";
        final int value = 42;
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final Action retAction = action.reportValue(valueName, value);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(eq(action), eq(valueName), eq(value));
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(action)));
    }


    @Test
    public void reportValueDoubleWithNullNameDoesNotReportValue() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        final Action retAction = target.reportValue(null, 42.25);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyDouble());
        assertThat(retAction, is(sameInstance((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportValue (double): valueName must not be null or empty");
    }

    @Test
    public void reportValueDoubleWithEmptyNameDoesNotReportValue() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when reporting integer value
        final Action obtained = target.reportValue("", 42.12345);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyDouble());
        assertThat(obtained, is(sameInstance((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportValue (double): valueName must not be null or empty");
    }

    @Test
    public void reportValueDoubleWithValidValue() {
        // given\
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        final Action obtained = target.reportValue("DoubleValue", 12.3456);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(target, "DoubleValue", 12.3456);
        assertThat(obtained, is(sameInstance((Action)target)));
    }


    @Test
    public void reportValueStringWithValidValue() {
        // create test environment
        final String valueName = "StringValue";
        final String value = "This is a string";
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final Action retAction = action.reportValue(valueName, value);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(eq(action), eq(valueName), eq(value));
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(action)));
    }

    @Test
    public void reportValueStringWithNullNameDoesNotReportValue() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        final Action obtained = target.reportValue(null, "value");

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportValue(org.mockito.Matchers.any(ActionImpl.class), anyString(), anyString());
        assertThat(obtained, is(equalTo((Action)target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportValue (string): valueName must not be null or empty");
    }

    @Test
    public void reportValueStringWithValueNull() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        final Action obtained = target.reportValue("StringValue", null);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportValue(target, "StringValue", null);
        assertThat(obtained, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) obtained, is(equalTo(target)));
    }

    @Test
    public void reportErrorWithAllValuesSet() {
        // create test environment
        final String errorName = "FATAL ERROR";
        final int errorCode = 0x8005037;
        final String reason = "Some reason for this fatal error";
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final Action retAction = action.reportError(errorName, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportError(eq(action), eq(errorName), eq(errorCode), eq(reason));
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(action)));
    }

    @Test
    public void reportErrorWithNullErrorNameDoesNotReportTheError() {
        // given
        final int errorCode = 0x8005037;
        final String reason = "Some reason for this fatal error";
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        final Action retAction = target.reportError(null, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportError(target, null, errorCode, reason);
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportError: errorName must not be null or empty");
    }

    @Test
    public void reportErrorWithEmptyErrorNameDoesNotReportTheError() {
        // given
        final int errorCode = 0x8005037;
        final String reason = "Some reason for this fatal error";
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        final Action retAction = target.reportError(null, errorCode, reason);

        // verify that beacon within the action is called properly
        verify(beacon, times(0)).reportError(target, "", errorCode, reason);
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(target)));

        // verify that a log message has been generated
        verify(logger, times(1)).warning("Action.reportError: errorName must not be null or empty");
    }

    @Test
    public void reportErrorWithEmptyNullErrorReasonDoesReport() {
        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        final Action retAction = target.reportError("errorName", 42, null);

        // verify that beacon within the action is called properly
        verify(beacon, times(1)).reportError(target, "errorName", 42, null);
        assertThat(retAction, is(instanceOf(ActionImpl.class)));
        assertThat((ActionImpl) retAction, is(equalTo(target)));
    }

    @Test
    public void canTraceWebRequestConnection() throws MalformedURLException {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final URLConnection connection = mock(URLConnection.class);
        final String urlStr = "http://example.com/pages/";
        final URL url = new URL(urlStr);
        when(connection.getURL()).thenReturn(url);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final WebRequestTracer traceWebRequest = action.traceWebRequest(connection);

        // verify the returned request
        assertThat(traceWebRequest, instanceOf(WebRequestTracerURLConnection.class));
        final WebRequestTracerURLConnection request = (WebRequestTracerURLConnection) traceWebRequest;
        assertThat(urlStr, is(equalTo(request.getURL()))); // that's possible, since there is no query string in the URL
    }

    @Test
    public void canTraceWebRequestConnectionWithParameters() throws MalformedURLException {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final URLConnection connection = mock(URLConnection.class);
        final String urlStr = "http://example.com/pages/";
        final String parStr = "?someParameter=hello&someOtherParameter=world";
        final URL url = new URL(urlStr + parStr);
        when(connection.getURL()).thenReturn(url);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final WebRequestTracer traceWebRequest = action.traceWebRequest(connection);

        // verify the returned request
        assertThat(traceWebRequest, instanceOf(WebRequestTracerURLConnection.class));
        final WebRequestTracerURLConnection request = (WebRequestTracerURLConnection) traceWebRequest;
        assertThat(urlStr, is(equalTo(request.getURL())));
    }

    @Test
    public void canTraceWebRequestUrl() throws MalformedURLException {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final String urlStr = "http://example.com/pages/";
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final WebRequestTracer traceWebRequest = action.traceWebRequest(urlStr);

        // verify the returned request
        assertThat(traceWebRequest, instanceOf(WebRequestTracerStringURL.class));
        final WebRequestTracerStringURL request = (WebRequestTracerStringURL) traceWebRequest;
        assertThat(urlStr, is(equalTo(request.getURL())));
    }

    @Test
    public void canTraceWebRequestUrlWithParameters() throws MalformedURLException {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final String urlStr = "http://example.com/pages/";
        final String parStr = "someParameter=hello&someOtherParameter=world";
        // Note, that the "?" is encoded (e.g. see https://stackoverflow.com/questions/10786042/java-url-encoding-of-query-string-parameters)
        final String url = urlStr + "?" + parStr;
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final WebRequestTracer traceWebRequest = action.traceWebRequest(url);

        // verify the returned request
        assertThat(traceWebRequest, instanceOf(WebRequestTracerStringURL.class));
        final WebRequestTracerStringURL request = (WebRequestTracerStringURL) traceWebRequest;
        assertThat(urlStr, is(equalTo(request.getURL())));
    }

    @Test
    public void canTraceWebRequestUrlWithParametersUtf8() throws MalformedURLException, UnsupportedEncodingException {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final String urlStr = "http://example.com/pages/";
        final String parStr = "someParameter=hello&someOtherParameter=world";
        // Note, that the "?" is encoded (e.g. see https://stackoverflow.com/questions/10786042/java-url-encoding-of-query-string-parameters)
        final String url = urlStr + "?" + URLEncoder.encode(parStr, "UTF-8");
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final WebRequestTracer traceWebRequest = action.traceWebRequest(url);

        // verify the returned request
        assertThat(traceWebRequest, instanceOf(WebRequestTracerStringURL.class));
        final WebRequestTracerStringURL request = (WebRequestTracerStringURL) traceWebRequest;
        assertThat(urlStr, is(equalTo(request.getURL())));
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {

        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest((String)null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning("Action.traceWebRequest (String): url must not be null or empty");
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {

        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning("Action.traceWebRequest (String): url must not be null or empty");
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {

        // given
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        final ActionImpl target = new ActionImpl(logger, beacon, actionName, actions);

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(logger, times(1)).warning("Action.traceWebRequest (URLConnection): connection must not be null");
    }

    @Test
    public void actionsEnteredAndLeft() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        final Beacon beacon = createTestBeacon();
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create a new parent action
        final ActionImpl parent = new ActionImpl(logger, beacon, actionName, actions);
        final ActionImpl parentImpl = parent;
        assertThat(parentImpl.getID(), is(1));
        assertThat(parentImpl.getParentID(), is(0));
        assertThat(actions.toArrayList().size(), is(1));

        // create child
        final ActionImpl child = new ActionImpl(logger, beacon, actionName, parentImpl, actions);
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
        retAction = parentImpl.leaveAction();
        assertThat(retAction, is(nullValue()));
        assertThat(actions.toArrayList().size(), is(0));
    }

    @Test
    public void leaveAction() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final long ts = System.currentTimeMillis();
        when(beacon.getCurrentTimestamp()).thenReturn(ts);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call: simulate a few reportValues and then leaveAction
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(action.getStartTime(), is(ts));
        assertThat(action.getEndTime(), is(-1L)); // not ended yet
        assertThat(action.getEndSequenceNo(), is(-1)); // not ended yet
        action.reportValue("DoubleValue", 3.141592654);
        action.reportValue("IntValue", 42);
        action.reportValue("StringValue", "nice value!");
        final Action retAction = action.leaveAction();

        // verify
        assertThat(retAction, is(nullValue())); // no parent action -> null
        assertThat(action.getStartTime(), is(ts));
        assertThat(action.getEndTime(), is(ts)); // now the action has ended
        assertThat(action.getEndSequenceNo(), not(-1));
    }

    @Test
    public void leaveActionTwice() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final long ts = System.currentTimeMillis();
        when(beacon.getCurrentTimestamp()).thenReturn(ts);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call: simulate a few reportValues and then leaveAction
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);
        final Action retAction1 = action.leaveAction();
        assertThat(retAction1, is(nullValue())); // no parent action -> null
        final Action retAction2 = action.leaveAction();
        assertThat(retAction2, is(nullValue())); // no parent action -> null
    }

    @Test
    public void verifySequenceNumbersParents() {
        // create test environment: IDs are created by the beacon, thus we cannot simply mock the beacon
        final Beacon beacon = createTestBeacon();
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create two parent action1
        final ActionImpl parent1 = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent1.getStartSequenceNo(), is(1));
        assertThat(parent1.getEndSequenceNo(), is(-1));
        final ActionImpl parent2 = new ActionImpl(logger, beacon, actionName, actions);
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
        final Beacon beacon = createTestBeacon();
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create two parent actions
        final ActionImpl parent1 = new ActionImpl(logger, beacon, actionName, actions);
        assertThat(parent1.getStartSequenceNo(), is(1));
        assertThat(parent1.getEndSequenceNo(), is(-1));
        final ActionImpl parent2 = new ActionImpl(logger, beacon, actionName, actions);
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
        final Beacon beacon = createTestBeacon();
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create parent action "1"
        final ActionImpl parent = new RootActionImpl(logger, beacon, "1", actions);
        assertThat(parent.getStartSequenceNo(), is(1));
        assertThat(parent.getEndSequenceNo(), is(-1));

        // create child "1.1"
        final ActionImpl child1 = new ActionImpl(logger, beacon, "1.1", parent, actions);
        assertThat(child1.getStartSequenceNo(), is(2));
        assertThat(child1.getEndSequenceNo(), is(-1));

        // create child "1.2"
        final ActionImpl child2 = new ActionImpl(logger, beacon, "1.2", parent, actions);
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
        final Beacon beacon = createTestBeacon();
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create parent action "1"
        final RootActionImpl parent = new RootActionImpl(logger, beacon, "1", actions);
        assertThat(parent.getStartSequenceNo(), is(1));
        assertThat(parent.getEndSequenceNo(), is(-1));

        // create child "1.1"
        final Action child1 = parent.enterAction("1.1");
        assertThat(child1, is(instanceOf(ActionImpl.class)));
        final ActionImpl child1impl = (ActionImpl)child1;
        assertThat(child1impl.getStartSequenceNo(), is(2));
        assertThat(child1impl.getEndSequenceNo(), is(-1));

        // create child "1.2"
        final Action child2 = parent.enterAction("1.2");
        assertThat(child2, is(instanceOf(ActionImpl.class)));
        final ActionImpl child2impl = (ActionImpl)child2;
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
        final Beacon beacon = mock(Beacon.class);
        final int sequenceNr = 13;
        final int id = 42;
        when(beacon.createSequenceNumber()).thenReturn(sequenceNr);
        when(beacon.createID()).thenReturn(id);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // execute the test call: simulate a few reportValues and then leaveAction
        final ActionImpl action = new ActionImpl(logger, beacon, actionName, actions);

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
        assertThat(obtained, is(sameInstance((Action)target)));
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
        assertThat(obtained, is(sameInstance((Action)target)));
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
        assertThat(obtained, is(sameInstance((Action)target)));
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
        assertThat(obtained, is(sameInstance((Action)target)));
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
        assertThat(obtained, is(sameInstance((Action)target)));
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

        SynchronizedQueue queue = new SynchronizedQueue<Action>();
        Closeable target = new ActionImpl(logger, mockBeacon, actionName, queue);

        // when
        target.close();

        // then
        assertThat(((ActionImpl)target).getEndTime(), is(equalTo(1234L)));
        assertThat(((ActionImpl)target).getEndSequenceNo(), is(equalTo(42)));

        verify(mockBeacon, times(1)).addAction((ActionImpl)target);
        verify(mockBeacon, times(3)).getCurrentTimestamp();
        verify(mockBeacon, times(2)).createSequenceNumber();
    }

    private Beacon createTestBeacon() {
        final Logger logger = mock(Logger.class);
        final BeaconCacheImpl beaconCache = new BeaconCacheImpl();
        final Configuration configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn("appID");
        when(configuration.getApplicationName()).thenReturn("appName");
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.isCapture()).thenReturn(true);
        final String clientIPAddress = "127.0.0.1";
        final ThreadIDProvider threadIDProvider = mock(ThreadIDProvider.class);
        final TimingProvider timingProvider = mock(TimingProvider.class);
        return new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider);
    }

}
