/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core.configuration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.openkit.core.DeviceImpl;
import com.dynatrace.openkit.protocol.StatusResponse;

/**
 * The AbstractConfiguration class holds all configuration settings, both provided by the user and the Dynatrace/AppMon server.
 */
public abstract class AbstractConfiguration {

    private static final boolean DEFAULT_CAPTURE = false;                           // default: capture off
    private static final int DEFAULT_SEND_INTERVAL = 2 * 60 * 1000;                 // default: wait 2m (in ms) to send beacon
    private static final int DEFAULT_MAX_BEACON_SIZE = 30 * 1024;                   // default: max 30KB (in B) to send in one beacon
    private static final boolean DEFAULT_CAPTURE_ERRORS = true;                     // default: capture errors on
    private static final boolean DEFAULT_CAPTURE_CRASHES = true;                    // default: capture crashes on

    // immutable settings
    private final String applicationName;
    private final String applicationID;
    private final OpenKitType openKitType;
    private final long visitorID;
    private final String endpointURL;
    private final boolean verbose;

    // mutable settings
    private AtomicBoolean capture;                               // capture on/off; can be written/read by different threads -> atomic
    private int sendInterval;                                    // beacon send interval; is only written/read by beacon sender thread -> non-atomic
    private String monitorName;                                  // monitor name part of URL; is only written/read by beacon sender thread -> non-atomic
    private int maxBeaconSize;                                   // max beacon size; is only written/read by beacon sender thread -> non-atomic
    private AtomicBoolean captureErrors;                         // capture errors on/off; can be written/read by different threads -> atomic
    private AtomicBoolean captureCrashes;                        // capture crashes on/off; can be written/read by different threads -> atomic
    private HTTPClientConfiguration httpClientConfiguration;     // the current http client configuration

    // application and device settings
    private String applicationVersion;
    private final DeviceImpl device;

    private AtomicInteger nextSessionNumber = new AtomicInteger(0);

    // *** constructors ***

    protected AbstractConfiguration(OpenKitType openKitType, String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
        this.verbose = verbose;

        this.openKitType = openKitType;

        // immutable settings
        this.applicationName = applicationName;
        this.applicationID = applicationID;
        this.visitorID = visitorID;
        this.endpointURL = endpointURL;

        // mutable settings
        capture = new AtomicBoolean(DEFAULT_CAPTURE);
        sendInterval = DEFAULT_SEND_INTERVAL;
        monitorName = openKitType.getDefaultMonitorName();
        maxBeaconSize = DEFAULT_MAX_BEACON_SIZE;
        captureErrors = new AtomicBoolean(DEFAULT_CAPTURE_ERRORS);
        captureCrashes = new AtomicBoolean(DEFAULT_CAPTURE_CRASHES);

        device = new DeviceImpl();
        applicationVersion = null;
        httpClientConfiguration = null;
    }

    // *** public methods ***

    // return next session number
    public int createSessionNumber() {
        return nextSessionNumber.incrementAndGet();
    }

    // updates settings based on a status response
    public void updateSettings(StatusResponse statusResponse) {
        // if invalid status response OR response code != 200 -> capture off
        if ((statusResponse == null) || (statusResponse.getResponseCode() != 200)) {
            disableCapture();
        } else {
            capture.set(statusResponse.isCapture());
        }

        // if capture is off -> leave other settings on their current values
        if (!isCapture()) {
            return;
        }

        // use monitor name from beacon response or default
        String newMonitorName = statusResponse.getMonitorName();
        if (newMonitorName == null) {
            newMonitorName = openKitType.getDefaultMonitorName();
        }

        // use server id from beacon response or default
        int newServerID = statusResponse.getServerID();
        if (newServerID == -1) {
            newServerID = openKitType.getDefaultServerID();
        }

        // check if http config changed
        if (!monitorName.equals(newMonitorName)
            || httpClientConfiguration.getServerID() != newServerID) {
            httpClientConfiguration = new HTTPClientConfiguration(
                createBaseURL(endpointURL, newMonitorName),
                newServerID,
                applicationID,
                verbose);
            monitorName = newMonitorName;
        }

        // use send interval from beacon response or default
        int newSendInterval = statusResponse.getSendInterval();
        if (newSendInterval == -1) {
            newSendInterval = DEFAULT_SEND_INTERVAL;
        }
        // check if send interval has to be updated
        if (sendInterval != newSendInterval) {
            sendInterval = newSendInterval;
        }

        // use max beacon size from beacon response or default
        int newMaxBeaconSize = statusResponse.getMaxBeaconSize();
        if (newMaxBeaconSize == -1) {
            newMaxBeaconSize = DEFAULT_MAX_BEACON_SIZE;
        }
        if (maxBeaconSize != newMaxBeaconSize) {
            maxBeaconSize = newMaxBeaconSize;
        }

        // use capture settings for errors and crashes
        captureErrors.set(statusResponse.isCaptureErrors());
        captureCrashes.set(statusResponse.isCaptureCrashes());
    }

    // create base URL for HTTP client from endpoint & monitorname
    protected abstract String createBaseURL(String endpointURL, String monitorName);

    // *** getter methods ***

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public long getVisitorID() {
        return visitorID;
    }

    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Enable capturing.
     */
    public void enableCapture() {
        capture.set(true);
    }

    /**
     * Disable capturing.
     */
    public void disableCapture() {
        capture.set(false);
    }

    /**
     * Get a boolean indicating whether capturing is enabled or not.
     *
     * @return {@code true} if capturing is enabled, {@code false} otherwise.
     */
    public boolean isCapture() {
        return capture.get();
    }

    public int getSendInterval() {
        return sendInterval;
    }

    public int getMaxBeaconSize() {
        return maxBeaconSize;
    }

    public boolean isCaptureErrors() {
        return captureErrors.get();
    }

    public boolean isCaptureCrashes() {
        return captureCrashes.get();
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public DeviceImpl getDevice() {
        return device;
    }

    /**
     * Returns the current http client configuration
     *
     * @return
     */
    public HTTPClientConfiguration getHttpClientConfig() {
        return httpClientConfiguration;
    }

    protected void setHttpClientConfiguration(HTTPClientConfiguration configuration) {
        httpClientConfiguration = configuration;
    }
}
