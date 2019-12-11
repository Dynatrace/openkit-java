/**
 *   Copyright 2018-2019 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.core;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.core.objects.SessionProxyImpl;
import org.junit.Before;
import org.junit.Test;

public class SessionWatchdogTest {

    private Logger mockLogger;
    private SessionWatchdogContext mockContext;
    private SessionImpl mockSession;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        when(mockLogger.isDebugEnabled()).thenReturn(true);

        mockContext = mock(SessionWatchdogContext.class);
        mockSession = mock(SessionImpl.class);
    }

    @Test
    public void initializeLogsWatchdogThreadStart() throws Exception {
        // given
        when(mockContext.isShutdownRequested()).thenReturn(true);
        SessionWatchdog target = createWatchdog();

        // when
        target.initialize();

        // then
        verify(mockLogger, timeout(1000)).debug(endsWith(" initialize() - session watchdog thread started"));
        verify(mockContext, timeout(1000)).isShutdownRequested();

        // cleanup
        target.shutdown();
    }

    @Test
    public void contextIsExecutedUntilShutdownIsRequested() {
        // given
        when(mockContext.isShutdownRequested()).thenReturn(false, false, true);
        SessionWatchdog target = createWatchdog();

        // when
        target.initialize();

        // then
        verify(mockContext, timeout(1000).times(3)).isShutdownRequested();
        verify(mockContext, timeout(1000).times(2)).execute();
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void shutdownLogsWatchdogThreadStop() {
         // given
        when(mockContext.isShutdownRequested()).thenReturn(true);
        SessionWatchdog target = createWatchdog();
        target.initialize();

        verify(mockContext, timeout(1000)).isShutdownRequested(); // wait for thread to end

        // when
        target.shutdown();

        // then
        verify(mockLogger, timeout(1000)).debug(endsWith(" shutdown() - session watchdog thread stopped"));
    }
    @Test
    public void shutdownLogsInvocation() {
        // given
        SessionWatchdog target = createWatchdog();

        // when
        target.shutdown();

        // then
        verify(mockLogger, times(1)).debug(endsWith(" shutdown() - session watchdog thread request shutdown"));
    }

    @Test
    public void closeOrEnqueueForClosingDelegatesToSessionWatchdogContext() {
        // given
        int gracePeriod = 1;
        SessionWatchdog target = createWatchdog();

        // when
        target.closeOrEnqueueForClosing(mockSession, gracePeriod);

        // then
        verify(mockContext, times(1)).closeOrEnqueueForClosing(mockSession, gracePeriod);
    }

    @Test
    public void dequeueFromClosingDelegatesToSessionWatchdogContext() {
        // given
        SessionWatchdog target = createWatchdog();

        // when
        target.dequeueFromClosing(mockSession);

        // then
        verify(mockContext, times(1)).dequeueFromClosing(mockSession);
    }

    @Test
    public void addToSplitByTimeoutDelegatesToSessionWatchdogContext() {
        // given
        SessionProxyImpl mockSessionProxy = mock(SessionProxyImpl.class);
        SessionWatchdog target = createWatchdog();

        // when
        target.addToSplitByTimeout(mockSessionProxy);

        // then
        verify(mockContext, times(1)).addToSplitByTimeout(mockSessionProxy);
    }

    @Test
    public void removeFromSplitByTimeoutDelegatesToSessionWatchdogContext() {
        // given
        SessionProxyImpl mockSessionProxy = mock(SessionProxyImpl.class);
        SessionWatchdog target = createWatchdog();

        // when
        target.removeFromSplitByTimeout(mockSessionProxy);

        // then
        verify(mockContext, times(1)).removeFromSplitByTimeout(mockSessionProxy);
    }

    private SessionWatchdog createWatchdog() {
        return new SessionWatchdog(mockLogger, mockContext);
    }
}
