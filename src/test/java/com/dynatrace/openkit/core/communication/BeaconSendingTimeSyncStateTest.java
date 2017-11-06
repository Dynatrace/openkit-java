package com.dynatrace.openkit.core.communication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.core.configuration.HttpClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimeProvider;
import com.dynatrace.openkit.providers.TimingProvider;

public class BeaconSendingTimeSyncStateTest {

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

	@After
	public void tearDown() {
		// reset the TimeProvider
		TimeProvider.initialize(0, false);
	}

	@Test
	public void twentyTimeSyncRequestsAreSentToTheServerBeforeGivingUp() {

		// given
		when(httpClient.sendTimeSyncRequest()).thenReturn(null);
		BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

		// when
		target.execute(stateContext);

		// verify init was not done
		assertThat(TimeProvider.isTimeSynced(), is(false));

		// then verify the number of retries
		verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_RETRY_COUNT)).sendTimeSyncRequest();
		verify(timingProvider, times(BeaconSendingTimeSyncState.TIME_SYNC_RETRY_COUNT)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
		verify(timingProvider, times(2 * BeaconSendingTimeSyncState.TIME_SYNC_RETRY_COUNT)).provideTimestampInMilliseconds();
	}

	@Test
	public void whenTimeSyncIsNotSupportedByTheServerSyncingIsImmediatelyAborted() {

		// given
		String responseOne = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=-5&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=-1";
		String responseTwo = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=-4&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=1";
		String responseThree = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=4&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=-1";
		when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(responseOne, 200))
											  .thenReturn(new TimeSyncResponse(responseTwo, 200))
											  .thenReturn(new TimeSyncResponse(responseThree, 200));

		// when both timestamps are negative
		new BeaconSendingTimeSyncState().execute(stateContext);

		// verify init was not done
		assertThat(TimeProvider.isTimeSynced(), is(false));

		// then verify the number of retries
		verify(httpClient, times(1)).sendTimeSyncRequest();
		verify(timingProvider, times(0)).sleep(anyLong());
		verify(timingProvider, times(2)).provideTimestampInMilliseconds();

		// when first timestamp is negative
		new BeaconSendingTimeSyncState().execute(stateContext);

		// verify init was not done
		assertThat(TimeProvider.isTimeSynced(), is(false));

		// then verify the number of retries
		verify(httpClient, times(2)).sendTimeSyncRequest();
		verify(timingProvider, times(0)).sleep(anyLong());
		verify(timingProvider, times(4)).provideTimestampInMilliseconds();

		// when second timestamp is negative
		new BeaconSendingTimeSyncState().execute(stateContext);

		// verify init was not done
		assertThat(TimeProvider.isTimeSynced(), is(false));

		// then verify the number of retries
		verify(httpClient, times(3)).sendTimeSyncRequest();
		verify(timingProvider, times(0)).sleep(anyLong());
		verify(timingProvider, times(6)).provideTimestampInMilliseconds();
	}

	@Test
	public void successfulTimeSyncInitializesTimeProvider() {

		// given
		String responseOne = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7";
		String responseTwo = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22";
		String responseThree = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41";
		String responseFour = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50";
		String responseFive = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61";

		when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(responseOne, 200))
				.thenReturn(new TimeSyncResponse(responseTwo, 200))
				.thenReturn(new TimeSyncResponse(responseThree, 200))
				.thenReturn(new TimeSyncResponse(responseFour, 200))
				.thenReturn(new TimeSyncResponse(responseFive, 200));

		when(timingProvider.provideTimestampInMilliseconds())
				.thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
				.thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
				.thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
				.thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
				.thenReturn(54L).thenReturn(62L); // times on client side for responseFive --> time sync offset = 2

		BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

		// when being executed
		target.execute(stateContext);

		// verify init was done
		assertThat(TimeProvider.isTimeSynced(), is(true));
		assertThat(TimeProvider.convertToClusterTime(0), is(2L));

		// verify number of method calls
		verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS)).sendTimeSyncRequest();
		verify(timingProvider, times(2 * BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS)).provideTimestampInMilliseconds();

		verifyNoMoreInteractions(timingProvider); // no sleeps
	}
}
