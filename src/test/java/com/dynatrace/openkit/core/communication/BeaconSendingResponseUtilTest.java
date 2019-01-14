/**
 * Copyright 2018-2019 Dynatrace LLC
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

import com.dynatrace.openkit.protocol.Response;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class BeaconSendingResponseUtilTest {


    @Test
    public void isSuccessfulResponseReturnsFalseIfResponseIsNull() {

        // when, then
        assertThat(BeaconSendingResponseUtil.isSuccessfulResponse(null), is(false));
    }

    @Test
    public void isSuccessfulResponseReturnsFalseIfResponseIsErroneous() {

        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.isErroneousResponse()).thenReturn(true);

        // when
        boolean obtained = BeaconSendingResponseUtil.isSuccessfulResponse(response);

        // then
        assertThat(obtained, is(false));

        // verify invocation
        verify(response, times(1)).isErroneousResponse();
        verifyNoMoreInteractions(response);
    }

    @Test
    public void isSuccessfulResponseReturnsTrueIfResponseIsNotErroneous() {

        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.isErroneousResponse()).thenReturn(false);

        // when
        boolean obtained = BeaconSendingResponseUtil.isSuccessfulResponse(response);

        // then
        assertThat(obtained, is(true));

        // verify invocation
        verify(response, times(1)).isErroneousResponse();
        verifyNoMoreInteractions(response);
    }

    @Test
    public void isTooManyRequestsResponseReturnsFalseIfResponseIsNull() {

        // when, then
        assertThat(BeaconSendingResponseUtil.isTooManyRequestsResponse(null), is(false));
    }

    @Test
    public void isTooManyRequestsResponseReturnsFalseIfResponseCodeIsNotEqualToTooManyRequestsCode() {

        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.getResponseCode()).thenReturn(Response.HTTP_BAD_REQUEST);

        // when
        boolean obtained = BeaconSendingResponseUtil.isTooManyRequestsResponse(response);

        // then
        assertThat(obtained, is(false));

        // verify invocation
        verify(response, times(1)).getResponseCode();
        verifyNoMoreInteractions(response);
    }

    @Test
    public void isTooManyRequestsResponseReturnsTrueIfResponseCodeIndicatesTooManyRequests() {

        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.getResponseCode()).thenReturn(Response.HTTP_TOO_MANY_REQUESTS);

        // when
        boolean obtained = BeaconSendingResponseUtil.isTooManyRequestsResponse(response);

        // then
        assertThat(obtained, is(true));

        // verify invocation
        verify(response, times(1)).getResponseCode();
        verifyNoMoreInteractions(response);
    }
}
