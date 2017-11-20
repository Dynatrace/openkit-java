package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingRequestUtilTest {

    private BeaconSendingContext context;
    private HTTPClient httpClient;
    private StatusResponse response;

    @Before
    public void setUp() {
        context = mock(BeaconSendingContext.class);
        httpClient = mock(HTTPClient.class);
        response = mock(StatusResponse.class);

        when(context.getHTTPClient()).thenReturn(httpClient);
    }

    @Test
    public void sendStatusRequestIsAbortedWhenShutdownIsRequested() throws InterruptedException {

        // given
        when(context.isShutdownRequested()).thenReturn(true);
        when(httpClient.sendStatusRequest()).thenReturn(null);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 5, 1000L);

        // then
        assertThat(obtained, is(nullValue()));

        verify(context, times(1)).isShutdownRequested();
        verify(context, times(1)).getHTTPClient();

        verify(httpClient, times(1)).sendStatusRequest();

        verifyNoMoreInteractions(context, httpClient);
    }

    @Test
    public void sendStatusRequestIsAbortedIfTheNumberOfRetriesIsExceeded() throws InterruptedException {

        // given
        when(context.isShutdownRequested()).thenReturn(false);
        when(httpClient.sendStatusRequest()).thenReturn(null);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 3, 1000L);

        // then
        assertThat(obtained, is(nullValue()));

        verify(context, times(4)).getHTTPClient();
        verify(context, times(3)).sleep(anyLong());
        verify(httpClient, times(4)).sendStatusRequest();

        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void sendStatusRequestIsDoneWhenHttpClientReturnsANonNullResponse() throws InterruptedException {

        // given
        when(context.isShutdownRequested()).thenReturn(false);
        when(httpClient.sendStatusRequest()).thenReturn(response);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 5, 1000L);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(sameInstance(response)));

        verify(context, times(1)).getHTTPClient();

        verify(httpClient, times(1)).sendStatusRequest();

        verifyNoMoreInteractions(context, httpClient);
    }

    @Test
    public void sleepTimeIsDoubledBetweenConsecutiveRetries() throws InterruptedException {

        // given
        when(context.isShutdownRequested()).thenReturn(false);
        when(httpClient.sendStatusRequest()).thenReturn(null);
        InOrder inOrder = inOrder(context);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 5, 1000L);

        // then
        assertThat(obtained, is(nullValue()));
        verify(context, times(6)).getHTTPClient();
        verify(httpClient, times(6)).sendStatusRequest();

        inOrder.verify(context).sleep(1000L);
        inOrder.verify(context).sleep(2000L);
        inOrder.verify(context).sleep(4000L);
        inOrder.verify(context).sleep(8000L);
        inOrder.verify(context).sleep(16000L);
    }


}
