package com.dynatrace.openkit.core.caching;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class BeaconCacheEntry {

    /**
     * List storing all active event data.
     */
    private LinkedList<BeaconCacheRecord> eventData = new LinkedList<BeaconCacheRecord>();

    /**
     * List storing all active session data.
     */
    private LinkedList<BeaconCacheRecord> actionData = new LinkedList<BeaconCacheRecord>();

    /**
     * Lock object for locking access to session & event data.
     */
    private final Object lockObject = new Object();

    /**
     * List storing all event data being sent.
     */
    private LinkedList<BeaconCacheRecord> eventDataBeingSent;
    /**
     * List storing all action data being sent.
     */
    private LinkedList<BeaconCacheRecord> actionDataBeingSent;


    /**
     * Add new event data record to cache.
     *
     * <p>
     *     This method is called from the Beacon and
     *     therefore potentially from multiple threads in parallel.
     * </p>
     *
     * @param record
     */
    void addEventData(BeaconCacheRecord record) {

        synchronized (lockObject) {
            eventData.add(record);
        }
    }

    /**
     * Add new action data record to the cache.
     *
     * <p>
     *     This method is called from the Beacon and
     *     therefore potentially from multiple threads in parallel.
     * </p>
     *
     * @param record
     */
    void addActionData(BeaconCacheRecord record) {

        synchronized (lockObject) {
            actionData.add(record);
        }
    }

    /**
     * Get next data chunk to send to the Dynatrace backend system.
     *
     * <p>
     *     This method is called from beacon sending thread.
     * </p>
     *
     * @param chunkPrefix The prefix to add to each chunk.
     * @param maxSize The maximum size in characters for one chunk.
     * @param delimiter The delimiter between data chunks.
     *
     * @return The string to send or an empty string if there is no more data to send.
     */
    String getDataToSend(String chunkPrefix, int maxSize, char delimiter) {

        if (eventDataBeingSent == null && actionDataBeingSent == null) {
            // both buffers are null
            // perform swap operation and chunk data
            copyDataForSending();
        }

        if (!hasDataToSend()) {
            // nothing to send - reset to null, so next time lists get copied again
            eventDataBeingSent = null;
            actionDataBeingSent = null;
            return "";
        }
        return getNextChunk(chunkPrefix, maxSize, delimiter);
    }

    private void copyDataForSending() {

        synchronized (lockObject) {
            actionDataBeingSent = actionData;
            eventDataBeingSent = eventData;
            actionData = new LinkedList<BeaconCacheRecord>();
            eventData = new LinkedList<BeaconCacheRecord>();
        }
    }

    private boolean hasDataToSend() {

        return (eventDataBeingSent != null && !eventDataBeingSent.isEmpty())
            || (actionDataBeingSent != null && !actionDataBeingSent.isEmpty());
    }

    private String getNextChunk(String chunkPrefix, int maxSize, char delimiter) {

        // create the string builder
        StringBuilder beaconBuilder = new StringBuilder(maxSize);

        // append the chunk prefix
        beaconBuilder.append(chunkPrefix);

        // append data from both lists
        // note the order is currently important -> event data goes first, then action data
        chunkifyDataList(beaconBuilder, eventDataBeingSent, maxSize, delimiter);
        chunkifyDataList(beaconBuilder, actionDataBeingSent, maxSize, delimiter);

        return beaconBuilder.toString();
    }

    private static void chunkifyDataList(StringBuilder chunkBuilder,
                                         LinkedList<BeaconCacheRecord> dataBeingSent,
                                         int maxSize,
                                         char delimiter) {

        Iterator<BeaconCacheRecord> iterator = dataBeingSent.iterator();
        while (iterator.hasNext() && chunkBuilder.length() <= maxSize) {

            // mark the record for sending
            BeaconCacheRecord record = iterator.next();
            record.markForSending();

            // append delimiter & data
            chunkBuilder.append(delimiter)
                        .append(record.getData());
        }
    }

    /**
     * Get total number of bytes used.
     *
     * <p>
     *     Note: As decided this is only taken from the lists where active records are added.
     *     Data that is currently being sent is not taken into account, since we assume sending is
     *     successful and therefore this data is just temporarily stored.
     * </p>
     *
     * @return Sum of data size in bytes for each {@link BeaconCacheRecord}.
     */
    int getTotalNumberOfBytes() {

        int totalNumBytes = 0;

        synchronized (lockObject) {
            for (BeaconCacheRecord record : eventData) {
                totalNumBytes += record.getDataSizeInBytes();
            }
            for (BeaconCacheRecord record : actionData) {
                totalNumBytes += record.getDataSizeInBytes();
            }
        }

        return totalNumBytes;
    }

    /**
     * Remove data that was previously marked for sending when {@link #getDataToSend} was called.
     */
    void removeDataMarkedForSending() {

        Iterator<BeaconCacheRecord> iterator = eventDataBeingSent.iterator();
        while (iterator.hasNext() && iterator.next().isMarkedForSending()) {
            iterator.remove();
        }

        if (!iterator.hasNext()) {
            // only check action data, if all event data has been traversed, otherwise it's just waste of cpu time
            iterator = actionDataBeingSent.iterator();
            while (iterator.hasNext() && iterator.next().isMarkedForSending()) {
                iterator.remove();
            }
        }
    }

    /**
     * Reset data that was previously marked for sending when {@link #getDataToSend} was called.
     */
    void resetDataMarkedForSending() {

        Iterator<BeaconCacheRecord> iterator = eventDataBeingSent.iterator();
        while (iterator.hasNext()) {
            BeaconCacheRecord record = iterator.next();
            if (record.isMarkedForSending()) {
                record.unsetSending();
            } else {
                break;
            }
        }

        if (!iterator.hasNext()) {
            // only check action data, if all event data has been traversed, otherwise it's just waste of cpu time
            iterator = actionDataBeingSent.iterator();
            while (iterator.hasNext()) {
                BeaconCacheRecord record = iterator.next();
                if (record.isMarkedForSending()) {
                    record.unsetSending();
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Get a shallow copy of event data.
     *
     * <p>
     *     This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getEventData() {
        synchronized (lockObject) {
            return new LinkedList<BeaconCacheRecord>(eventData);
        }
    }

    /**
     * Get a snapshot of action data.
     *
     * <p>
     *     This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getActionData() {
        synchronized (lockObject) {
            return new LinkedList<BeaconCacheRecord>(actionData);
        }
    }

    /**
     * Get a readonly list of event data being sent.
     *
     * <p>
     *     This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getEventDataBeingSent() {
        return eventDataBeingSent == null ? null : Collections.unmodifiableList(eventDataBeingSent);
    }

    /**
     * Get a readonly list of action data being sent.
     *
     * <p>
     *     This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getActionDataBeingSent() {
        return actionDataBeingSent == null ? null : Collections.unmodifiableList(actionDataBeingSent);
    }
}
