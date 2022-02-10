package com.dynatrace.openkit.util.json.objects;

/**
 * Determines the content of the output string when json is converted
 */
public enum JSONOutputConfig {
    /**
     * Outputs all valid json values
     */
    DEFAULT,
    /**
     * Outpus all valid json values but will ignore null values
     */
    IGNORE_NULL
}
