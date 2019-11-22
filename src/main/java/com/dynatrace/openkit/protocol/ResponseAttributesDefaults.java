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
package com.dynatrace.openkit.protocol;

import java.util.concurrent.TimeUnit;

public enum ResponseAttributesDefaults implements ResponseAttributes {

    JSON_RESPONSE {
        private final int DEFAULT_BEACON_SIZE_IN_BYTES = 150 * 1024; // 150 kB
        private final int DEFAULT_SESSION_DURATION_IN_MILLIS = (int) TimeUnit.MINUTES.toMillis(360); // 360 minutes
        private final int DEFAULT_EVENTS_PER_SESSION = 200;
        private final int DEFAULT_SESSION_TIMEOUT_IN_MILLIS = (int) TimeUnit.SECONDS.toMillis(600); // 600 seconds

        @Override
        public int getMaxBeaconSizeInBytes() {
            return DEFAULT_BEACON_SIZE_IN_BYTES;
        }

        @Override
        public int getMaxSessionDurationInMilliseconds() {
            return DEFAULT_SESSION_DURATION_IN_MILLIS;
        }

        @Override
        public int getMaxEventsPerSession() {
            return DEFAULT_EVENTS_PER_SESSION;
        }

        @Override
        public int getSessionTimeoutInMilliseconds() {
            return DEFAULT_SESSION_TIMEOUT_IN_MILLIS;
        }
    },

    KEY_VALUE_RESPONSE {
        private final int DEFAULT_BEACON_SIZE_IN_BYTES = 30 * 1024; // 30 kB
        private final int DEFAULT_SESSION_DURATION_IN_MILLIS = -1;
        private final int DEFAULT_EVENTS_PER_SESSION = -1;
        private final int DEFAULT_SESSION_TIMEOUT_IN_MILLIS = -1;
        private final int DEFAULT_SEND_INTERVAL_IN_MILLIS = (int)TimeUnit.SECONDS.toMillis(120);

        @Override
        public int getMaxBeaconSizeInBytes() {
            return DEFAULT_BEACON_SIZE_IN_BYTES;
        }

        @Override
        public int getMaxSessionDurationInMilliseconds() {
            return DEFAULT_SESSION_DURATION_IN_MILLIS;
        }

        @Override
        public int getMaxEventsPerSession() {
            return DEFAULT_EVENTS_PER_SESSION;
        }

        @Override
        public int getSessionTimeoutInMilliseconds() {
            return DEFAULT_SESSION_TIMEOUT_IN_MILLIS;
        }

        @Override
        public int getSendIntervalInMilliseconds() {
            return DEFAULT_SEND_INTERVAL_IN_MILLIS;
        }
    },

    UNDEFINED {
        private final int DEFAULT_BEACON_SIZE_IN_BYTES = 30 * 1024; // 30 kB
        private final int DEFAULT_SESSION_DURATION_IN_MILLIS = -1;
        private final int DEFAULT_EVENTS_PER_SESSION = -1;
        private final int DEFAULT_SESSION_TIMEOUT_IN_MILLIS = -1;
        private final int DEFAULT_SERVER_ID = -1;

        @Override
        public int getMaxBeaconSizeInBytes() {
            return DEFAULT_BEACON_SIZE_IN_BYTES;
        }

        @Override
        public int getMaxSessionDurationInMilliseconds() {
            return DEFAULT_SESSION_DURATION_IN_MILLIS;
        }

        @Override
        public int getMaxEventsPerSession() {
            return DEFAULT_EVENTS_PER_SESSION;
        }

        @Override
        public int getSessionTimeoutInMilliseconds() {
            return DEFAULT_SESSION_TIMEOUT_IN_MILLIS;
        }

        @Override
        public int getServerId() {
            return DEFAULT_SERVER_ID;
        }
    };

    private static final int DEFAULT_VISIT_STORE_VERSION = 1;
    private static final boolean DEFAULT_CAPTURE = true;
    private static final boolean DEFAULT_CAPTURE_CRASHES = true;
    private static final boolean DEFAULT_CAPTURE_ERRORS = true;
    private static final int DEFAULT_MULTIPLICITY = 1;
    private static final int DEFAULT_SERVER_ID = 1;
    private static final int DEFAULT_TIMESTAMP = 0;
    private final int DEFAULT_SEND_INTERVAL_IN_MILLIS = (int) TimeUnit.SECONDS.toMillis(120); // 120 seconds

    public abstract int getMaxBeaconSizeInBytes();

    public abstract int getMaxSessionDurationInMilliseconds();

    public abstract int getMaxEventsPerSession();

    public abstract int getSessionTimeoutInMilliseconds();

    public int getSendIntervalInMilliseconds() {
        return DEFAULT_SEND_INTERVAL_IN_MILLIS;
    }

    public int getVisitStoreVersion() {
        return DEFAULT_VISIT_STORE_VERSION;
    }

    public boolean isCapture() {
        return DEFAULT_CAPTURE;
    }

    public boolean isCaptureCrashes() {
        return DEFAULT_CAPTURE_CRASHES;
    }

    public boolean isCaptureErrors() {
        return DEFAULT_CAPTURE_ERRORS;
    }

    public int getMultiplicity() {
        return DEFAULT_MULTIPLICITY;
    }

    public int getServerId() {
        return DEFAULT_SERVER_ID;
    }

    public long getTimestampInMilliseconds() {
        return DEFAULT_TIMESTAMP;
    }

    @Override
    public boolean isAttributeSet(ResponseAttribute attribute) {
        return false;
    }

    @Override
    public ResponseAttributes merge(ResponseAttributes responseAttributes) {
        return responseAttributes;
    }
}
