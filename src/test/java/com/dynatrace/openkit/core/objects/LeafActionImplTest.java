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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeafActionImplTest {

    private static final String ACTION_NAME = "TestAction";

    private Logger logger;
    private RootActionImpl rootAction;
    private Beacon beacon;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);

        rootAction = mock(RootActionImpl.class);

        beacon = mock(Beacon.class);
    }

    @Test
    public void getParentActionReturnsValuePassedInConstructor() {
        // given
        LeafActionImpl target = new LeafActionImpl(logger, rootAction, ACTION_NAME, beacon);

        // when
        Action obtained = target.getParentAction();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(sameInstance((Action)rootAction)));
    }

    @Test
    public void toStringReturnsAppropriateResult() {
        // given
        when(beacon.getSessionNumber()).thenReturn(21);
        when(beacon.createID()).thenReturn(1, 2, 3, 100);
        when(rootAction.getActionID()).thenReturn(42);

        LeafActionImpl target = new LeafActionImpl(logger, rootAction, ACTION_NAME, beacon);

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is(equalTo("LeafActionImpl [sn=21, id=1, name=" + ACTION_NAME + ", pa=42] ")));
    }
}
