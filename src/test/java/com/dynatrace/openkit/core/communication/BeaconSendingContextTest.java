package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.core.configuration.HttpClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingContextTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private AbstractConfiguration configuration;
    private HTTPClientProvider httpClientProvider;
    private TimingProvider timingProvider;
    private AbstractBeaconSendingState mockState;

    @Before
    public void setUp() {

        configuration = mock(AbstractConfiguration.class);
        httpClientProvider = mock(HTTPClientProvider.class);
        timingProvider = mock(TimingProvider.class);
        mockState = mock(AbstractBeaconSendingState.class);
    }

    @Test
    public void currentStateIsInitializedAccordingly() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        assertThat(target.getCurrentState(), notNullValue());
        assertThat(target.getCurrentState(), instanceOf(BeaconSendingInitState.class));
    }

    @Test
    public void setCurrentStateChangesState() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.setCurrentState(mockState);

        assertThat(target.getCurrentState(), is(sameInstance(mockState)));
    }

    @Test
    public void executeCurrentStateCallsExecuteOnCurrentState() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        target.setCurrentState(mockState);

        verifyZeroInteractions(mockState);

        target.executeCurrentState();

        verify(mockState, times(1)).doExecute(target);
    }

    @Test
    public void initCompleteSuccessAndWait() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.initCompleted(true);
        boolean obtained = target.waitForInit();

        assertThat(obtained, is(true));
    }

    @Test
    public void requestShutdown() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        assertThat(target.isShutdownRequested(), is(false));

        target.requestShutdown();

        assertThat(target.isShutdownRequested(), is(true));
    }

    @Test
    public void initCompleteFailureAndWait() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.initCompleted(false);
        boolean obtained = target.waitForInit();

        assertThat(obtained, is(false));
    }

    @Test
    public void setAndGetLastOpenSessionBeaconSendTime() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.setLastOpenSessionBeaconSendTime(1234L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(1234L));

        target.setLastOpenSessionBeaconSendTime(5678L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(5678L));
    }

    @Test
    public void setAndGetLastStatusCheckTime() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.setLastStatusCheckTime(1234L);
        assertThat(target.getLastStatusCheckTime(), is(1234L));

        target.setLastStatusCheckTime(5678L);
        assertThat(target.getLastStatusCheckTime(), is(5678L));
    }

    @Test
    public void testGetHTTPClient() {

        HTTPClient mockClient = mock(HTTPClient.class);
        HttpClientConfiguration mockConfiguration = mock(HttpClientConfiguration.class);

        when(configuration.getHttpClientConfig()).thenReturn(mockConfiguration);
        when(httpClientProvider.createClient(any(HttpClientConfiguration.class))).thenReturn(mockClient);

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(configuration, httpClientProvider);

        HTTPClient obtained = target.getHTTPClient();

        assertThat(obtained, notNullValue());
        assertThat(obtained, is(sameInstance(mockClient)));

        verify(configuration, times(1)).getHttpClientConfig();
        verify(httpClientProvider, times(1)).createClient(mockConfiguration);
        verifyNoMoreInteractions(configuration, httpClientProvider);
        verifyZeroInteractions(mockClient, mockConfiguration);
    }

    @Test
    public void getCurrentTimestamp() {

        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(1234567890L);

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(timingProvider);

        long obtained = target.getCurrentTimestamp();

        assertThat(obtained, is(1234567890L));
        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepDefaultTime() throws InterruptedException {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(timingProvider);

        target.sleep();

        verify(timingProvider, times(1)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepWithGivenTime() throws InterruptedException {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(timingProvider);

        target.sleep(1234L);

        verify(timingProvider, times(1)).sleep(1234L);
        verifyNoMoreInteractions(timingProvider);
    }
}
