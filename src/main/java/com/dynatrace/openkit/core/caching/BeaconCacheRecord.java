package com.dynatrace.openkit.core.caching;

/**
 * A single record in the {@link BeaconCache}
 *
 * <p>
 *     A record is described by
 *     <ol>
 *         <li>The timestamp when it was created/ended</li>
 *         <li>Serialized data</li>
 *     </ol>
 * </p>
 */
class BeaconCacheRecord {

    private static final int CHAR_SIZE_BYTES = 2;

    private final long timestamp;
    private final String data;
    private boolean markedForSending = false;

    /**
     * Create a new {@link BeaconCacheRecord}
     *
     * @param timestamp Timestamp for this record.
     * @param data Data to store for this record.
     */
    BeaconCacheRecord(long timestamp, String data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    /**
     * Get timestamp.
     */
    long getTimestamp() {
        return timestamp;
    }

    /**
     * Get data.
     */
    String getData() {
        return data;
    }

    /**
     * Get data size estimation of this record.
     *
     * <p>
     *     Note that this is just a very rough estimation required for cache eviction.
     *
     *     It's sufficient to approximate the bytes required by the string and omit any other information like
     *     the timestamp, any references and so on.
     * </p>
     *
     * @return Data size in bytes.
     */
    int getDataSizeInBytes() {
        if (getData() == null) {
            return 0;
        }
        return getData().length() * CHAR_SIZE_BYTES;
    }

    /**
     * Test if this record is already marked for sending.
     *
     * @return {@code true} if this record was previously marked for sending, {@code false} otherwise.
     */
    boolean isMarkedForSending() {
        return markedForSending;
    }

    /**
     * Mark this record for sending ({@link #isMarkedForSending()}).
     */
    void markForSending() {
        markedForSending = true;
    }

    /**
     * Reset marked for sending flag ({@link #isMarkedForSending()}).
     */
    void unsetSending() {
        markedForSending = false;
    }
}
