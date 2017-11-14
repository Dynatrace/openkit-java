package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;

public class BeaconSendingTerminalStateTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private BeaconSendingContext stateContext;

    @Before
    public void setUp() {

        AbstractConfiguration configuration = mock(AbstractConfiguration.class);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        TimingProvider timingProvider = mock(TimingProvider.class);
        stateContext = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
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
        assertThat((BeaconSendingTerminalState)obtained, is(sameInstance(target)));
    }

    @Test
    public void executeRequestsShutdown() {

        // verify shutdown is not set before
        assertThat(stateContext.isShutdownRequested(), is(false));

        // when executing the state
        BeaconSendingTerminalState target = new BeaconSendingTerminalState();
        target.doExecute(stateContext);

        // verify shutdown is requested now
        assertThat(stateContext.isShutdownRequested(), is(true));
    }
}
