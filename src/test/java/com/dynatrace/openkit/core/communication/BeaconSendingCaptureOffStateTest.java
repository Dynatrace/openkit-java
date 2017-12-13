package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.providers.TestTimeProvider;
import com.dynatrace.openkit.providers.TimeProvider;
import org.junit.Before;
import org.junit.Test;
import com.dynatrace.openkit.protocol.StatusResponse;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingCaptureOffStateTest {

   // private AbstractBeaconSendingState mockState;
    private BeaconSendingContext mockContext;
    private StatusResponse mockResponse;
    private HTTPClient httpClient;
    private TimeProvider timeProvider;

    @Before
    public void setUp() throws InterruptedException {

        timeProvider.initialize(0, true);

        mockResponse = mock(StatusResponse.class);

        httpClient = mock(HTTPClient.class);
        when(httpClient.sendStatusRequest()).thenReturn(mockResponse);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.getHTTPClient()).thenReturn(httpClient);
    }

    @Test
    public void aBeaconSendingCaptureOffStateIsNotATerminalState(){
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        //verify that BeaconSendingCaptureOffState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingCaptureOffStateHasTerminalStateBeaconSendingFlushSessions(){
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void aBeaconSendingCaptureOffStateTransitionsToTimeSyncStateWhenCapturingNotActive() throws InterruptedException {

        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);
        timeProvider.initialize(0, false);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCapture();

        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(org.mockito.Matchers.anyLong());
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }

    @Test
    public void aBeaconSendingCaptureOffStateTransitionsToCaptureOnStateWhenCapturingActive() throws InterruptedException {

        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(true);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCapture();

        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(org.mockito.Matchers.anyLong());
        //verifyNoMoreInteractions(mockContext);
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void aBeaconSendingCaptureOffStateWaitsForSpecifiedTimeWhenTimeSyncFails() throws InterruptedException{

        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);
        timeProvider.initialize(0, false);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCapture();
        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(org.mockito.Matchers.anyLong());
        // verify that the next time sync operation will follow after a sleep of 7200000 ms
        verify(mockContext, times(1)).sleep(7200000);//wait for two hours
        // verify that after sleeping the transition to BeaconSendingTimeSyncState works
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }
}
