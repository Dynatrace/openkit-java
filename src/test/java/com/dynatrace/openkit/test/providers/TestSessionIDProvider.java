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

package com.dynatrace.openkit.test.providers;

import com.dynatrace.openkit.providers.SessionIDProvider;

public class TestSessionIDProvider implements SessionIDProvider {

    private int initialIntegerOffset = 0;

    @Override
    public int getNextSessionID() {
        if (initialIntegerOffset == Integer.MAX_VALUE) {
            initialIntegerOffset = 0;
        }
        initialIntegerOffset = initialIntegerOffset + 1;
        return initialIntegerOffset;
    }
}
