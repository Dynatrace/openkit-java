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
     * @param context Used to retrieve the {@link com.dynatrace.openkit.protocol.HTTPClient} and for delaying methods.
     * @param numRetries The number of retries (total number of tries = numRetries + 1)
     * @param initialRetryDelayInMillis The initial delay which is doubles between one unsuccessful attempt and the next retry.
     *
     * @return A status response or {@code null} if shutdown was requested or number of retries was reached.
     */
    static StatusResponse sendStatusRequest(BeaconSendingContext context, int numRetries, long initialRetryDelayInMillis) throws InterruptedException {

        StatusResponse statusResponse = null;
        long sleepTimeInMillis = initialRetryDelayInMillis;
        int retry = 0;

        while (true) {
            statusResponse = context.getHTTPClient().sendStatusRequest();
            if (statusResponse != null || retry >= numRetries || context.isShutdownRequested()) {
                break;
            }

            // if no (valid) status response was received -> sleep 1s [2s, 4s, 8s, 16s] and then retry (max 6 times altogether)
            context.sleep(sleepTimeInMillis);
            sleepTimeInMillis *= 2;
            retry++;
        }

        return statusResponse;
    }
}
