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

import com.dynatrace.openkit.protocol.StatusResponse;

/**
 * Utility class for sending requests to the server and retry several timesl
 */
class BeaconSendingRequestUtil {

    private BeaconSendingRequestUtil() {
    }

    /**
     * Send a status request to the server and try to get the status response.
     *
     * @param context                   Used to retrieve the {@link com.dynatrace.openkit.protocol.HTTPClient} and for delaying methods.
     * @param numRetries                The number of retries (total number of tries = numRetries + 1)
     * @param initialRetryDelayInMillis The initial delay which is doubles between one unsuccessful attempt and the next retry.
     * @return A status response or {@code null} if shutdown was requested or number of retries was reached.
     */
    static StatusResponse sendStatusRequest(BeaconSendingContext context, int numRetries, long initialRetryDelayInMillis) throws InterruptedException {

        StatusResponse statusResponse;
        long sleepTimeInMillis = initialRetryDelayInMillis;
        int retry = 0;

        while (true) {
            statusResponse = context.getHTTPClient().sendStatusRequest();
            if (statusResponse != null || retry >= numRetries || context.isShutdownRequested()) {
                break;
            }

            // if no (valid) status response was received -> sleep and double the delay for each retry
            context.sleep(sleepTimeInMillis);
            sleepTimeInMillis *= 2;
            retry++;
        }

        return statusResponse;
    }
}
