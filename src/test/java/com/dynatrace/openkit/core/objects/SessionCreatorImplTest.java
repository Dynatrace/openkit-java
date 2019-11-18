/**
 * Copyright 2018-2019 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class SessionCreatorImplTest {

    private SessionCreatorInput mockInput;
    private Logger mockLogger;
    private OpenKitConfiguration mockOpenKitConfiguration;
    private PrivacyConfiguration mockPrivacyConfiguration;
    private BeaconCache mockBeaconCache;
    private SessionIDProvider mockSessionIdProvider;
    private ThreadIDProvider mockThreadIdProvider;
    private TimingProvider mockTimingProvider;
    private OpenKitComposite mockParent;
    private final int SERVER_ID = 999;
    private final int SESSION_ID = 777;
    private final long DEVICE_ID = 1;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        mockOpenKitConfiguration = mock(OpenKitConfiguration.class);
        when(mockOpenKitConfiguration.getApplicationID()).thenReturn("");
        when(mockOpenKitConfiguration.getApplicationName()).thenReturn("");
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn("");
        when(mockOpenKitConfiguration.getDeviceID()).thenReturn(DEVICE_ID);

        mockPrivacyConfiguration = mock(PrivacyConfiguration.class);
        mockBeaconCache = mock(BeaconCache.class);
        mockSessionIdProvider = mock(SessionIDProvider.class);
        mockThreadIdProvider = mock(ThreadIDProvider.class);
        mockTimingProvider = mock(TimingProvider.class);
        mockParent = mock(OpenKitComposite.class);

        mockInput = mock(SessionCreatorInput.class);
        when(mockInput.getLogger()).thenReturn(mockLogger);
        when(mockInput.getOpenKitConfiguration()).thenReturn(mockOpenKitConfiguration);
        when(mockInput.getPrivacyConfiguration()).thenReturn(mockPrivacyConfiguration);
        when(mockInput.getBeaconCache()).thenReturn(mockBeaconCache);
        when(mockInput.getSessionIdProvider()).thenReturn(mockSessionIdProvider);
        when(mockInput.getThreadIdProvider()).thenReturn(mockThreadIdProvider);
        when(mockInput.getTimingProvider()).thenReturn(mockTimingProvider);
        when(mockInput.getCurrentServerId()).thenReturn(SERVER_ID);
    }

    @Test
    public void constructorTakesOverLogger() {
        // when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getLogger();
        verifyZeroInteractions(mockLogger);
    }

    @Test
    public void constructorTakesOverOpenKitConfiguration() {
        // given, when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getOpenKitConfiguration();
        verifyZeroInteractions(mockOpenKitConfiguration);
    }

    @Test
    public void constructorTakesOverPrivacyConfiguration() {
        // when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getPrivacyConfiguration();
        verifyZeroInteractions(mockPrivacyConfiguration);
    }

    @Test
    public void constructorTakesOverBeaconCache() {
        // when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getBeaconCache();
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void constructorTakesOverThreadIdProvider() {
        // when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getThreadIdProvider();
        verifyZeroInteractions(mockThreadIdProvider);
    }

    @Test
    public void constructorTakesOverTimingProvider() {
        // given, when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getTimingProvider();
        verifyZeroInteractions(mockTimingProvider);
    }

    @Test
    public void constructorTakesOverServerId() {
        //  when
        createSessionCreator();

        // then
        verify(mockInput, times(1)).getCurrentServerId();
    }

    @Test
    public void constructorDrawsNextSessionId() {
        // when
        createSessionCreator();

        // then
        verify(mockSessionIdProvider, times(1)).getNextSessionID();
        verifyNoMoreInteractions(mockSessionIdProvider);
    }

    @Test
    public void createSessionReturnsNewSessionInstance() {
        // given
        SessionCreatorImpl target = createSessionCreator();

        // when
        SessionImpl obtained = target.createSession(mockParent);

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void createSessionGivesSessionsWithAlwaysSameSessionNumber() {
        // given
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(true);
        when(mockSessionIdProvider.getNextSessionID()).thenReturn(SESSION_ID, 1, 2, 3);
        SessionCreatorImpl target = createSessionCreator();

        // when
        SessionImpl obtainedOne = target.createSession(mockParent);
        SessionImpl obtainedTwo = target.createSession(mockParent);
        SessionImpl obtainedThree = target.createSession(mockParent);

        // then
        assertThat(obtainedOne.getBeacon().getSessionNumber(), is(SESSION_ID));
        assertThat(obtainedTwo.getBeacon().getSessionNumber(), is(SESSION_ID));
        assertThat(obtainedThree.getBeacon().getSessionNumber(), is(SESSION_ID));
    }

    @Test
    public void createSessionGivesSessionsWithSameRandomizedDeviceId() {
        // given
        when(mockPrivacyConfiguration.isDeviceIDSendingAllowed()).thenReturn(false);

        SessionCreatorImpl target = createSessionCreator();
        long randomizedDeviceId = target.getRandomNumberGenerator().nextPositiveLong();

        // when
        SessionImpl obtainedOne = target.createSession(mockParent);
        SessionImpl obtainedTwo = target.createSession(mockParent);
        SessionImpl obtainedThree = target.createSession(mockParent);

        // then
        assertThat(obtainedOne.getBeacon().getDeviceID(), is(randomizedDeviceId));
        assertThat(obtainedTwo.getBeacon().getDeviceID(), is(randomizedDeviceId));
        assertThat(obtainedThree.getBeacon().getDeviceID(), is(randomizedDeviceId));
    }

    @Test
    public void createSessionIncreasesSessionSequenceNumber() {
        // given
        SessionCreatorImpl target = createSessionCreator();

        assertThat(target.getSessionSequenceNumber(), is(0));

        // when
        target.createSession(mockParent);

        // then
        assertThat(target.getSessionSequenceNumber(), is(1));

        // and when
        target.createSession(mockParent);

        // then
        assertThat(target.getSessionSequenceNumber(), is(2));
    }

    private SessionCreatorImpl createSessionCreator() {
        return new SessionCreatorImpl(mockInput, "https://localhost");
    }
}
