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
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SessionWatchdogContextTest {

    private TimingProvider mockTimingProvider;
    private SessionImpl mockSession;

    @Before
    public void setUp() {
        mockTimingProvider = mock(TimingProvider.class);
        mockSession = mock(SessionImpl.class);
    }

    @Test
    public void onDefaultShutdownIsFalse() {
        // given
        SessionWatchdogContext target = createContext();

        // then
        assertThat(target.isShutdownRequested(), is(false));
    }

    @Test
    public void onDefaultSessionsToCloseIsEmpty() {
        // given
        SessionWatchdogContext target = createContext();

        // then
        assertThat(target.getSessionsToClose(), is(empty()));
    }

    @Test
    public void closeOrEnqueueForClosingDoesNotAddSessionIfItCanBeClosed() {
        // given
        when(mockSession.tryEnd()).thenReturn(true);
        SessionWatchdogContext target = createContext();

        // when
        target.closeOrEnqueueForClosing(mockSession, 0);

        // then
        assertThat(target.getSessionsToClose(), is(empty()));
        verify(mockSession, times(1)).tryEnd();
        verifyNoMoreInteractions(mockSession);
    }

    @Test
    public void closeOrEnqueueForClosingAddsSessionIfSessionCannotBeClosed() {
        // given
        when(mockSession.tryEnd()).thenReturn(false);
        SessionWatchdogContext target = createContext();

        // when
        target.closeOrEnqueueForClosing(mockSession, 17);

        // then
        assertThat(target.getSessionsToClose().size(), is(1));
    }

    @Test
    public void closeOrEnqueueForClosingSetsSplitByEventsGracePeriodEndTimeIfSessionCannotBeClosed() {
        // given
        long timestamp = 10;
        int gracePeriod = 5;
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(timestamp);
        when(mockSession.tryEnd()).thenReturn(false);
        SessionWatchdogContext target = createContext();

        // when
        target.closeOrEnqueueForClosing(mockSession, gracePeriod);

        // then
        verify(mockSession, times(1)).setSplitByEventsGracePeriodEndTimeInMillis(timestamp + gracePeriod);
    }

    @Test
    public void dequeueFromClosingRemovesSession() {
        // given
        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0);
        assertThat(target.getSessionsToClose().size(), is(1));

        // when
        target.dequeueFromClosing(mockSession);

        // then
        assertThat(target.getSessionsToClose().size(), is(0));
    }

    @Test
    public void executeEndsSessionsWithExpiredGracePeriod() {
        // given
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(5L);
        when(mockSession.tryEnd()).thenReturn(false);
        when(mockSession.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(4L);

        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0 /* irrelevant */);

        // when
        target.execute();

        // then
        verify(mockSession, times(1)).end();
        assertThat(target.getSessionsToClose().size(), is(0));
    }

    @Test
    public void executeEndsSessionsWithGraceEndTimeSameAsCurrentTime() {
        // given
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(5L);
        when(mockSession.tryEnd()).thenReturn(false);
        when(mockSession.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(5L);

        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0 /* irrelevant */);

        // when
        target.execute();

        // then
        verify(mockSession, times(1)).end();
        assertThat(target.getSessionsToClose().size(), is(0));
    }

    @Test
    public void executeDoesNotEndSessionsWhenGracePeriodIsNotExpired() {
        // given
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(5L);
        when(mockSession.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(6L);
        when(mockSession.tryEnd()).thenReturn(false);

        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0 /* irrelevant */);

        // when
        target.execute();

        // then
        verify(mockSession, times(0)).end();
        assertThat(target.getSessionsToClose().size(), is(1));
    }

    @Test
    public void executeSleepsDefaultTimeIfNoSessionToCloseExists() throws InterruptedException {
        // given
        SessionWatchdogContext target = createContext();

        // when
        target.execute();

        // then
        verify(mockTimingProvider, times(1)).sleep(SessionWatchdogContext.DEFAULT_SLEEP_TIME_IN_MILLIS);
    }

    @Test
    public void executeSleepsDefaultTimeIfSessionIsExpiredAndNoFurtherNonExpiredSessions() throws InterruptedException {
        // given
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(5L);
        when(mockSession.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(3L);
        when(mockSession.tryEnd()).thenReturn(false);

        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0 /* irrelevant */);

        // when
        target.execute();

        // then
        verify(mockTimingProvider, times(1)).sleep(SessionWatchdogContext.DEFAULT_SLEEP_TIME_IN_MILLIS);
        verify(mockSession, times(1)).end();
        ;
    }

    @Test
    public void executeSleepsMinimumTimeToNextSessionGraceEndPeriod() throws InterruptedException {
        // given
        when(mockSession.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(4L);
        when(mockSession.tryEnd()).thenReturn(false);

        SessionImpl mockSession1 = mock(SessionImpl.class);
        when(mockSession1.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(3L);
        when(mockSession1.tryEnd()).thenReturn(false);

        SessionImpl mockSession2 = mock(SessionImpl.class);
        when(mockSession2.getSplitByEventsGracePeriodEndTimeInMillis()).thenReturn(5L);
        when(mockSession2.tryEnd()).thenReturn(false);

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(0L);

        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0 /* irrelevant */);
        target.closeOrEnqueueForClosing(mockSession1, 0 /* irrelevant */);
        target.closeOrEnqueueForClosing(mockSession2, 0 /* irrelevant */);

        // when
        target.execute();

        // then
        verify(mockTimingProvider, times(1)).sleep(3L);
        verify(mockSession, times(0)).end();
        verify(mockSession1, times(0)).end();
        verify(mockSession2, times(0)).end();
    }

    @Test
    public void executeDoesNotSleepLongerThanDefaultSleepTime() throws InterruptedException {
        // given
        when(mockSession.tryEnd()).thenReturn(false);
        when(mockSession.getSplitByEventsGracePeriodEndTimeInMillis())
                .thenReturn(SessionWatchdogContext.DEFAULT_SLEEP_TIME_IN_MILLIS + 10);

        SessionWatchdogContext target = createContext();
        target.closeOrEnqueueForClosing(mockSession, 0 /* irrelevant */);

        // when
        target.execute();

        // then
        verify(mockTimingProvider, times(1)).sleep(SessionWatchdogContext.DEFAULT_SLEEP_TIME_IN_MILLIS);
    }

    @Test
    public void executeRequestsShutdownIfInterruptedDuringSleep() throws InterruptedException {
        // given
        doThrow(new InterruptedException()).when(mockTimingProvider).sleep(anyInt());

        SessionWatchdogContext target = createContext();
        assertThat(target.isShutdownRequested(), is(false));

        // when
        target.execute();

        // then
        assertThat(target.isShutdownRequested(), is(true));
    }

    @Test
    public void requestShutdownSetsIsShutdownRequestedToTrue() {
        // given
        SessionWatchdogContext target = createContext();
        assertThat(target.isShutdownRequested(), is(false));

        // when
        target.requestShutdown();

        // then
        assertThat(target.isShutdownRequested(), is(true));
    }

    private SessionWatchdogContext createContext() {
        return new SessionWatchdogContext(mockTimingProvider);
    }
}
