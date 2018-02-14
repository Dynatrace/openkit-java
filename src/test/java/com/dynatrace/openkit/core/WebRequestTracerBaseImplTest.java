package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class WebRequestTracerBaseImplTest {

    private Beacon mockBeacon;
    private ActionImpl mockActionImpl;

    private static final int SEQUENCE_NUMBER = 1234;
    private static final String TAG = "THE_TAG";

    @Before
    public void setUp() {
        mockBeacon = mock(Beacon.class);
        mockActionImpl = mock(ActionImpl.class);

        when(mockBeacon.createSequenceNumber()).thenReturn(SEQUENCE_NUMBER);
        when(mockBeacon.createTag(org.mockito.Matchers.any(ActionImpl.class), anyInt())).thenReturn(TAG);
    }

    @Test
    public void defaultValues() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // then
        assertThat(target.getURL(), is("<unknown>"));
        assertThat(target.getResponseCode(), is(-1));
        assertThat(target.getStartTime(), is(-1L));
        assertThat(target.getEndTime(), is(-1L));
        assertThat(target.getStartSequenceNo(), is(SEQUENCE_NUMBER));
        assertThat(target.getEndSequenceNo(), is(-1));
        assertThat(target.getBytesSent(), is(-1));
        assertThat(target.getBytesReceived(), is(-1));

        // and verify that the sequence number was retrieved from beacon, as well as the tag
        verify(mockBeacon, times(1)).createSequenceNumber();
        verify(mockBeacon, times(1)).createTag(mockActionImpl, SEQUENCE_NUMBER);
    }

    @Test
    public void getTag() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // then
        assertThat(target.getTag(), is(TAG));
    }

    @Test
    public void aNewlyCreatedWebRequestTracerIsNotStopped() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // then
        assertThat(target.isStopped(), is(false));
    }

    @Test
    public void aWebRequestTracerIsStoppedAfterStopHasBeenCalled() {
        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // when calling the stop method
        target.stop();

        // then
        assertThat(target.isStopped(), is(true));
    }

    @Test
    public void setResponseCodeSetsTheResponseCode() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // when setting response code
        WebRequestTracer obtained = target.setResponseCode(418);

        // then
        assertThat(target.getResponseCode(), is(418));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setResponseCodeDoesNotSetTheResponseCodeIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);
        target.stop();

        // when setting response code
        WebRequestTracer obtained = target.setResponseCode(418);

        // then
        assertThat(target.getResponseCode(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesSentSetsTheNumberOfSentBytes() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // when setting the sent bytes
        WebRequestTracer obtained = target.setBytesSent(1234);

        // then
        assertThat(target.getBytesSent(), is(1234));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesSentDoesNotSetAnythingIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);
        target.stop();

        // when setting the sent bytes
        WebRequestTracer obtained = target.setBytesSent(1234);

        // then
        assertThat(target.getBytesSent(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesReceivedSetsTheNumberOfReceivedBytes() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);

        // when setting the received bytes
        WebRequestTracer obtained = target.setBytesReceived(4321);

        // then
        assertThat(target.getBytesReceived(), is(4321));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void setBytesReceivedDoesNotSetAnythingIfStopped() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);
        target.stop();

        // when setting the received bytes
        WebRequestTracer obtained = target.setBytesReceived(4321);

        // then
        assertThat(target.getBytesReceived(), is(-1));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void startSetsTheStartTime() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);
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
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);
        when(mockBeacon.getCurrentTimestamp()).thenReturn(123456789L);
        target.stop();

        // when starting web request tracing
        WebRequestTracer obtained = target.start();

        // then
        assertThat(target.getStartTime(), is(-1L));
        assertThat(obtained, is(sameInstance((WebRequestTracer)target)));
    }

    @Test
    public void stopCanOnlyBeExecutedOnce() {

        // given
        WebRequestTracerBaseImpl target = new TestWebRequestTracerBaseImpl(mockBeacon, mockActionImpl);
        when(mockBeacon.createSequenceNumber()).thenReturn(42);

        // when executed the first time
        target.stop();

        // then
        assertThat(target.getEndSequenceNo(), is(42));
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(mockActionImpl, target);

        // and when executed the second time
        target.stop();

        // then
        verify(mockBeacon, times(2)).createSequenceNumber();
        verify(mockBeacon, times(1)).addWebRequest(mockActionImpl, target);
    }

    private static final class TestWebRequestTracerBaseImpl extends WebRequestTracerBaseImpl {

        public TestWebRequestTracerBaseImpl(Beacon beacon, ActionImpl action) {
            super(beacon, action);
        }
    }
}
