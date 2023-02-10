/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.AdditionalQueryParameters;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BeaconSendingRequestUtilTest {

    private BeaconSendingContext context;
    private HTTPClient httpClient;
    private StatusResponse response;

    @Before
    public void setUp() {
        context = mock(BeaconSendingContext.class);
        httpClient = mock(HTTPClient.class);
        response = mock(StatusResponse.class);
        when(response.getResponseCode()).thenReturn(StatusResponse.HTTP_OK);
        when(response.isErroneousResponse()).thenReturn(false);

        when(context.getHTTPClient()).thenReturn(httpClient);
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(response);
    }

    @Test
    public void sendStatusRequestIsAbortedWhenShutdownIsRequested() throws InterruptedException {

        // given
        when(response.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(response.isErroneousResponse()).thenReturn(true);
        when(context.isShutdownRequested()).thenReturn(true);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 5, 1000L);

        // then
        assertThat(obtained, is(sameInstance(response)));

        verify(context, times(1)).isShutdownRequested();
        verify(context, times(1)).getHTTPClient();

        verify(httpClient, times(1)).sendStatusRequest(any(AdditionalQueryParameters.class));

        verifyNoMoreInteractions(context, httpClient);
    }

    @Test
    public void sendStatusRequestIsAbortedIfTheNumberOfRetriesIsExceeded() throws InterruptedException {

        // given
        when(response.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(response.isErroneousResponse()).thenReturn(true);
        when(context.isShutdownRequested()).thenReturn(false);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 3, 1000L);

        // then
        assertThat(obtained, is(sameInstance(response)));

        verify(context, times(4)).getHTTPClient();
        verify(context, times(3)).sleep(anyLong());
        verify(httpClient, times(4)).sendStatusRequest(any(AdditionalQueryParameters.class));

        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void sendStatusRequestIsDoneWhenHttpClientReturnsASuccessfulResponse() throws InterruptedException {

        // given
        when(context.isShutdownRequested()).thenReturn(false);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 5, 1000L);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(sameInstance(response)));

        verify(context, times(1)).getHTTPClient();

        verify(httpClient, times(1)).sendStatusRequest(any(AdditionalQueryParameters.class));

        verifyNoMoreInteractions(context, httpClient);
    }

    @Test
    public void sleepTimeIsDoubledBetweenConsecutiveRetries() throws InterruptedException {

        // given
        when(response.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(response.isErroneousResponse()).thenReturn(true);
        when(context.isShutdownRequested()).thenReturn(false);
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(response);
        InOrder inOrder = inOrder(context);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 5, 1000L);

        // then
        assertThat(obtained, is(sameInstance(response)));
        verify(context, times(6)).getHTTPClient();
        verify(httpClient, times(6)).sendStatusRequest(any(AdditionalQueryParameters.class));

        inOrder.verify(context).sleep(1000L);
        inOrder.verify(context).sleep(2000L);
        inOrder.verify(context).sleep(4000L);
        inOrder.verify(context).sleep(8000L);
        inOrder.verify(context).sleep(16000L);
    }

    @Test
    public void sendStatusRequestHandlesNullResponsesSameAsErroneousResponses() throws InterruptedException {

        // given
        when(context.isShutdownRequested()).thenReturn(false);
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(null);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 3, 1000L);

        // then
        assertThat(obtained, is(nullValue()));

        verify(context, times(4)).getHTTPClient();
        verify(context, times(3)).sleep(anyLong());
        verify(httpClient, times(4)).sendStatusRequest(any(AdditionalQueryParameters.class));

        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void sendStatusRequestReturnsTooManyRequestsResponseImmediately() throws InterruptedException {

        // given
        when(response.isErroneousResponse()).thenReturn(true);
        when(response.getResponseCode()).thenReturn(StatusResponse.HTTP_TOO_MANY_REQUESTS);
        when(context.isShutdownRequested()).thenReturn(false);

        // when
        StatusResponse obtained = BeaconSendingRequestUtil.sendStatusRequest(context, 3, 1000L);

        // then
        assertThat(obtained, is(sameInstance(response)));

        verify(context, times(1)).getHTTPClient();
        verify(context, times(0)).sleep(anyLong());
        verify(httpClient, times(1)).sendStatusRequest(any(AdditionalQueryParameters.class));

        verifyNoMoreInteractions(httpClient);
    }

}
