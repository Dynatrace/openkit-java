/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Tests the root action having some knowledge of the internals of the underlying actions.
 */
public class RootActionImplTest {

    @Test
    public void testEnterNull() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create root action with a null child action (must be valid)
        final String rootActionStr = "rootAction";
        final RootAction rootAction = new RootActionImpl(beacon, rootActionStr, actions);
        final Action childAction = rootAction.enterAction(null);

        // child leaves immediately
        final Action retAction = childAction.leaveAction();
        assertThat(retAction, is(equalTo((Action) rootAction)));
    }

    @Test
    public void testEnterLeaveAction() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create root action with child action
        final String rootActionStr = "rootAction";
        final String childActionStr = "childAction";
        final RootActionImpl rootAction = new RootActionImpl(beacon, rootActionStr, actions);
        final Action childAction = rootAction.enterAction(childActionStr);

        // verify
        assertThat(rootAction, is(instanceOf(ActionImpl.class)));
        assertThat(childAction, is(instanceOf(ActionImpl.class)));
        assertThat(((ActionImpl) rootAction).getName(), is(equalTo(rootActionStr)));
        assertThat(((ActionImpl) childAction).getName(), is(equalTo(childActionStr)));

        // child leaves
        Action retAction = childAction.leaveAction();
        assertThat(retAction, is(equalTo((Action) rootAction)));

        // parent leaves
        retAction = rootAction.leaveAction();
        assertThat(retAction, is(nullValue()));

        // verify that beacon cache is now empty
        assertThat(actions.toArrayList().isEmpty(), is(true));
    }

    @Test
    public void testEnterLeaveAction2() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SynchronizedQueue<Action> actions = new SynchronizedQueue<Action>();

        // create root action with 2 children
        final String rootActionStr = "rootAction";
        final String childOneActionStr = "childOneAction";
        final String childTwoActionStr = "childTwoAction";
        final RootActionImpl rootAction = new RootActionImpl(beacon, rootActionStr, actions);
        final Action childAction1 = rootAction.enterAction(childOneActionStr);
        final Action childAction2 = rootAction.enterAction(childTwoActionStr);

        // verify (using internally known methods)
        assertThat(rootAction, is(instanceOf(ActionImpl.class)));
        assertThat(childAction1, is(instanceOf(ActionImpl.class)));
        assertThat(childAction2, is(instanceOf(ActionImpl.class)));
        assertThat(((ActionImpl) rootAction).getName(), is(equalTo(rootActionStr)));
        assertThat(((ActionImpl) childAction1).getName(), is(equalTo(childOneActionStr)));
        assertThat(((ActionImpl) childAction2).getName(), is(equalTo(childTwoActionStr)));

        // parent leaves, thus the children are also left
        final Action retAction = rootAction.leaveAction();
        assertThat(retAction, is(nullValue()));
        assertThat(actions.toArrayList().isEmpty(), is(true));
    }
}
