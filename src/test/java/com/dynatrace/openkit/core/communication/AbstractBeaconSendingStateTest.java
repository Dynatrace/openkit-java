package com.dynatrace.openkit.core.communication;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class AbstractBeaconSendingStateTest {

    private AbstractBeaconSendingState mockState;
    private BeaconSendingContext mockContext;

    @Before
    public void setUp() {
        mockState = mock(AbstractBeaconSendingState.class);
        doCallRealMethod().when(mockState).execute(org.mockito.Matchers.any(BeaconSendingContext.class));
        mockContext = mock(BeaconSendingContext.class);
    }

    @Test
    public void aTestBeaconSendingStateExecutes() throws InterruptedException {

        // when calling execute
        mockState.execute(mockContext);

        // then verify doExecute was called
        verify(mockState, times(1)).doExecute(mockContext);

        // also verify that shutdown requested is queried, but nothing else
        verify(mockContext, times(1)).isShutdownRequested();
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void aTestBeaconSendingStateExecutesButIsInterrupted() throws InterruptedException {

        // when calling execute leads to an InterruptedException
        doThrow(new InterruptedException()).when(mockState).doExecute(mockContext);
        mockState.execute(mockContext);

        // then verify doExecute was called
        verify(mockState, times(1)).doExecute(mockContext);

        // also verify that shutdown requested is queried, but nothing else
        verify(mockState, times(1)).onInterrupted(mockContext);
        verify(mockContext, times(1)).isShutdownRequested();
        verify(mockContext, times(1)).requestShutdown();
        verifyNoMoreInteractions(mockContext);
    }
}
