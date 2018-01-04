package com.dynatrace.openkit.api;

/**
 * Defines constant values used in OpenKit
 */
public class OpenKitConstants {

    /**
     * Explicit default constructor to hide implicit public one.
     */
    private OpenKitConstants() {
        throw new IllegalStateException("constants class");
    }

    /**
     * Name of Dynatrace HTTP header which is used for tracing web requests.
     */
    public static final String WEBREQUEST_TAG_HEADER = "X-dynaTrace";

    // default values used in configuration
    public static final String DEFAULT_APPLICATION_VERSION = "0.4";
    public static final String DEFAULT_OPERATING_SYSTEM = "OpenKit " + DEFAULT_APPLICATION_VERSION;
    public static final String DEFAULT_MANUFACTURER = "Dynatrace";
    public static final String DEFAULT_MODEL_ID = "OpenKitDevice";

}
