package com.dynatrace.openkit.core.communication;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingTerminalStateTest {

    private BeaconSendingContext stateContext;

    @Before
    public void setUp() {

        stateContext = mock(BeaconSendingContext.class);
    }

    @Test
    public void isTerminalStateIsTrueForTheTerminalState() {

        // given
        BeaconSendingTerminalState target = new BeaconSendingTerminalState();

        // when/then
        assertThat(target.isTerminalState(), is(true));
    }

    @Test
    public void theShutdownStateIsAlwaysTheSameReference() {

        // given
        BeaconSendingTerminalState target = new BeaconSendingTerminalState();

        // when
        AbstractBeaconSendingState obtained = target.getShutdownState();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(BeaconSendingTerminalState.class)));
        assertThat((BeaconSendingTerminalState) obtained, is(sameInstance(target)));
    }

    @Test
    public void executeRequestsShutdown() {

        // given
        BeaconSendingTerminalState target = new BeaconSendingTerminalState();

        // when executing the state
        target.execute(stateContext);

        // verify shutdown is requested now
        verify(stateContext, times(1)).requestShutdown();
        verify(stateContext, times(1)).isShutdownRequested();
        verifyNoMoreInteractions(stateContext);
    }
}
