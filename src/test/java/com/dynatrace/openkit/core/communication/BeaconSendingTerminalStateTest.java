package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
    public void executeRequestsShutdown() {

        // verify shutdown is not set before
        assertThat(stateContext.isShutdownRequested(), is(false));

        // execute the state
        BeaconSendingTerminalState target = new BeaconSendingTerminalState();
        target.execute(stateContext);

        // verify shutdown is requested now
        assertThat(stateContext.isShutdownRequested(), is(true));
    }
}
