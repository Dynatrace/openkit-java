package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.core.configuration.HttpClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class TestBeaconSendingInitState {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private HTTPClient httpClient;
    private TimingProvider timingProvider;
    private BeaconSendingContext stateContext;

    @Before
    public void setUp() {

        AbstractConfiguration configuration = mock(AbstractConfiguration.class);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        httpClient = mock(HTTPClient.class);
        HttpClientConfiguration httpClientConfiguration = mock(HttpClientConfiguration.class);
        timingProvider = mock(TimingProvider.class);
        stateContext = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        when(configuration.getHttpClientConfig()).thenReturn(httpClientConfiguration);
        when(httpClientProvider.createClient(any(HttpClientConfiguration.class))).thenReturn(httpClient);
    }

    @Test
    public void stateInitializesLastOpenSessionBeaconSendTimeAndLastStatusCheckTime() {

        assertThat(stateContext.getLastOpenSessionBeaconSendTime(), is(0L));
        assertThat(stateContext.getLastStatusCheckTime(), is(0L));

        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(123456789L);

        // immediately request shutdown - times are set anyway
        stateContext.requestShutdown();

        BeaconSendingInitState target = new BeaconSendingInitState();

        // execute
        target.execute(stateContext);

        // verify
        assertThat(stateContext.getLastOpenSessionBeaconSendTime(), is(123456789L));
        assertThat(stateContext.getLastStatusCheckTime(), is(123456789L));

        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        verify(timingProvider, times(1)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
    }

    @Test
    public void initialStatusRequestIsTriedSeveralTimesBeforeGivingUp() {

        // always return null
        when(httpClient.sendStatusRequest()).thenReturn(null);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // some checks before execution
        assertThat(stateContext.isShutdownRequested(), is(false));
        assertThat(stateContext.getCurrentState(), is(instanceOf(BeaconSendingInitState.class)));
        verifyZeroInteractions(httpClient);

        // execute
        target.execute(stateContext);

        // verify
        assertThat(stateContext.isShutdownRequested(), is(true));
        assertThat(stateContext.getCurrentState(), is(instanceOf(BeaconSendingTerminalState.class)));

        verify(httpClient, times(BeaconSendingInitState.MAX_INITIAL_STATUS_REQUEST_RETRIES)).sendStatusRequest();
        verifyNoMoreInteractions(httpClient);

        verify(timingProvider, times(BeaconSendingInitState.MAX_INITIAL_STATUS_REQUEST_RETRIES)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
    }

    @Test
    public void initialStatusRequestGivesUpWhenShutdownRequestIsSetDuringExecution() {

        // always return null
        when(httpClient.sendStatusRequest()).thenReturn(null).thenReturn(null).then(new Answer<StatusResponse>() {
            @Override
            public StatusResponse answer(InvocationOnMock invocation) throws Throwable {
                stateContext.requestShutdown();
                return null;
            }
        });

        BeaconSendingInitState target = new BeaconSendingInitState();

        // some checks before execution
        assertThat(stateContext.isShutdownRequested(), is(false));
        assertThat(stateContext.getCurrentState(), is(instanceOf(BeaconSendingInitState.class)));
        verifyZeroInteractions(httpClient);

        // execute
        target.execute(stateContext);

        // verify
        assertThat(stateContext.isShutdownRequested(), is(true));
        assertThat(stateContext.getCurrentState(), is(instanceOf(BeaconSendingTerminalState.class)));

        verify(httpClient, times(3)).sendStatusRequest();
        verifyNoMoreInteractions(httpClient);

        verify(timingProvider, times(3)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
    }
}
