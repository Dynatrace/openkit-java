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

import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SessionWrapperTest {

    private SessionImpl mockSessionImpl;

    @Before
    public void setUp() {
        mockSessionImpl = mock(SessionImpl.class);
    }

    @Test
    public void byDefaultBeaconConfigurationIsNotSet() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // then
        assertThat(target.isBeaconConfigurationSet(), is(false));
    }

    @Test
    public void afterUpdatingTheBeaconConfigurationTheBeaconConfigurationIsSet() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        BeaconConfiguration newConfiguration = new BeaconConfiguration(42);

        // when updating
        target.updateBeaconConfiguration(newConfiguration);

        // then
        assertThat(target.isBeaconConfigurationSet(), is(true));

        // also verify that SessionImpl has been invoked
        verify(mockSessionImpl, times(1)).setBeaconConfiguration(newConfiguration);
        verifyNoMoreInteractions(mockSessionImpl);
    }

    @Test
    public void byDefaultTheSessionIsNotFinished() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // then
        assertThat(target.isSessionFinished(), is(false));
    }

    @Test
    public void theSessionIsFinishedAfterCallingFinishSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // when
        target.finishSession();

        // then
        assertThat(target.isSessionFinished(), is(true));
    }

    @Test
    public void aDefaultConstructedSessionWrapperCanSendRequests() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // then
        assertThat(target.canSendNewSessionRequest(), is(true));
    }

    @Test
    public void afterDecreasingNumNewSessionRequestsFourTimesSendingRequestsIsNoLongerAllowed() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // when decreasing first time
        target.decreaseNumNewSessionRequests();

        // then sending is still allowed
        assertThat(target.canSendNewSessionRequest(), is(true));

        // when decreasing second time
        target.decreaseNumNewSessionRequests();

        // then sending is still allowed
        assertThat(target.canSendNewSessionRequest(), is(true));

        // when decreasing third time
        target.decreaseNumNewSessionRequests();

        // then sending is still allowed
        assertThat(target.canSendNewSessionRequest(), is(true));

        // when decreasing fourth time
        target.decreaseNumNewSessionRequests();

        // then sending is no longer allowed
        assertThat(target.canSendNewSessionRequest(), is(false));
    }

    @Test
    public void getBeaconConfigurationCallsWrappedSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        when(mockSessionImpl.getBeaconConfiguration()).thenReturn(null);

        // when, then
        assertThat(target.getBeaconConfiguration(), is(nullValue()));
        verify(mockSessionImpl, times(1)).getBeaconConfiguration();
        verifyNoMoreInteractions(mockSessionImpl);
    }

    @Test
    public void clearCapturedDataCallsWrappedSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // when
        target.clearCapturedData();

        // verify forwarded calls
        verify(mockSessionImpl, times(1)).clearCapturedData();
        verifyNoMoreInteractions(mockSessionImpl);
    }

    @Test
    public void sendBeaconCallsWrappedSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        HTTPClientProvider mockClientProvider = mock(HTTPClientProvider.class);
        when(target.sendBeacon(any(HTTPClientProvider.class))).thenReturn(null);

        // when
        assertThat(target.sendBeacon(mockClientProvider), is(nullValue()));

        // verify forwarded calls
        verify(mockSessionImpl, times(1)).sendBeacon(mockClientProvider);
        verifyNoMoreInteractions(mockSessionImpl);
    }

    @Test
    public void isEmptyCallsWrappedSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        when(mockSessionImpl.isEmpty()).thenReturn(true);

        // when, then
        assertThat(target.isEmpty(), is(true));

        // verify forwarded calls
        verify(mockSessionImpl, times(1)).isEmpty();
        verifyNoMoreInteractions(mockSessionImpl);
    }

    @Test
    public void endCallsWrappedSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // when
        target.end();

        // verify forwarded calls
        verify(mockSessionImpl, times(1)).end();
        verifyNoMoreInteractions(mockSessionImpl);
    }

    @Test
    public void getSessionReturnsWrappedSession() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // when
        SessionImpl obtained = target.getSession();

        // then
        assertThat(obtained, is(sameInstance(mockSessionImpl)));
        verifyZeroInteractions(mockSessionImpl);
    }

    @Test
    public void whenBeaconConfigurationIsNotSetSendingIsNotAllowed() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);

        // when, then
        assertThat(target.isDataSendingAllowed(), is(false));
    }

    @Test
    public void whenBeaconConfigurationIsSetSendingIsAllowedIfMultiplicityIsGreaterThanZero() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        BeaconConfiguration mockConfiguration = mock(BeaconConfiguration.class);
        when(mockSessionImpl.getBeaconConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getMultiplicity()).thenReturn(1);

        // when
        target.updateBeaconConfiguration(mockConfiguration);

        // then
        assertThat(target.isDataSendingAllowed(), is(true));
    }

    @Test
    public void whenBeaconConfigurationIsSetSendingIsDisallowedIfMultiplicityIsZero() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        BeaconConfiguration mockConfiguration = mock(BeaconConfiguration.class);
        when(mockSessionImpl.getBeaconConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getMultiplicity()).thenReturn(0);

        // when
        target.updateBeaconConfiguration(mockConfiguration);

        // then
        assertThat(target.isDataSendingAllowed(), is(false));
    }

    @Test
    public void whenBeaconConfigurationIsSetSendingIsDisallowedIfMultiplicityIsLessThanZero() {

        // given
        SessionWrapper target = new SessionWrapper(mockSessionImpl);
        BeaconConfiguration mockConfiguration = mock(BeaconConfiguration.class);
        when(mockSessionImpl.getBeaconConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getMultiplicity()).thenReturn(-1);

        // when
        target.updateBeaconConfiguration(mockConfiguration);

        // then
        assertThat(target.isDataSendingAllowed(), is(false));
    }
}
