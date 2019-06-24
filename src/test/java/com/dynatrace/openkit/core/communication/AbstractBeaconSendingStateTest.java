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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AbstractBeaconSendingStateTest {

    private AbstractBeaconSendingState mockState;
    private BeaconSendingContext mockContext;

    @Before
    public void setUp() {
        mockState = mock(AbstractBeaconSendingState.class);
        doCallRealMethod().when(mockState).execute(org.mockito.Matchers.any(BeaconSendingContext.class));
        mockContext = mock(BeaconSendingContext.class);
    }

    @Test
    public void aTestBeaconSendingStateExecutes() throws InterruptedException {

        // when calling execute
        mockState.execute(mockContext);

        // then verify doExecute was called
        verify(mockState, times(1)).doExecute(mockContext);

        // also verify that shutdown requested is queried, but nothing else
        verify(mockContext, times(1)).isShutdownRequested();
        verifyNoMoreInteractions(mockContext);
    }


    @Test
    public void aTestBeaconSendingStateExecutesButIsInterrupted() throws InterruptedException {

        // when calling execute leads to an InterruptedException
        doThrow(new InterruptedException()).when(mockState).doExecute(mockContext);
        mockState.execute(mockContext);

        /*
            check and reset interrupted flag
            if the flag is not reset, subsequent tests might fail
        */
        assertThat(Thread.interrupted(), is(true));

        // then verify doExecute was called
        verify(mockState, times(1)).doExecute(mockContext);

        // also verify that shutdown requested is queried, but nothing else
        verify(mockState, times(1)).onInterrupted(mockContext);
        verify(mockContext, times(1)).isShutdownRequested();
        verify(mockContext, times(1)).requestShutdown();
        verifyNoMoreInteractions(mockContext);
    }
}
