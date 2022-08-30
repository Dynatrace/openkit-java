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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents an entry in the {@link BeaconCacheImpl}.
 *
 * <p>
 * The caller is responsible to lock this element, before the first method is invoked using
 * {@link #lock()} and after the last operation is invoked {@link #unlock()} must be called.
 * </p>
 */
class BeaconCacheEntry {

    /**
     * List storing all active event data.
     */
    private LinkedList<BeaconCacheRecord> eventData = new LinkedList<>();

    /**
     * List storing all active session data.
     */
    private LinkedList<BeaconCacheRecord> actionData = new LinkedList<>();

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
     * Total number of bytes consumed by this entry.
     */
    private long totalNumBytes = 0;

    /**
     * Lock this {@link BeaconCacheEntry} for reading & writing.
     *
     * <p>
     * When locking is no longer required, {@link #unlock()} must be called.
     * </p>
     */
    void lock() {
        lock.lock();
    }

    /**
     * Release this {@link BeaconCacheEntry} lock, so that other threads can access this object.
     *
     * <p>
     * When calling this method ensure {@link #lock()} was called before.
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
        totalNumBytes += record.getDataSizeInBytes();
    }

    /**
     * Add new action data record to the cache.
     *
     * @param record The new record to add.
     */
    void addActionData(BeaconCacheRecord record) {
        actionData.add(record);
        totalNumBytes += record.getDataSizeInBytes();
    }

    /**
     * Test if data shall be copied, before creating chunks for sending.
     *
     * @return {@code true} if data must be copied, {@code false} otherwise.
     */
    boolean needsDataCopyBeforeSending() {
        return !hasDataToSend();
    }

    /**
     * Copy data for sending.
     */
    void copyDataForSending() {
        actionDataBeingSent = actionData;
        eventDataBeingSent = eventData;
        actionData = new LinkedList<>();
        eventData = new LinkedList<>();
        totalNumBytes = 0; // data which is being sent is not counted
    }

    /**
     * Get next data chunk to send to the Dynatrace backend system.
     *
     * <p>
     * This method is called from beacon sending thread.
     * </p>
     *
     * @param chunkPrefix The prefix to add to each chunk.
     * @param maxSize     The maximum size in characters for one chunk.
     * @param delimiter   The delimiter between data chunks.
     *
     * @return The string to send or an empty string if there is no more data to send.
     */
    String getChunk(String chunkPrefix, int maxSize, char delimiter) {

        if (!hasDataToSend()) {
            return "";
        }
        return getNextChunk(chunkPrefix, maxSize, delimiter);
    }

    /**
     * Test if there is more data to send (to chunk).
     *
     * @return {@code true} if there is more data, {@code false} otherwise.
     */
    boolean hasDataToSend() {
        return (eventDataBeingSent != null && !eventDataBeingSent.isEmpty())
            || (actionDataBeingSent != null && !actionDataBeingSent.isEmpty());
    }

    /**
     * Get the next chunk.
     *
     * @param chunkPrefix The prefix to add to each chunk.
     * @param maxSize     The maximum size in characters for one chunk.
     * @param delimiter   The delimiter between data chunks.
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

    private static void chunkifyDataList(StringBuilder chunkBuilder, LinkedList<BeaconCacheRecord> dataBeingSent, int maxSize, char delimiter) {

        Iterator<BeaconCacheRecord> iterator = dataBeingSent.iterator();
        while (iterator.hasNext() && chunkBuilder.length() <= maxSize) {

            // mark the record for sending
            BeaconCacheRecord record = iterator.next();
            record.markForSending();

            // append delimiter & data
            chunkBuilder.append(delimiter).append(record.getData());
        }
    }

    /**
     * Remove data that was previously marked for sending when {@link #getNextChunk(String, int, char)} was called.
     */
    void removeDataMarkedForSending() {

        if (!hasDataToSend()) {
            // data has not been copied yet - avoid NPE
            return;
        }

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

        if (!hasDataToSend()) {
            // data has not been copied yet - avoid NPE
            return;
        }

        // reset the "sending marks" and in the same traversal count the bytes which are added back
        long numBytes = 0;
        for (BeaconCacheRecord record : eventDataBeingSent) {
            record.unsetSending();
            numBytes += record.getDataSizeInBytes();
        }

        for (BeaconCacheRecord record : actionDataBeingSent) {
            record.unsetSending();
            numBytes += record.getDataSizeInBytes();
        }

        // merge data
        eventDataBeingSent.addAll(eventData);
        actionDataBeingSent.addAll(actionData);
        eventData = eventDataBeingSent;
        actionData = actionDataBeingSent;
        eventDataBeingSent = null;
        actionDataBeingSent = null;

        totalNumBytes += numBytes;
    }


    /**
     * Get total number of bytes used.
     *
     * <p>
     * Note: The number of bytes is calculated from the lists where active records are added.
     * Data that is currently being sent is not taken into account, since we assume sending is
     * successful and therefore this data is just temporarily stored.
     * </p>
     *
     * @return Sum of data size in bytes for each {@link BeaconCacheRecord}.
     */
    long getTotalNumberOfBytes() {

        return totalNumBytes;
    }

    /**
     * Remove all {@link BeaconCacheRecord beacon cache records} from event and action data
     * which are older than given minTimestamp
     *
     * <p>
     * Records which are currently being sent are not evicted.
     * </p>
     *
     * @param minTimestamp The minimum timestamp allowed.
     *
     * @return The total number of removed records.
     */
    int removeRecordsOlderThan(long minTimestamp) {


        int numRecordsRemoved = removeRecordsOlderThan(eventData, minTimestamp);
        numRecordsRemoved += removeRecordsOlderThan(actionData, minTimestamp);

        return numRecordsRemoved;
    }

    /**
     * Remove all {@link BeaconCacheRecord beacon cache records} from {@code records}.
     *
     * @param minTimestamp The minimum timestamp allowed.
     *
     * @return The number of records removed from {@code records}.
     */
    private static int removeRecordsOlderThan(List<BeaconCacheRecord> records, long minTimestamp) {

        int numRecordsRemoved = 0;

        Iterator<BeaconCacheRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            BeaconCacheRecord record = iterator.next();
            if (record.getTimestamp() < minTimestamp) {
                iterator.remove();
                numRecordsRemoved++;
            }
        }

        return numRecordsRemoved;
    }

    /**
     * Remove up to {@code numRecords} records from event & action data, compared by their age.
     *
     * <p>
     * Note not all event/action data entries are traversed, only the first action data & first event
     * data is removed and compared against each other, which one to remove first. If the first action's timestamp and
     * first event's timestamp are equal, the first event is removed.
     * </p>
     *
     * @param numRecords The number of records.
     *
     * @return Number of actually removed records.
     */
    int removeOldestRecords(int numRecords) {

        int numRecordsRemoved = 0;

        Iterator<BeaconCacheRecord> eventsIterator = eventData.iterator();
        Iterator<BeaconCacheRecord> actionsIterator = actionData.iterator();
        BeaconCacheRecord currentEvent = eventsIterator.hasNext() ? eventsIterator.next() : null;
        BeaconCacheRecord currentAction = actionsIterator.hasNext() ? actionsIterator.next() : null;

        while (numRecordsRemoved < numRecords && (currentEvent != null || currentAction != null)) {

            if (currentEvent == null) {
                // actions is not null -> remove action
                currentAction = removeAndAdvanceIterator(actionsIterator);
            } else if (currentAction == null) {
                // events is not null -> remove event
                currentEvent = removeAndAdvanceIterator(eventsIterator);
            } else {
                // both are not null -> compare by timestamp and take the older one
                if (currentAction.getTimestamp() < currentEvent.getTimestamp()) {
                    // first action is older than first event
                    currentAction = removeAndAdvanceIterator(actionsIterator);
                } else {
                    // first event is older than first action
                    currentEvent = removeAndAdvanceIterator(eventsIterator);
                }
            }

            numRecordsRemoved++;
        }

        return numRecordsRemoved;
    }

    private static <E> E removeAndAdvanceIterator(Iterator<E> iterator) {
        iterator.remove();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Get a shallow copy of event data.
     *
     * <p>
     * This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getEventData() {
        return new LinkedList<>(eventData);
    }

    /**
     * Get a snapshot of action data.
     *
     * <p>
     * This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getActionData() {
        return new LinkedList<>(actionData);
    }

    /**
     * Get a readonly list of event data being sent.
     *
     * <p>
     * This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getEventDataBeingSent() {
        return eventDataBeingSent == null ? null : Collections.unmodifiableList(eventDataBeingSent);
    }

    /**
     * Get a readonly list of action data being sent.
     *
     * <p>
     * This method shall only be used for testing purposes.
     * </p>
     */
    List<BeaconCacheRecord> getActionDataBeingSent() {
        return actionDataBeingSent == null ? null : Collections.unmodifiableList(actionDataBeingSent);
    }
}
