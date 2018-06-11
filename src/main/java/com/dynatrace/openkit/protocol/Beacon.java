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

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.ActionImpl;
import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.WebRequestTracerBaseImpl;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.util.InetAddressValidator;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Beacon class holds all the beacon data and the beacon protocol implementation.
 */
public class Beacon {

    // basic data constants
    private static final String BEACON_KEY_PROTOCOL_VERSION = "vv";
    private static final String BEACON_KEY_OPENKIT_VERSION = "va";
    private static final String BEACON_KEY_APPLICATION_ID = "ap";
    private static final String BEACON_KEY_APPLICATION_NAME = "an";
    private static final String BEACON_KEY_APPLICATION_VERSION = "vn";
    private static final String BEACON_KEY_PLATFORM_TYPE = "pt";
    private static final String BEACON_KEY_AGENT_TECHNOLOGY_TYPE = "tt";
    private static final String BEACON_KEY_VISITOR_ID = "vi";
    private static final String BEACON_KEY_SESSION_NUMBER = "sn";
    private static final String BEACON_KEY_CLIENT_IP_ADDRESS = "ip";
    private static final String BEACON_KEY_MULTIPLICITY = "mp";

    // device data constants
    private static final String BEACON_KEY_DEVICE_OS = "os";
    private static final String BEACON_KEY_DEVICE_MANUFACTURER = "mf";
    private static final String BEACON_KEY_DEVICE_MODEL = "md";

    // timestamp constants
    private static final String BEACON_KEY_SESSION_START_TIME = "tv";
    private static final String BEACON_KEY_TIMESYNC_TIME = "ts";
    private static final String BEACON_KEY_TRANSMISSION_TIME = "tx";

    // Action related constants
    private static final String BEACON_KEY_EVENT_TYPE = "et";
    private static final String BEACON_KEY_NAME = "na";
    private static final String BEACON_KEY_THREAD_ID = "it";
    private static final String BEACON_KEY_ACTION_ID = "ca";
    private static final String BEACON_KEY_PARENT_ACTION_ID = "pa";
    private static final String BEACON_KEY_START_SEQUENCE_NUMBER = "s0";
    private static final String BEACON_KEY_TIME_0 = "t0";
    private static final String BEACON_KEY_END_SEQUENCE_NUMBER = "s1";
    private static final String BEACON_KEY_TIME_1 = "t1";

    // data, error & crash capture constants
    private static final String BEACON_KEY_VALUE = "vl";
    private static final String BEACON_KEY_ERROR_CODE = "ev";
    private static final String BEACON_KEY_ERROR_REASON = "rs";
    private static final String BEACON_KEY_ERROR_STACKTRACE = "st";
    private static final String BEACON_KEY_WEBREQUEST_RESPONSECODE = "rc";
    private static final String BEACON_KEY_WEBREQUEST_BYTES_SENT = "bs";
    private static final String BEACON_KEY_WEBREQUEST_BYTES_RECEIVED = "br";

    // in Java 6 there is no constant for "UTF-8" in the JDK yet, so we define it ourselves
    public static final String CHARSET = "UTF-8";

    // max name length
    private static final int MAX_NAME_LEN = 250;

    // web request tag prefix constant
    private static final String TAG_PREFIX = "MT";

    private static final char BEACON_DATA_DELIMITER = '&';

    // next ID and sequence number
    private final AtomicInteger nextID = new AtomicInteger(0);
    private final AtomicInteger nextSequenceNumber = new AtomicInteger(0);

    // session number & start time
    private final int sessionNumber;
    private final TimingProvider timingProvider;
    private final ThreadIDProvider threadIDProvider;
    private final long sessionStartTime;

    // client IP address
    private final String clientIPAddress;

    // basic beacon data which does not change over time
    private final String immutableBasicBeaconData;

    // AbstractConfiguration reference
    private final Configuration configuration;

    // HTTPClientConfiguration reference
    private final HTTPClientConfiguration httpConfiguration;

    private final Logger logger;

    private final BeaconCacheImpl beaconCache;

    private static final BeaconConfiguration DEFAULT_CONFIGURATION = new BeaconConfiguration(1);
    private final AtomicReference<BeaconConfiguration> beaconConfiguration;

    // *** constructors ***

    /**
     * Constructor.
     *
     * @param logger Logger for logging messages.
     * @param beaconCache Cache storing beacon related data.
     * @param configuration OpenKit related configuration.
     * @param clientIPAddress The client's IP address.
     * @param threadIDProvider Provider for retrieving thread id.
     * @param timingProvider Provider for time related methods.
     */
    public Beacon(Logger logger, BeaconCacheImpl beaconCache, Configuration configuration, String clientIPAddress, ThreadIDProvider threadIDProvider, TimingProvider timingProvider) {
        this.logger = logger;
        this.beaconCache = beaconCache;
        this.sessionNumber = configuration.createSessionNumber();
        this.timingProvider = timingProvider;

        this.configuration = configuration;
        this.threadIDProvider = threadIDProvider;
        this.sessionStartTime = timingProvider.provideTimestampInMilliseconds();

        if (InetAddressValidator.isValidIP(clientIPAddress)) {
            this.clientIPAddress = clientIPAddress;
        } else {
            if (logger.isWarnEnabled()) {
                logger.warning(getClass().getSimpleName() + " Client IP address validation failed: " + clientIPAddress);
            }
            this.clientIPAddress = "";
        }

        // store the current configuration
        this.httpConfiguration = configuration.getHttpClientConfig();

        immutableBasicBeaconData = createImmutableBasicBeaconData();
        beaconConfiguration = new AtomicReference<BeaconConfiguration>(DEFAULT_CONFIGURATION);
    }

    /**
     * Create a unique identifier.
     *
     * <p>
     * The identifier returned is only unique per Beacon.
     * Calling this method on two different Beacon instances, might give the same result.
     * </p>
     *
     * @return A unique identifier.
     */
    public int createID() {
        return nextID.incrementAndGet();
    }

    /**
     * Get the current timestamp in milliseconds by delegating to TimingProvider
     *
     * @return Current timestamp in milliseconds.
     */
    public long getCurrentTimestamp() {
        return timingProvider.provideTimestampInMilliseconds();
    }

    /**
     * Create a unique sequence number.
     *
     * <p>
     * The sequence number returned is only unique per Beacon.
     * Calling this method on two different Beacon instances, might give the same result.
     * </p>
     *
     * @return A unique sequence number.
     */
    public int createSequenceNumber() {
        return nextSequenceNumber.incrementAndGet();
    }

    /**
     * Create a web request tag.
     *
     * <p>
     * Web request tags can be attached as HTTP header for web request tracing.
     * </p>
     *
     * @param parentAction The action for which to create a web request tag.
     * @param sequenceNo Sequence number of the {@link com.dynatrace.openkit.api.WebRequestTracer}.
     * @return A web request tracer tag.
     */
    public String createTag(ActionImpl parentAction, int sequenceNo) {
        return TAG_PREFIX + "_"
            + ProtocolConstants.PROTOCOL_VERSION + "_"
            + httpConfiguration.getServerID() + "_"
            + configuration.getDeviceID() + "_"
            + sessionNumber + "_"
            + configuration.getApplicationID() + "_"
            + parentAction.getID() + "_"
            + threadIDProvider.getThreadID() + "_"
            + sequenceNo;
    }

    /**
     * Add {@link ActionImpl} to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param action The action to add.
     */
    public void addAction(ActionImpl action) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder actionBuilder = new StringBuilder();

        buildBasicEventData(actionBuilder, EventType.ACTION, action.getName());

        addKeyValuePair(actionBuilder, BEACON_KEY_ACTION_ID, action.getID());
        addKeyValuePair(actionBuilder, BEACON_KEY_PARENT_ACTION_ID, action.getParentID());
        addKeyValuePair(actionBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, action.getStartSequenceNo());
        addKeyValuePair(actionBuilder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(action.getStartTime()));
        addKeyValuePair(actionBuilder, BEACON_KEY_END_SEQUENCE_NUMBER, action.getEndSequenceNo());
        addKeyValuePair(actionBuilder, BEACON_KEY_TIME_1, action.getEndTime() - action.getStartTime());

        addActionData(action.getStartTime(), actionBuilder);
    }

    /**
     * Add {@link SessionImpl} to Beacon when session is ended.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param session The session to add.
     */
    public void endSession(SessionImpl session) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        buildBasicEventData(eventBuilder, EventType.SESSION_END, null);

        addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, 0);
        addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
        addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(session.getEndTime()));

        addEventData(session.getEndTime(), eventBuilder);
    }

    /**
     * Add key-value-pair to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this value was reported.
     * @param valueName Value's name.
     * @param value Actual value to report.
     */
    public void reportValue(ActionImpl parentAction, String valueName, int value) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        long eventTimestamp = buildEvent(eventBuilder, EventType.VALUE_INT, valueName, parentAction);
        addKeyValuePair(eventBuilder, BEACON_KEY_VALUE, value);

        addEventData(eventTimestamp, eventBuilder);
    }

    /**
     * Add key-value-pair to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this value was reported.
     * @param valueName Value's name.
     * @param value Actual value to report.
     */
    public void reportValue(ActionImpl parentAction, String valueName, double value) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        long eventTimestamp = buildEvent(eventBuilder, EventType.VALUE_DOUBLE, valueName, parentAction);
        addKeyValuePair(eventBuilder, BEACON_KEY_VALUE, value);

        addEventData(eventTimestamp, eventBuilder);
    }

    /**
     * Add key-value-pair to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this value was reported.
     * @param valueName Value's name.
     * @param value Actual value to report.
     */
    public void reportValue(ActionImpl parentAction, String valueName, String value) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        long eventTimestamp = buildEvent(eventBuilder, EventType.VALUE_STRING, valueName, parentAction);
        if (value != null) {
            addKeyValuePair(eventBuilder, BEACON_KEY_VALUE, truncate(value));
        }

        addEventData(eventTimestamp, eventBuilder);
    }

    /**
     * Add event (aka. named event) to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this event was reported.
     * @param eventName Event's name.
     */
    public void reportEvent(ActionImpl parentAction, String eventName) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        long eventTimestamp = buildEvent(eventBuilder, EventType.NAMED_EVENT, eventName, parentAction);

        addEventData(eventTimestamp, eventBuilder);
    }

    /**
     * Add error to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this error was reported.
     * @param errorName Error's name.
     * @param errorCode Some error code.
     * @param reason Reason for that error.
     */
    public void reportError(ActionImpl parentAction, String errorName, int errorCode, String reason) {
        // if capture errors is off -> do nothing
        if (isCapturingDisabled() || !configuration.isCaptureErrors()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        buildBasicEventData(eventBuilder, EventType.ERROR, errorName);

        long timestamp = timingProvider.provideTimestampInMilliseconds();
        addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, parentAction.getID());
        addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
        addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(timestamp));
        addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_CODE, errorCode);
        if (reason != null) {
            addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_REASON, reason);
        }

        addEventData(timestamp, eventBuilder);
    }

    /**
     * Add crash to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param errorName Error's name.
     * @param reason Reason for that error.
     * @param stacktrace Crash stacktrace.
     */
    public void reportCrash(String errorName, String reason, String stacktrace) {
        // if capture crashes is off -> do nothing
        if (isCapturingDisabled() || !configuration.isCaptureCrashes()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        buildBasicEventData(eventBuilder, EventType.CRASH, errorName);

        long timestamp = timingProvider.provideTimestampInMilliseconds();
        addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, 0);                                  // no parent action
        addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
        addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(timestamp));
        if (reason != null) {
            addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_REASON, reason);
        }
        if (stacktrace != null) {
            addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_STACKTRACE, stacktrace);
        }

        addEventData(timestamp, eventBuilder);
    }

    /**
     * Add web request to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this web request was reported.
     * @param webRequestTracer Web request tracer to serialize.
     */
    public void addWebRequest(ActionImpl parentAction, WebRequestTracerBaseImpl webRequestTracer) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        buildBasicEventData(eventBuilder, EventType.WEBREQUEST, webRequestTracer.getURL());

        addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, parentAction.getID());
        addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, webRequestTracer.getStartSequenceNo());
        addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(webRequestTracer.getStartTime()));
        addKeyValuePair(eventBuilder, BEACON_KEY_END_SEQUENCE_NUMBER, webRequestTracer.getEndSequenceNo());
        addKeyValuePair(eventBuilder, BEACON_KEY_TIME_1, webRequestTracer.getEndTime() - webRequestTracer.getStartTime());

        if (webRequestTracer.getBytesSent() > -1) {
            addKeyValuePair(eventBuilder, BEACON_KEY_WEBREQUEST_BYTES_SENT, webRequestTracer.getBytesSent());
        }

        if (webRequestTracer.getBytesReceived() > -1) {
            addKeyValuePair(eventBuilder, BEACON_KEY_WEBREQUEST_BYTES_RECEIVED, webRequestTracer.getBytesReceived());
        }

        if (webRequestTracer.getResponseCode() != -1) {
            addKeyValuePair(eventBuilder, BEACON_KEY_WEBREQUEST_RESPONSECODE, webRequestTracer.getResponseCode());
        }

        addEventData(webRequestTracer.getStartTime(), eventBuilder);
    }
    /**
     * Add user identification to Beacon.
     *
     * <p>
     * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     * </p>
     *
     * @param userTag User tag containing data to serialize.
     */
    public void identifyUser(String userTag) {

        if (isCapturingDisabled()) {
            return;
        }

        StringBuilder eventBuilder = new StringBuilder();

        buildBasicEventData(eventBuilder, EventType.IDENTIFY_USER, userTag);

        long timestamp = timingProvider.provideTimestampInMilliseconds();
        addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, 0);
        addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
        addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(timestamp));

        addEventData(timestamp, eventBuilder);
    }

    /**
     * Send current state of Beacon.
     *
     * <p>
     * This method tries to send all so far collected and serialized data.
     * </p>
     *
     * @param provider Provider for getting an {@link HTTPClient} required to send the data.
     *
     * @return Returns the last status response retrieved from the server side, or {@code null} if an error occurred.
     */
    public StatusResponse send(HTTPClientProvider provider) {

        HTTPClient httpClient = provider.createClient(httpConfiguration);
        StatusResponse response = null;

        while (true) {

            // prefix for this chunk - must be built up newly, due to changing timestamps
            String prefix = appendMutableBeaconData(immutableBasicBeaconData);
            // subtract 1024 to ensure that the chunk does not exceed the send size configured on server side?
            // i guess that was the original intention, but i'm not sure about this
            // TODO stefan.eberl - This is a quite uncool algorithm and should be improved, avoid subtracting some "magic" number
            String chunk = beaconCache.getNextBeaconChunk(sessionNumber, prefix, configuration.getMaxBeaconSize() - 1024, BEACON_DATA_DELIMITER);
            if (chunk == null || chunk.isEmpty()) {
                // no data added so far or no data to send
                return response;
            }

            byte[] encodedBeacon;
            try {
                encodedBeacon = chunk.getBytes(CHARSET);
            } catch (UnsupportedEncodingException e) {
                // must not happen, as UTF-8 should *really* be supported
                logger.error(getClass().getSimpleName() + "Required charset \"" + CHARSET + "\" is not supported.", e);
                beaconCache.resetChunkedData(sessionNumber);
                return response;
            }

            // send the request
            response = httpClient.sendBeaconRequest(clientIPAddress, encodedBeacon);
            if (response == null) {
                // error happened - but don't know what exactly
                // reset the previously retrieved chunk (restore it in internal cache) & retry another time
                beaconCache.resetChunkedData(sessionNumber);
                break;
            } else {
                // worked -> remove previously retrieved chunk from cache
                beaconCache.removeChunkedData(sessionNumber);
            }
        }

        return response;
    }

    private String appendMutableBeaconData(String immutableBasicBeaconData) {

        StringBuilder mutableBeaconDataBuilder;
        if (immutableBasicBeaconData != null && !immutableBasicBeaconData.isEmpty()) {
            mutableBeaconDataBuilder = new StringBuilder(immutableBasicBeaconData);
            mutableBeaconDataBuilder.append(BEACON_DATA_DELIMITER);
        } else {
            mutableBeaconDataBuilder = new StringBuilder();
        }

        // append timestamp data
        mutableBeaconDataBuilder.append(createTimestampData());

        // append multiplicity
        mutableBeaconDataBuilder.append(BEACON_DATA_DELIMITER)
               .append(createMultiplicityData());

        return mutableBeaconDataBuilder.toString();
    }

    /**
     * Gets all events.
     *
     * <p>
     * This returns a shallow copy of events entries and is intended only
     * for testing purposes.
     * </p>
     */
    String[] getEvents() {
        return beaconCache.getEvents(sessionNumber);
    }

    /**
     * Gets all actions.
     *
     * <p>
     * This returns a shallow copy of all actions and is intended only
     * for testing purposes.
     * </p>
     */
    String[] getActions() {
        return beaconCache.getActions(sessionNumber);
    }

    /**
     * Add previously serialized action data to the beacon cache.
     *
     * @param timestamp The timestamp when the action data occurred.
     * @param actionBuilder Contains the serialized action data.
     */
    private void addActionData(long timestamp, StringBuilder actionBuilder) {

        if (configuration.isCapture()) {
            beaconCache.addActionData(sessionNumber, timestamp, actionBuilder.toString());
        }
    }

    /**
     * Add previously serialized event data to the beacon cache.
     *
     * @param timestamp The timestamp when the event data occurred.
     * @param eventBuilder Contains the serialized event data.
     */
    private void addEventData(long timestamp, StringBuilder eventBuilder) {

        if (configuration.isCapture()) {
            beaconCache.addEventData(sessionNumber, timestamp, eventBuilder.toString());
        }
    }

    /**
     * Clears all previously collected data for this Beacon.
     *
     * <p>
     * This only affects the so far serialized data, which gets removed from the cache.
     * </p>
     */
    public void clearData() {

        // remove all cached data for this Beacon from the cache
        beaconCache.deleteCacheEntry(sessionNumber);
    }

    /**
     * Serialization helper for event data.
     *
     * @param builder String builder storing the serialzed data.
     * @param eventType The event's type.
     * @param name Event name
     * @param parentAction The action on which this event was reported.
     * @return The timestamp associated with the event (timestamp since session start time).
     */
    private long buildEvent(StringBuilder builder, EventType eventType, String name, ActionImpl parentAction) {
        buildBasicEventData(builder, eventType, name);

        long eventTimestamp = timingProvider.provideTimestampInMilliseconds();

        addKeyValuePair(builder, BEACON_KEY_PARENT_ACTION_ID, parentAction.getID());
        addKeyValuePair(builder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
        addKeyValuePair(builder, BEACON_KEY_TIME_0, getTimeSinceSessionStartTime(eventTimestamp));

        return eventTimestamp;
    }

    /**
     * Serialization for building basic event data.
     *
     * @param builder String builder storing serialized data.
     * @param eventType The event's type.
     * @param name Event's name.
     */
    private void buildBasicEventData(StringBuilder builder, EventType eventType, String name) {
        addKeyValuePair(builder, BEACON_KEY_EVENT_TYPE, eventType.protocolValue());
        if (name != null) {
            addKeyValuePair(builder, BEACON_KEY_NAME, truncate(name));
        }
        addKeyValuePair(builder, BEACON_KEY_THREAD_ID, threadIDProvider.getThreadID());
    }

    /**
     * Serialization helper method for creating basic beacon protocol data.
     *
     * @return Serialized data.
     */
    private String createImmutableBasicBeaconData() {
        StringBuilder basicBeaconBuilder = new StringBuilder();

        // version and application information
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_PROTOCOL_VERSION, ProtocolConstants.PROTOCOL_VERSION);
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_OPENKIT_VERSION, ProtocolConstants.OPENKIT_VERSION);
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_ID, configuration.getApplicationID());
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_NAME, configuration.getApplicationName());
        if (configuration.getApplicationVersion() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_VERSION, configuration.getApplicationVersion());
        }
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_PLATFORM_TYPE, ProtocolConstants.PLATFORM_TYPE_OPENKIT);
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_AGENT_TECHNOLOGY_TYPE, ProtocolConstants.AGENT_TECHNOLOGY_TYPE);

        // device/visitor ID, session number and IP address
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_VISITOR_ID, configuration.getDeviceID());
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_SESSION_NUMBER, sessionNumber);
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_CLIENT_IP_ADDRESS, clientIPAddress);

        // platform information
        if (configuration.getDevice().getOperatingSystem() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_OS, configuration.getDevice().getOperatingSystem());
        }
        if (configuration.getDevice().getManufacturer() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_MANUFACTURER, configuration.getDevice()
                                                                                             .getManufacturer());
        }
        if (configuration.getDevice().getModelID() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_MODEL, configuration.getDevice().getModelID());
        }

        return basicBeaconBuilder.toString();
    }

    /**
     * Serialization helper method for creating basic timestamp data.
     *
     * @return Serialized data.
     */
    private String createTimestampData() {
        StringBuilder timestampBuilder = new StringBuilder();

        // timestamp information
        addKeyValuePair(timestampBuilder, BEACON_KEY_SESSION_START_TIME, timingProvider.convertToClusterTime(sessionStartTime));
        addKeyValuePair(timestampBuilder, BEACON_KEY_TIMESYNC_TIME, timingProvider.convertToClusterTime(sessionStartTime));
        if (!timingProvider.isTimeSyncSupported()) {
            addKeyValuePair(timestampBuilder, BEACON_KEY_TRANSMISSION_TIME, timingProvider.provideTimestampInMilliseconds());
        }

        return timestampBuilder.toString();
    }

    /**
     * Serialization helper method for creating multiplicity data.
     *
     * @return Serialized data.
     */
    private String createMultiplicityData() {

        StringBuilder multiplicityBuilder = new StringBuilder();

        // timestamp information
        addKeyValuePair(multiplicityBuilder, BEACON_KEY_MULTIPLICITY, getMultiplicity());

        return multiplicityBuilder.toString();
    }

    /**
     * Serialization helper method for adding key/value pairs with string values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param stringValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, String stringValue) {
        String encodedValue;
        try {
            encodedValue = URLEncoder.encode(stringValue, CHARSET);
        } catch (UnsupportedEncodingException e) {
            // if encoding fails, skip this key/value pair
            logger.error(getClass().getSimpleName() + "Skipped encoding of Key/Value: " + key + "/" + stringValue, e);
            return;
        }

        appendKey(builder, key);
        builder.append(encodedValue);
    }

    /**
     * Serialization helper method for adding key/value pairs with long values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param longValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, long longValue) {
        appendKey(builder, key);
        builder.append(longValue);
    }

    /**
     * Serialization helper method for adding key/value pairs with int values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param intValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, int intValue) {
        appendKey(builder, key);
        builder.append(intValue);
    }

    /**
     * Serialization helper method for adding key/value pairs with double values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param doubleValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, double doubleValue) {
        appendKey(builder, key);
        builder.append(doubleValue);
    }

    /**
     * Serialization helper method for appending a key.
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     */
    private void appendKey(StringBuilder builder, String key) {
        if (!builder.toString().isEmpty()) {
            builder.append('&');
        }
        builder.append(key);
        builder.append('=');
    }

    /**
     * helper method for truncating name at max name size
     */
    private String truncate(String name) {
        name = name.trim();
        if (name.length() > MAX_NAME_LEN) {
            name = name.substring(0, MAX_NAME_LEN);
        }
        return name;
    }

    /**
     * Get a timestamp relative to the time this session (aka. beacon) was created.
     *
     * @param timestamp The absolute timestamp for which to get a relative one.
     * @return Relative timestamp.
     */
    private long getTimeSinceSessionStartTime(long timestamp) {
        return timestamp - sessionStartTime;
    }

    /**
     * Tests if the Beacon is empty.
     *
     * <p>
     * A beacon is considered to be empty, if it does not contain any action or event data.
     * </p>
     *
     * @return {@code true} if the beacon is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return beaconCache.isEmpty(sessionNumber);
    }

    /**
     * Returns the session number.
     *
     * @return the sessionNumber
     */
    public int getSessionNumber() {
        return sessionNumber;
    }

    /**
     * Sets the Beacon configuration.
     *
     * @param beaconConfiguration The new beacon configuration to set.
     */
    public void setBeaconConfiguration(BeaconConfiguration beaconConfiguration) {
        if (beaconConfiguration != null) {
            this.beaconConfiguration.set(beaconConfiguration);
        }
    }

    /**
     * Tests if capturing is allowed.
     *
     * <p>
     *     Note: Due to multithreading this behaviour could already change,
     *     when evaluating the result of this call.
     * </p>
     *
     * @return {@code true} if capturing is allowed, {@code false} otherwise.
     */
    boolean isCapturingDisabled() {
        return !beaconConfiguration.get().isCapturingAllowed();
    }

    /**
     * Get multiplicity from {@link BeaconConfiguration}.
     *
     * @return Multiplicity received from the server.
     */
    int getMultiplicity() {
        return beaconConfiguration.get().getMultiplicity();
    }
}
