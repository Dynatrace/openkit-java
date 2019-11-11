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

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the root action having some knowledge of the internals of the underlying actions.
 */

@SuppressWarnings("resource")
public class RootActionImplTest {

    private static final String ROOT_ACTION_NAME = "parent action";
    private static final String CHILD_ACTION_NAME = "child action";

    private Logger logger;
    private Beacon beacon;
    private SessionImpl session;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);

        beacon = mock(Beacon.class);

        session = mock(SessionImpl.class);
    }

    @Test
    public void getParentActionReturnsNull() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        Action obtained = target.getParentAction();

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void enterActionWithNullNameGivesNullAction() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        Action obtained = target.enterAction(null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(NullAction.class));

        verify(logger, times(1)).warning(endsWith("enterAction: actionName must not be null or empty"));
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void enterActionWithEmptyNameGivesNullAction() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        Action obtained = target.enterAction("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(NullAction.class));

        verify(logger, times(1)).warning(endsWith("enterAction: actionName must not be null or empty"));
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void enterActionGivesLeafActionInstance() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        Action obtained = target.enterAction(CHILD_ACTION_NAME);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(LeafActionImpl.class));
    }

    @Test
    public void enterActionAddsLeafActionToListOfChildObjects() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        Action obtained = target.enterAction(CHILD_ACTION_NAME);

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject)obtained))));
    }

    @Test
    public void enterActionGivesNullActionIfAlreadyLeft() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);
        target.leaveAction();

        // when
        Action obtained = target.enterAction("child action");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullAction.class)));
    }

    @Test
    public void enterActionLogsInvocation() {
        // given
        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        target.enterAction(CHILD_ACTION_NAME);

        // then
        verify(logger, times(1)).isDebugEnabled();
        verify(logger, times(1)).debug(endsWith("enterAction(" + CHILD_ACTION_NAME + ")"));
    }

    @Test
    public void toStringReturnsAppropriateResult() {
        // given
        when(beacon.getSessionNumber()).thenReturn(21);
        when(beacon.createID()).thenReturn(1, 2, 3, 100);

        RootActionImpl target = new RootActionImpl(logger, session, ROOT_ACTION_NAME, beacon);

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is(equalTo("RootActionImpl [sn=21, id=1, name=" + ROOT_ACTION_NAME + "] ")));
    }
}
