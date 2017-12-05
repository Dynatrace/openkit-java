/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.openkit.core.ActionImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.WebRequestTracerBaseImpl;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimeProvider;

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
	private static final String BEACON_KEY_VISITOR_ID = "vi";
	private static final String BEACON_KEY_SESSION_NUMBER = "sn";
	private static final String BEACON_KEY_CLIENT_IP_ADDRESS = "ip";

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

	// version constants
	public static final String OPENKIT_VERSION = "7.0.0000";
	private static final int PROTOCOL_VERSION = 3;
	private static final int PLATFORM_TYPE_OPENKIT = 1;

	// in Java 6 there is no constant for "UTF-8" in the JDK yet, so we define it ourselves
	public static final String CHARSET = "UTF-8";

	// max name length
	private static final int MAX_NAME_LEN = 250;

	// web request tag prefix constant
	private static final String TAG_PREFIX = "MT";

	/** Initial time to sleep after the first failed beacon send attempt. */
    private static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

	// next ID and sequence number
	private AtomicInteger nextID = new AtomicInteger(0);
	private AtomicInteger nextSequenceNumber = new AtomicInteger(0);

	// session number & start time
	private final int sessionNumber;
	private final ThreadIDProvider threadIDProvider;
	private final long sessionStartTime;

	// client IP address
	private final String clientIPAddress;

	// basic beacon protocol data
	private String basicBeaconData;

	// AbstractConfiguration reference
	private final AbstractConfiguration configuration;

	// HTTPClientConfiguration reference
	private final HTTPClientConfiguration httpConfiguration;

	// lists of events and actions currently on the Beacon
	private final LinkedList<String> eventDataList = new LinkedList<String>();
	private final LinkedList<String> actionDataList = new LinkedList<String>();

	// *** constructors ***

	public Beacon(AbstractConfiguration configuration, String clientIPAddress, ThreadIDProvider threadIDProvider) {
		this.sessionNumber = configuration.createSessionNumber();
		this.sessionStartTime = TimeProvider.getTimestamp();
		this.configuration = configuration;
		this.clientIPAddress = clientIPAddress;
		this.threadIDProvider = threadIDProvider;

		// store the current configuration
		this.httpConfiguration = configuration.getHttpClientConfig();

		basicBeaconData = createBasicBeaconData();
	}

	// *** public methods ***

	// create next ID
	public int createID() {
		return nextID.incrementAndGet();
	}

	// create next sequence number
	public int createSequenceNumber() {
		return nextSequenceNumber.incrementAndGet();
	}

	// create web request tag
	public String createTag(ActionImpl parentAction, int sequenceNo) {
		return TAG_PREFIX + "_"
				   + PROTOCOL_VERSION + "_"
				   + httpConfiguration.getServerID() + "_"
				   + configuration.getVisitorID() + "_"
				   + sessionNumber + "_"
				   + configuration.getApplicationID() + "_"
				   + parentAction.getID() + "_"
				   + threadIDProvider.getThreadID() + "_"
				   + sequenceNo;
	}

	// add an Action to this Beacon
	public void addAction(ActionImpl action) {
		StringBuilder actionBuilder = new StringBuilder();

		buildBasicEventData(actionBuilder, EventType.ACTION, action.getName());

		addKeyValuePair(actionBuilder, BEACON_KEY_ACTION_ID, action.getID());
		addKeyValuePair(actionBuilder, BEACON_KEY_PARENT_ACTION_ID, action.getParentID());
		addKeyValuePair(actionBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, action.getStartSequenceNo());
		addKeyValuePair(actionBuilder, BEACON_KEY_TIME_0, TimeProvider.getTimeSinceLastInitTime(action.getStartTime()));
		addKeyValuePair(actionBuilder, BEACON_KEY_END_SEQUENCE_NUMBER, action.getEndSequenceNo());
		addKeyValuePair(actionBuilder, BEACON_KEY_TIME_1, action.getEndTime() - action.getStartTime());

		synchronized (actionDataList) {
			actionDataList.add(actionBuilder.toString());
		}
	}

	// end Session on this Beacon
	public void endSession(SessionImpl session) {
		StringBuilder eventBuilder = new StringBuilder();

		buildBasicEventData(eventBuilder, EventType.SESSION_END, null);

		addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, 0);
		addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
		addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, TimeProvider.getTimeSinceLastInitTime(session.getEndTime()));

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// report int value on the provided Action
	public void reportValue(ActionImpl parentAction, String valueName, int value) {
		StringBuilder eventBuilder = new StringBuilder();

		buildEvent(eventBuilder, EventType.VALUE_INT, valueName, parentAction);
		addKeyValuePair(eventBuilder, BEACON_KEY_VALUE, value);

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// report double value on the provided Action
	public void reportValue(ActionImpl parentAction, String valueName, double value) {
		StringBuilder eventBuilder = new StringBuilder();

		buildEvent(eventBuilder, EventType.VALUE_DOUBLE, valueName, parentAction);
		addKeyValuePair(eventBuilder, BEACON_KEY_VALUE, value);

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// report string value on the provided Action
	public void reportValue(ActionImpl parentAction, String valueName, String value) {
		StringBuilder eventBuilder = new StringBuilder();

		buildEvent(eventBuilder, EventType.VALUE_STRING, valueName, parentAction);
		addKeyValuePair(eventBuilder, BEACON_KEY_VALUE, truncate(value));

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// report named event on the provided Action
	public void reportEvent(ActionImpl parentAction, String eventName) {
		StringBuilder eventBuilder = new StringBuilder();

		buildEvent(eventBuilder, EventType.NAMED_EVENT, eventName, parentAction);

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// report error on the provided Action
	public void reportError(ActionImpl parentAction, String errorName, int errorCode, String reason) {
		// if capture errors is off -> do nothing
		if (!configuration.isCaptureErrors()) {
			return;
		}

		StringBuilder eventBuilder = new StringBuilder();

		buildBasicEventData(eventBuilder, EventType.ERROR, errorName);

		addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, parentAction.getID());
		addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
		addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, TimeProvider.getTimeSinceLastInitTime());
		addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_CODE, errorCode);
		addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_REASON, reason);

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// report a crash
	public void reportCrash(String errorName, String reason, String stacktrace) {
		// if capture crashes is off -> do nothing
		if (!configuration.isCaptureCrashes()) {
			return;
		}

		StringBuilder eventBuilder = new StringBuilder();

		buildBasicEventData(eventBuilder, EventType.CRASH, errorName);

		addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, 0);                                  // no parent action
		addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
		addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, TimeProvider.getTimeSinceLastInitTime());
		addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_REASON, reason);
		addKeyValuePair(eventBuilder, BEACON_KEY_ERROR_STACKTRACE, stacktrace);

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// add web request to the provided Action
	public void addWebRequest(ActionImpl parentAction, WebRequestTracerBaseImpl webRequestTracer) {
		StringBuilder eventBuilder = new StringBuilder();

		buildBasicEventData(eventBuilder, EventType.WEBREQUEST, webRequestTracer.getURL());

		addKeyValuePair(eventBuilder, BEACON_KEY_PARENT_ACTION_ID, parentAction.getID());
		addKeyValuePair(eventBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, webRequestTracer.getStartSequenceNo());
		addKeyValuePair(eventBuilder, BEACON_KEY_TIME_0, TimeProvider.getTimeSinceLastInitTime(webRequestTracer.getStartTime()));
		addKeyValuePair(eventBuilder, BEACON_KEY_END_SEQUENCE_NUMBER, webRequestTracer.getEndSequenceNo());
		addKeyValuePair(eventBuilder, BEACON_KEY_TIME_1, webRequestTracer.getEndTime() - webRequestTracer.getStartTime());
		if (webRequestTracer.getResponseCode() != -1) {
			addKeyValuePair(eventBuilder, BEACON_KEY_WEBREQUEST_RESPONSECODE, webRequestTracer.getResponseCode());
		}

		synchronized (eventDataList) {
			eventDataList.add(eventBuilder.toString());
		}
	}

	// send current state of Beacon
	public StatusResponse send(HTTPClientProvider provider, int numRetries) throws InterruptedException {
		HTTPClient httpClient = provider.createClient(httpConfiguration);
		ArrayList<byte[]> beaconDataChunks = createBeaconDataChunks();
		StatusResponse response = null;
		for (byte[] beaconData : beaconDataChunks) {
		    response = sendBeaconRequest(httpClient, beaconData, numRetries);
		}

		// only return last status response for updating the settings
		return response;
	}

    private StatusResponse sendBeaconRequest(HTTPClient httpClient, byte[] beaconData, int numRetries) throws InterruptedException {

        StatusResponse response;
        int retry = 0;
        long retrySleepMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;

        while (true) {

            response = httpClient.sendBeaconRequest(clientIPAddress, beaconData);
            if (response != null || (retry >= numRetries)) {
                break; // success or max retry count reached
            }

            Thread.sleep(retrySleepMillis);
            retrySleepMillis *= 2;
            retry++;
        }

        return response;
    }

    // *** private methods ***

	// helper method for building events
	private void buildEvent(StringBuilder builder, EventType eventType, String name, ActionImpl parentAction) {
		buildBasicEventData(builder, eventType, name);

		addKeyValuePair(builder, BEACON_KEY_PARENT_ACTION_ID, parentAction.getID());
		addKeyValuePair(builder, BEACON_KEY_START_SEQUENCE_NUMBER, createSequenceNumber());
		addKeyValuePair(builder, BEACON_KEY_TIME_0, TimeProvider.getTimeSinceLastInitTime());
	}

	// helper method for building basic event data
	private void buildBasicEventData(StringBuilder builder, EventType eventType, String name) {
		addKeyValuePair(builder, BEACON_KEY_EVENT_TYPE, eventType.protocolValue());
		if (name != null) {
			addKeyValuePair(builder, BEACON_KEY_NAME, truncate(name));
		}
		addKeyValuePair(builder, BEACON_KEY_THREAD_ID, threadIDProvider.getThreadID());
	}

	// creates (possibly) multiple beacon chunks based on max beacon size
	public ArrayList<byte[]> createBeaconDataChunks() {
		ArrayList<byte[]> beaconDataChunks = new ArrayList<byte[]>();

		synchronized (eventDataList) {
			synchronized (actionDataList) {
				while (!eventDataList.isEmpty() || !actionDataList.isEmpty()) {
					StringBuilder beaconBuilder = new StringBuilder();

					beaconBuilder.append(basicBeaconData);
					beaconBuilder.append('&');
					beaconBuilder.append(createTimestampData());

					while (!eventDataList.isEmpty()) {
						if (beaconBuilder.length() > configuration.getMaxBeaconSize() - 1024) {
							break;
						}

						String eventData = eventDataList.removeFirst();
						beaconBuilder.append('&');
						beaconBuilder.append(eventData);
					}

					while (!actionDataList.isEmpty()) {
						if (beaconBuilder.length() > configuration.getMaxBeaconSize() - 1024) {
							break;
						}

						String actionData = actionDataList.removeFirst();
						beaconBuilder.append('&');
						beaconBuilder.append(actionData);
					}

					byte[] encodedBeacon = null;
					try {
						encodedBeacon = beaconBuilder.toString().getBytes(CHARSET);
					} catch (UnsupportedEncodingException e) {
						// must not happen, as UTF-8 should *really* be supported
					}
					beaconDataChunks.add(encodedBeacon);
				}
			}
		}

		return beaconDataChunks;
	}

	// helper method for creating basic beacon protocol data
	private String createBasicBeaconData() {
		StringBuilder basicBeaconBuilder = new StringBuilder();

		// version and application information
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_PROTOCOL_VERSION, PROTOCOL_VERSION);
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_OPENKIT_VERSION, OPENKIT_VERSION);
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_ID, configuration.getApplicationID());
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_NAME, configuration.getApplicationName());
		if (configuration.getApplicationVersion() != null) {
			addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_VERSION, configuration.getApplicationVersion());
		}
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_PLATFORM_TYPE, PLATFORM_TYPE_OPENKIT);

		// visitor ID, session number and IP address
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_VISITOR_ID, configuration.getVisitorID());
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_SESSION_NUMBER, sessionNumber);
		addKeyValuePair(basicBeaconBuilder, BEACON_KEY_CLIENT_IP_ADDRESS, clientIPAddress);

		// platform information
		if (configuration.getDevice().getOperatingSystem() != null) {
			addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_OS, configuration.getDevice().getOperatingSystem());
		}
		if (configuration.getDevice().getManufacturer() != null) {
			addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_MANUFACTURER, configuration.getDevice().getManufacturer());
		}
		if (configuration.getDevice().getModelID() != null) {
			addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_MODEL, configuration.getDevice().getModelID());
		}

		return basicBeaconBuilder.toString();
	}

	// helper method for creating basic timestamp data
	private String createTimestampData() {
		StringBuilder timestampBuilder = new StringBuilder();

		// timestamp information
		addKeyValuePair(timestampBuilder, BEACON_KEY_SESSION_START_TIME, TimeProvider.convertToClusterTime(sessionStartTime));
		addKeyValuePair(timestampBuilder, BEACON_KEY_TIMESYNC_TIME, TimeProvider.getLastInitTimeInClusterTime());
		if (!TimeProvider.isTimeSynced()) {
			addKeyValuePair(timestampBuilder, BEACON_KEY_TRANSMISSION_TIME, TimeProvider.getTimestamp());
		}

		return timestampBuilder.toString();
	}

	// helper method for adding key/value pairs with string values
	private void addKeyValuePair(StringBuilder builder, String key, String stringValue) {
		String encodedValue;
		try {
			encodedValue = URLEncoder.encode(stringValue, CHARSET);
		} catch (UnsupportedEncodingException e) {
			// if encoding fails, skip this key/value pair
			if (configuration.isVerbose()) {
				System.out.println("Skipped encoding of Key/Value: " + key + "/" + stringValue);
			}
			return;
		}

		appendKey(builder, key);
		builder.append(encodedValue);
	}

	// helper method for adding key/value pairs with long values
	private void addKeyValuePair(StringBuilder builder, String key, long longValue) {
		appendKey(builder, key);
		builder.append(longValue);
	}

	// helper method for adding key/value pairs with int values
	private void addKeyValuePair(StringBuilder builder, String key, int intValue) {
		appendKey(builder, key);
		builder.append(intValue);
	}

	// helper method for adding key/value pairs with double values
	private void addKeyValuePair(StringBuilder builder, String key, double doubleValue) {
		appendKey(builder, key);
		builder.append(doubleValue);
	}

	// helper method for appending a key
	private void appendKey(StringBuilder builder, String key) {
		if (!builder.toString().isEmpty()) {
			builder.append('&');
		}
		builder.append(key);
		builder.append('=');
	}

	// helper method for truncating name at max name size
	private String truncate(String name) {
		name = name.trim();
		if (name.length() > MAX_NAME_LEN) {
			name = name.substring(0, MAX_NAME_LEN);
		}
		return name;
	}

}
