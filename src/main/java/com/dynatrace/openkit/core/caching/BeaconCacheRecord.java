/**
 * Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit.core.caching;

import java.util.Arrays;

/**
 * A single record in the {@link BeaconCacheImpl}
 *
 * <p>
 * A record is described by
 * <ol>
 * <li>The timestamp when it was created/ended</li>
 * <li>Serialized data</li>
 * </ol>
 * </p>
 */
class BeaconCacheRecord {

    private static final long CHAR_SIZE_BYTES = 2L;

    private final long timestamp;
    private final String data;
    private boolean markedForSending = false;

    /**
     * Create a new {@link BeaconCacheRecord}
     *
     * @param timestamp Timestamp for this record.
     * @param data      Data to store for this record.
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
     * Note that this is just a very rough estimation required for cache eviction.
     *
     * It's sufficient to approximate the bytes required by the string and omit any other information like
     * the timestamp, any references and so on.
     * </p>
     *
     * @return Data size in bytes.
     */
    long getDataSizeInBytes() {
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeaconCacheRecord record = (BeaconCacheRecord) o;
        return getTimestamp() == record.getTimestamp() && isMarkedForSending() == record.isMarkedForSending() && getData()
            .equals(record.getData());
    }

    @Override
    public int hashCode() {

        return Arrays.hashCode(new Object[]{getTimestamp(), getData(), isMarkedForSending()});
    }
}
