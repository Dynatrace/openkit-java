/**
 * Copyright 2018-2020 Dynatrace LLC
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
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("resource")
public class WebRequestTracerBaseImplTest {

    private Logger logger;
    private Beacon mockBeacon;
    private OpenKitComposite parentOpenKitObject;

    private static final int SEQUENCE_NUMBER = 1234;
    private static final String TAG = "THE_TAG";

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);

        mockBeacon = mock(Beacon.class);
        parentOpenKitObject = mock(OpenKitComposite.class);
        when(parentOpenKitObject.getActionID()).thenReturn(0);


        when(mockBeacon.createSequenceNumber()).thenReturn(SEQUENCE_NUMBER);
        when(mockBeacon.createTag(anyInt(), anyInt())).thenReturn(TAG);
    }

    @Test
    public void defaultValues() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // then
        assertThat(target.getURL(), is("<unknown>"));
        assertThat(target.getResponseCode(), is(-1));
        assertThat(target.getStartTime(), is(0L));
        assertThat(target.getEndTime(), is(-1L));
        assertThat(target.getStartSequenceNo(), is(SEQUENCE_NUMBER));
        assertThat(target.getEndSequenceNo(), is(-1));
        assertThat(target.getBytesSent(), is(-1));
        assertThat(target.getBytesReceived(), is(-1));
        assertThat(target.getParent(), is(sameInstance(parentOpenKitObject)));

        // and verify that the sequence number was retrieved from beacon, as well as the tag
        verify(mockBeacon, times(1)).createSequenceNumber();
        verify(mockBeacon, times(1)).createTag(0, SEQUENCE_NUMBER);
    }

    @Test
    public void aNewlyCreatedWebRequestTracerDoesNotAttachToTheParent() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // then no interaction with parent has be done
        verify(parentOpenKitObject, times(1)).getActionID();
        verifyNoMoreInteractions(parentOpenKitObject);
    }

    @Test
    public void getTag() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // then
        assertThat(target.getTag(), is(TAG));
    }

    @Test
    public void aNewlyCreatedWebRequestTracerIsNotStopped() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // then
        assertThat(target.isStopped(), is(false));
    }

    @Test
    public void aWebRequestTracerIsStoppedAfterStopHasBeenCalled() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when calling the stop method
        target.stop();

        // then
        assertThat(target.isStopped(), is(true));
    }

    @Test
    public void aWebRequestTracerIsStoppedWithResponseCodeAfterStopHasBeenCalled() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when calling the stop method with response code
        target.stop(200);

        // then
        assertThat(target.isStopped(), is(true));
    }

    @Test
    public void setResponseCodeSetsTheResponseCode() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when setting response code
        WebRequestTracer obtained = target.setResponseCode(418);

        // then
        assertThat(target.getResponseCode(), is(418));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void stopWithResponseCodeSetsTheResponseCode() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when setting response code via stop
       target.stop(418);

        // then
        assertThat(target.getResponseCode(), is(418));
    }

    @Test
    public void setResponseCodeDoesNotSetTheResponseCodeIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        target.stop();

        // when setting response code
        WebRequestTracer obtained = target.setResponseCode(418);

        // then
        assertThat(target.getResponseCode(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void stopWithResponseCodeDoesNotSetTheResponseCodeIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        target.stop(200);

        // when setting stopping with response code
        target.stop(404);


        // then
        assertThat(target.getResponseCode(), is(200));
    }

    @Test
    public void setBytesSentSetsTheNumberOfSentBytes() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when setting the sent bytes
        WebRequestTracer obtained = target.setBytesSent(1234);

        // then
        assertThat(target.getBytesSent(), is(1234));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesSentDoesNotSetAnythingIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        target.stop();

        // when setting the sent bytes
        WebRequestTracer obtained = target.setBytesSent(1234);

        // then
        assertThat(target.getBytesSent(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesSentDoesNotSetAnythingIfStoppedWithResponseCode() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        target.stop(200);

        // when setting the sent bytes
        WebRequestTracer obtained = target.setBytesSent(1234);

        // then
        assertThat(target.getBytesSent(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesReceivedSetsTheNumberOfReceivedBytes() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when setting the received bytes
        WebRequestTracer obtained = target.setBytesReceived(4321);

        // then
        assertThat(target.getBytesReceived(), is(4321));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesReceivedDoesNotSetAnythingIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        target.stop();

        // when setting the received bytes
        WebRequestTracer obtained = target.setBytesReceived(4321);

        // then
        assertThat(target.getBytesReceived(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesReceivedDoesNotSetAnythingIfStoppedWithResponseCode() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        target.stop(200);

        // when setting the received bytes
        WebRequestTracer obtained = target.setBytesReceived(4321);

        // then
        assertThat(target.getBytesReceived(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void startSetsTheStartTime() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.getCurrentTimestamp()).thenReturn(123456789L);

        // when starting web request tracing
        WebRequestTracer obtained = target.start();

        // then
        assertThat(target.getStartTime(), is(123456789L));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void startDoesNothingIfAlreadyStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.getCurrentTimestamp()).thenReturn(123456789L);
        target.stop();

        // when starting web request tracing
        WebRequestTracer obtained = target.start();

        // then
        assertThat(target.getStartTime(), is(0L));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void startDoesNothingIfAlreadyStoppedWithResponseCode() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.getCurrentTimestamp()).thenReturn(123456789L);
        target.stop(200);

        // when starting web request tracing
        WebRequestTracer obtained = target.start();

        // then
        assertThat(target.getStartTime(), is(0L));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void stopCanOnlyBeExecutedOnce() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        // when executed the first time
        target.stop();

        // then
        assertThat(target.getEndSequenceNo(), is(42));
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(0, target);

        // and when executed the second time
        target.stop();

        // then
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(0, target);
    }

    @Test
    public void stopWithResponseCodeCanOnlyBeExecutedOnce() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        // when executed the first time
        target.stop(200);

        // then
        assertThat(target.getEndSequenceNo(), is(42));
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(0, target);

        // and when executed the second time
        target.stop(200);

        // then
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(0, target);
    }

    @Test
    public void stopWithResponseCodeNotifiesParentAndDetachesFromParent() {

        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        // when
        target.stop(200);

        // then
        assertThat(target.getParent(), is(nullValue()));
        verify(parentOpenKitObject, times(1)).onChildClosed(target);
        verify(parentOpenKitObject, times(1)).getActionID();
        verifyNoMoreInteractions(parentOpenKitObject);
    }

    @Test
    public void stopNotifiesParentAndDetachesFromParent() {
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        // when
        target.stop();

        // then
        assertThat(target.getParent(), is(nullValue()));
        verify(parentOpenKitObject, times(1)).onChildClosed(target);
        verify(parentOpenKitObject, times(1)).getActionID();
        verifyNoMoreInteractions(parentOpenKitObject);
    }

    @Test
    public void stopUsesSetResponseCode() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);

        // when setting response code and stopping
        WebRequestTracer obtained = target.setResponseCode(418);
        target.stop();

        // then
        assertThat(target.getResponseCode(), is(418));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void closingAWebRequestStopsIt() throws IOException {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(logger, parentOpenKitObject, mockBeacon);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        // when executed the first time
        target.close();

        // then
        assertThat(target.getEndSequenceNo(), is(42));
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(0, target);
    }

    private static final class TestWebRequestTracerBaseImpl extends WebRequestTracerBaseImpl {

        TestWebRequestTracerBaseImpl(Logger logger, OpenKitComposite parent, Beacon beacon) {
            super(logger, parent, WebRequestTracerBaseImpl.UNKNOWN_URL, beacon);
        }
    }
}
