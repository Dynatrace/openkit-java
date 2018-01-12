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

package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.protocol.Beacon;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class used in OpenKit to cache serialized {@link Beacon} data.
 *
 * <p>
 *     This cache needs to deal with high concurrency, since it's possible that a lot of threads
 *     insert new data concurrently.
 *
 *     Furthermore the sending thread also accesses this cache and also an own eviction cache.
 * </p>
 */
public class BeaconCacheImpl extends Observable implements BeaconCache {

    private final ReadWriteLock globalCacheLock;
    private final Map<Integer, BeaconCacheEntry> beacons;
    private final BeaconCacheStats cacheStats;

    /**
     * Create BeaconCache.
     */
    public BeaconCacheImpl() {
        globalCacheLock = new ReentrantReadWriteLock();
        beacons = new HashMap<Integer, BeaconCacheEntry>();
        cacheStats = new BeaconCacheStats();
    }


    @Override
    public void addEventData(Integer beaconID, long timestamp, String data) {

        // get a reference to the cache entry
        BeaconCacheEntry entry = getCachedEntryOrInsert(beaconID);

        BeaconCacheRecord record = new BeaconCacheRecord(timestamp, data);

        try {
            // lock and add the data
            entry.lock();
            entry.addEventData(record);
        } finally {
            entry.unlock();
        }

        // update cache stats
        cacheStats.numBytesAdded(record.getDataSizeInBytes());

        // notify observers
        onDataAdded();
    }

    @Override
    public void addActionData(Integer beaconID, long timestamp, String data) {

        BeaconCacheEntry entry = getCachedEntryOrInsert(beaconID);

        // add event data for that beacon
        BeaconCacheRecord record = new BeaconCacheRecord(timestamp, data);

        try {
            // lock and add the data
            entry.lock();
            entry.addActionData(record);
        } finally {
            entry.unlock();
        }

        // update cache stats
        cacheStats.numBytesAdded(record.getDataSizeInBytes());

        // notify observers
        onDataAdded();
    }

    @Override
    public void deleteCacheEntry(Integer beaconID) {

        BeaconCacheEntry entry;
        try {
            globalCacheLock.writeLock().lock();
            entry = beacons.remove(beaconID);

        } finally {
            globalCacheLock.writeLock().unlock();
        }

        if (entry != null) {
            cacheStats.numBytesRemoved(entry.getTotalNumberOfBytes());
        }
    }


    @Override
    public String getNextBeaconChunk(Integer beaconID, String chunkPrefix, int maxSize, char delimiter) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // a cache entry for the given beaconID does not exist
            return null;
        }

        if (entry.needsDataCopyBeforeChunking()) {
            // both entries are null, prepare data for sending
            long numBytes;
            try {
                entry.lock();
                numBytes = entry.getTotalNumberOfBytes();
                entry.copyDataForChunking();

            } finally {
                entry.unlock();
            }
            // assumption: sending will work fine, and everything we copied will be removed quite soon
            cacheStats.numBytesRemoved(numBytes);
        }

        // data for chunking is available
        return entry.getChunk(chunkPrefix, maxSize, delimiter);
    }

    /**
     * Remove all data that was previously included in chunks.
     *
     * <p>
     *     This method must be called, when data retrieved via {@link #getNextBeaconChunk(Integer, String, int, char)}
     *     was successfully sent to the backend, otherwise subsequent calls to {@link #getNextBeaconChunk(Integer, String, int, char)}
     *     will retrieve the same data again and again.
     * </p>
     *
     * <p>
     *     Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param beaconID The beacon id for which to remove already chunked data.
     */
    @Override
    public void removeChunkedData(Integer beaconID) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // a cache entry for the given beaconID does not exist
            return;
        }

        entry.removeDataMarkedForSending();
    }

    /**
     * Reset all data that was previously included in chunks.
     *
     * <p>
     *     Note: This method should only be invoked from the beacon sending thread,
     *     but can be from any, since it's thread safe.
     * </p>
     *
     * @param beaconID The beacon id for which to remove already chunked data.
     */
    @Override
    public void resetChunkedData(Integer beaconID) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // a cache entry for the given beaconID does not exist
            return;
        }

        long numBytes;
        try {
            entry.lock();
            long oldSize = entry.getTotalNumberOfBytes();
            entry.resetDataMarkedForSending();
            long newSize = entry.getTotalNumberOfBytes();
            numBytes = newSize - oldSize;
        } finally {
            entry.unlock();
        }

        cacheStats.numBytesAdded(numBytes);

        // notify observers
        onDataAdded();
    }

    /**
     * Get cached {@link BeaconCacheEntry} or insert new one if nothing exists for given {@code beaconID}.
     *
     * @param beaconID The beacon id to search for.
     *
     * @return The already cached entry or newly created one.
     */
    private BeaconCacheEntry getCachedEntryOrInsert(Integer beaconID) {

        // get the appropriate cache entry
        BeaconCacheEntry entry = getCachedEntry(beaconID);

        if (entry == null) {
            try {
                // does not exist, and needs to be inserted
                globalCacheLock.writeLock().lock();
                if (!beacons.containsKey(beaconID)) {
                    // double check since this could have been added in the mean time
                    entry = new BeaconCacheEntry();
                    beacons.put(beaconID, entry);
                } else {
                    entry = beacons.get(beaconID);
                }
            } finally {
                globalCacheLock.writeLock().unlock();
            }
        }

        return entry;
    }

    /**
     * Get a shallow copy of events collected so far.
     *
     * <p>
     *     Although this method is intended for debugging purposes only, it still does appropriate locking.
     * </p>
     *
     * @param beaconID The beacon id for which to retrieve the events.
     *
     * @return List of event data.
     */
    public String[] getEvents(Integer beaconID) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // entry not found
            return new String[0];
        }

        try {
            entry.lock();
            return extractData(entry.getEventData());
        } finally {
            entry.unlock();
        }
    }

    /**
     * Get a shallow copy of events that are about to be sent.
     *
     * <P>
     * This method is only intended for internal unit tests.
     * </P>
     *
     * @param beaconID The beacon id for which to retrieve the events.
     *
     * @return List of event data.
     */
    List<BeaconCacheRecord> getEventsBeingSent(Integer beaconID) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        return Collections.unmodifiableList(entry.getEventDataBeingSent());
    }

    /**
     * Get a shallow copy of actions collected so far.
     *
     * <p>
     *     Although this method is intended for debugging purposes only, it still does appropriate locking.
     * </p>
     *
     * @param beaconID The beacon id for which to retrieve the events.
     *
     * @return List of event data.
     */
    public String[] getActions(Integer beaconID) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // entry not found
            return new String[0];
        }

        try {
            entry.lock();
            return extractData(entry.getActionData());
        } finally {
            entry.unlock();
        }
    }

    /**
     * Get a shallow copy of events that are about to be sent.
     *
     * <P>
     * This method is only intended for internal unit tests.
     * </P>
     *
     * @param beaconID The beacon id for which to retrieve the events.
     *
     * @return List of event data.
     */
    List<BeaconCacheRecord> getActionsBeingSent(Integer beaconID) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        return Collections.unmodifiableList(entry.getActionDataBeingSent());
    }

    private static String[] extractData(List<BeaconCacheRecord> eventData) {
        List<String> result = new ArrayList<String>(eventData.size());
        for (BeaconCacheRecord record : eventData) {
            result.add(record.getData());
        }

        return result.toArray(new String[0]);
    }

    /**
     * Get cached {@link BeaconCacheEntry} or {@code null} if nothing exists for given {@code beaconID}.
     *
     * @param beaconID The beacon id to search for.
     *
     * @return The cached entry or {@code null}.
     */
    private BeaconCacheEntry getCachedEntry(Integer beaconID) {

        BeaconCacheEntry entry;

        // acquire read lock and get the entry
        try {
            globalCacheLock.readLock().lock();
            entry = beacons.get(beaconID);
        } finally {
            globalCacheLock.readLock().unlock();
        }

        return entry;
    }

    /**
     * Get a Set of currently inserted Beacon ids.
     *
     * <p>
     * The return value is a snapshot of currently inserted beacon ids.
     * All changes made after this call are not reflected in the returned Set.
     * </p>
     *
     * @return Snapshot of all beacon ids in the cache.
     */
    @Override
    public Set<Integer> getBeaconIDs() {

        Set<Integer> result;
        try {
            globalCacheLock.readLock().lock();
            result = new HashSet<Integer>(beacons.keySet());
        } finally {
            globalCacheLock.readLock().unlock();
        }

        return result;
    }

    /**
     * Evict {@link BeaconCacheRecord beacon cache records} by age for a given beacon.
     *
     * @param beaconID The beacon's identifier.
     * @param maxAge The maximum age allowed for beacon's records.
     */
    @Override
    public int evictRecordsByAge(Integer beaconID, long maxAge) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // already removed
            return 0;
        }

        int numRecordsRemoved;
        try {
            entry.lock();
            numRecordsRemoved = entry.removeRecordsOlderThan(maxAge);
        } finally {
            entry.unlock();
        }

        return numRecordsRemoved;
    }

    /**
     * Evict {@link BeaconCacheRecord beacon cache records} by number for given beacon.
     *
     * @param beaconID The beacon's identifier.
     * @param numRecords The maximum number of records to evict.
     */
    @Override
    public int evictRecordsByNumber(Integer beaconID, int numRecords) {

        BeaconCacheEntry entry = getCachedEntry(beaconID);
        if (entry == null) {
            // already removed
            return 0;
        }

        int numRecordsRemoved;
        try {
            entry.lock();
            numRecordsRemoved = entry.removeOldestRecords(numRecords);
        } finally {
            entry.unlock();
        }

        return numRecordsRemoved;
    }

    /**
     * Get number of bytes currently stored in cache.
     */
    @Override
    public long getNumBytesInCache() {
        return cacheStats.getNumBytesInCache();
    }

    /**
     * Call this method when something was added (size of cache increased).
     */
    private void onDataAdded() {
        setChanged();
        notifyObservers();
    }

    /**
     * Cache statistics
     */
    private static final class BeaconCacheStats {

        private long totalNumBytes = 0;

        synchronized void numBytesAdded(long numBytes) {
            totalNumBytes += numBytes;
        }

        synchronized void numBytesRemoved(long numBytes) {
            totalNumBytes -= numBytes;
        }

        synchronized long getNumBytesInCache() {
            return totalNumBytes;
        }
    }
}
