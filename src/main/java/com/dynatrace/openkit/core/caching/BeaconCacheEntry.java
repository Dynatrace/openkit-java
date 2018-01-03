package com.dynatrace.openkit.core.caching;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents an entry in the {@link BeaconCache}.
 *
 * <p>
 *     When locking is required, the caller is responsible to lock this entry.
 * </p>
 */
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
    private final Lock lock = new ReentrantLock();

    /**
     * List storing all event data being sent.
     */
    private LinkedList<BeaconCacheRecord> eventDataBeingSent;
    /**
     * List storing all action data being sent.
     */
    private LinkedList<BeaconCacheRecord> actionDataBeingSent;

    /**
     * Lock this {@link BeaconCacheEntry} for reading & writing.
     *
     * <p>
     *     When locking is no longer required, {@link #unlock()} must be called.
     * </p>
     */
    void lock() {
        lock.lock();
    }

    /**
     * Release this {@link BeaconCacheEntry} lock, so that other threads can access this object.
     *
     * <p>
     *     When calling this method ensure {@link #lock()} was called before.
     * </p>
     */
    void unlock() {
        lock.unlock();
    }

    /**
     * Add new event data record to cache.
     *
     * @param record The new record to add.
     */
    void addEventData(BeaconCacheRecord record) {
        eventData.add(record);
    }

    /**
     * Add new action data record to the cache.
     *
     * @param record The new record to add.
     */
    void addActionData(BeaconCacheRecord record) {
        actionData.add(record);
    }

    /**
     * Test if data shall be copied, before creating chunks for sending.
     *
     * @return {@code true} if data must be copied, {@code false} otherwise.
     */
    boolean needsDataCopyBeforeChunking() {
        return actionDataBeingSent == null && eventDataBeingSent == null;
    }

    /**
     * Copy data for sending.
     */
    void copyDataForChunking() {
        actionDataBeingSent = actionData;
        eventDataBeingSent = eventData;
        actionData = new LinkedList<BeaconCacheRecord>();
        eventData = new LinkedList<BeaconCacheRecord>();
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
    String getChunk(String chunkPrefix, int maxSize, char delimiter) {

        if (!hasDataToSend()) {
            // nothing to send - reset to null, so next time lists get copied again
            eventDataBeingSent = null;
            actionDataBeingSent = null;
            return "";
        }
        return getNextChunk(chunkPrefix, maxSize, delimiter);
    }

    /**
     * Test if there is more data to send (to chunk).
     *
     * @return {@code true} if there is more data, {@code false} otherwise.
     */
    private boolean hasDataToSend() {

        return (eventDataBeingSent != null && !eventDataBeingSent.isEmpty())
            || (actionDataBeingSent != null && !actionDataBeingSent.isEmpty());
    }

    /**
     * Get the next chunk.
     *
     * @param chunkPrefix The prefix to add to each chunk.
     * @param maxSize The maximum size in characters for one chunk.
     * @param delimiter The delimiter between data chunks.
     *
     * @return The string to send or an empty string if there is no more data to send.
     */
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
     * Remove data that was previously marked for sending when {@link #getNextChunk(String, int, char)} was called.
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
     * This method removes the marked for sending and prepends the copied data back to the data.
     */
    void resetDataMarkedForSending() {

        // reset the "sending marks"
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

        // merge data
        eventDataBeingSent.addAll(eventData);
        actionDataBeingSent.addAll(actionData);
        eventData = eventDataBeingSent;
        actionData = actionDataBeingSent;
        eventDataBeingSent = null;
        actionDataBeingSent = null;
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

        for (BeaconCacheRecord record : eventData) {
            totalNumBytes += record.getDataSizeInBytes();
        }
        for (BeaconCacheRecord record : actionData) {
            totalNumBytes += record.getDataSizeInBytes();
        }

        return totalNumBytes;
    }


    /**
     * Get a shallow copy of event data.
     *
     * <p>
     *     This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getEventData() {
        return new LinkedList<BeaconCacheRecord>(eventData);
    }

    /**
     * Get a snapshot of action data.
     *
     * <p>
     *     This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getActionData() {
        return new LinkedList<BeaconCacheRecord>(actionData);
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
