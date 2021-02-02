/**
 * Copyright 2018-2020 Dynatrace LLC
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

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.protocol.Beacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class used in OpenKit to cache serialized {@link Beacon} data.
 *
 * <p>
 * This cache needs to deal with high concurrency, since it's possible that a lot of threads
 * insert new data concurrently.
 *
 * Furthermore two OpenKit internal threads are also accessing the cache.
 * </p>
 */
public class BeaconCacheImpl extends Observable implements BeaconCache {

    private final Logger logger;
    private final ReadWriteLock globalCacheLock;
    private final Map<BeaconKey, BeaconCacheEntry> beacons;
    private final AtomicLong cacheSizeInBytes;

    /**
     * Create BeaconCache.
     *
     * @param logger For trace messages.
     */
    public BeaconCacheImpl(Logger logger) {
        this.logger = logger;
        globalCacheLock = new ReentrantReadWriteLock();
        beacons = new HashMap<BeaconKey, BeaconCacheEntry>();
        cacheSizeInBytes = new AtomicLong(0L);
    }


    @Override
    public void addEventData(BeaconKey key, long timestamp, String data) {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName()
                    + " addEventData(sn=" + key.beaconId + ", seq=" + key.beaconSeqNo
                    + ", timestamp=" + timestamp + ", data='" + data + "')");
        }
        // get a reference to the cache entry
        BeaconCacheEntry entry = getCachedEntryOrInsert(key);

        BeaconCacheRecord record = new BeaconCacheRecord(timestamp, data);

        try {
            // lock and add the data
            entry.lock();
            entry.addEventData(record);
        } finally {
            entry.unlock();
        }

        // update cache stats
        cacheSizeInBytes.addAndGet(record.getDataSizeInBytes());

        // notify observers
        onDataAdded();
    }

    @Override
    public void addActionData(BeaconKey key, long timestamp, String data) {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName()
                    + " addActionData(sn=" + key.beaconId + ", seq=" + key.beaconSeqNo
                    + ", timestamp=" + timestamp + ", data='" + data + "')");
        }
        BeaconCacheEntry entry = getCachedEntryOrInsert(key);

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
        cacheSizeInBytes.addAndGet(record.getDataSizeInBytes());

        // notify observers
        onDataAdded();
    }

    @Override
    public void deleteCacheEntry(BeaconKey key) {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName()
                    + " deleteCacheEntry(sn=" + key.beaconId + ", seq=" +key.beaconSeqNo+ ")");
        }
        BeaconCacheEntry entry;
        try {
            globalCacheLock.writeLock().lock();
            entry = beacons.remove(key);

        } finally {
            globalCacheLock.writeLock().unlock();
        }

        if (entry != null) {
            cacheSizeInBytes.addAndGet(-1L * entry.getTotalNumberOfBytes());
        }
    }

    @Override
    public void prepareDataForSending(BeaconKey key) {
        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // a cache entry for the given key does not exist
            return;
        }

        if (entry.needsDataCopyBeforeSending()) {
            // both entries are null, prepare data for sending
            long numBytes;
            try {
                entry.lock();
                numBytes = entry.getTotalNumberOfBytes();
                entry.copyDataForSending();

            } finally {
                entry.unlock();
            }
            // assumption: sending will work fine, and everything we copied will be removed quite soon
            cacheSizeInBytes.addAndGet(-1L * numBytes);
        }
    }

    @Override
    public boolean hasDataForSending(BeaconKey key) {
        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // a cache entry for the given key does not exist
            return false;
        }

        return entry.hasDataToSend();
    }

    @Override
    public String getNextBeaconChunk(BeaconKey key, String chunkPrefix, int maxSize, char delimiter) {

        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // a cache entry for the given key does not exist
            return null;
        }

        // data for chunking is available
        return entry.getChunk(chunkPrefix, maxSize, delimiter);
    }

    @Override
    public void removeChunkedData(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // a cache entry for the given key does not exist
            return;
        }

        entry.removeDataMarkedForSending();
    }


    @Override
    public void resetChunkedData(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // a cache entry for the given key does not exist
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

        cacheSizeInBytes.addAndGet(numBytes);

        // notify observers
        onDataAdded();
    }

    /**
     * Get cached {@link BeaconCacheEntry} or insert new one if nothing exists for given {@code key}.
     *
     * @param key The key of the beacon to search for.
     *
     * @return The already cached entry or newly created one.
     */
    private BeaconCacheEntry getCachedEntryOrInsert(BeaconKey key) {

        // get the appropriate cache entry
        BeaconCacheEntry entry = getCachedEntry(key);

        if (entry == null) {
            try {
                // does not exist, and needs to be inserted
                globalCacheLock.writeLock().lock();
                if (!beacons.containsKey(key)) {
                    // double check since this could have been added in the mean time
                    entry = new BeaconCacheEntry();
                    beacons.put(key, entry);
                } else {
                    entry = beacons.get(key);
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
     * Although this method is intended for debugging purposes only, it still does appropriate locking.
     * </p>
     *
     * @param key The key of the beacon for which to retrieve the events.
     *
     * @return List of event data.
     */
    public String[] getEvents(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
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
     * @param key The key of the beacon for which to retrieve the events.
     *
     * @return List of event data.
     */
    List<BeaconCacheRecord> getEventsBeingSent(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
        return entry.getEventDataBeingSent();
    }

    /**
     * Get a shallow copy of actions collected so far.
     *
     * <p>
     * Although this method is intended for debugging purposes only, it still does appropriate locking.
     * </p>
     *
     * @param key The key of the beacon for which to retrieve the events.
     *
     * @return List of event data.
     */
    public String[] getActions(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
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
     * @param key The key of the beacon for which to retrieve the events.
     *
     * @return List of event data.
     */
    List<BeaconCacheRecord> getActionsBeingSent(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
        return entry.getActionDataBeingSent();
    }

    private static String[] extractData(List<BeaconCacheRecord> eventData) {
        List<String> result = new ArrayList<String>(eventData.size());
        for (BeaconCacheRecord record : eventData) {
            result.add(record.getData());
        }

        return result.toArray(new String[0]);
    }

    /**
     * Get cached {@link BeaconCacheEntry} or {@code null} if nothing exists for given {@code key}.
     *
     * @param key The key of the beacon to search for.
     *
     * @return The cached entry or {@code null}.
     */
    private BeaconCacheEntry getCachedEntry(BeaconKey key) {

        BeaconCacheEntry entry;

        // acquire read lock and get the entry
        try {
            globalCacheLock.readLock().lock();
            entry = beacons.get(key);
        } finally {
            globalCacheLock.readLock().unlock();
        }

        return entry;
    }

    @Override
    public Set<BeaconKey> getBeaconKeys() {

        Set<BeaconKey> result;
        try {
            globalCacheLock.readLock().lock();
            result = new HashSet<BeaconKey>(beacons.keySet());
        } finally {
            globalCacheLock.readLock().unlock();
        }

        return result;
    }


    @Override
    public int evictRecordsByAge(BeaconKey key, long minTimestamp) {

        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // already removed
            return 0;
        }

        int numRecordsRemoved;
        try {
            entry.lock();
            numRecordsRemoved = entry.removeRecordsOlderThan(minTimestamp);
        } finally {
            entry.unlock();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName()
                    + " evictRecordsByAge(sn=" + key.beaconId + "seq=" + key.beaconSeqNo
                    + ", minTimestamp=" + minTimestamp + ") has evicted " + numRecordsRemoved + " records");
        }
        return numRecordsRemoved;
    }


    @Override
    public int evictRecordsByNumber(BeaconKey key, int numRecords) {

        BeaconCacheEntry entry = getCachedEntry(key);
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

        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName()
                    + " evictRecordsByNumber(sn=" + key.beaconId + ", seq=" + key.beaconSeqNo
                    + ", numRecords=" + numRecords + ") has evicted " + numRecordsRemoved + " records");
        }
        return numRecordsRemoved;
    }

    @Override
    public long getNumBytesInCache() {
        return cacheSizeInBytes.get();
    }

    /**
     * Call this method when something was added (size of cache increased).
     */
    private void onDataAdded() {
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean isEmpty(BeaconKey key) {

        BeaconCacheEntry entry = getCachedEntry(key);
        if (entry == null) {
            // already removed
            return true;
        }

        boolean isEmpty;
        try {
            entry.lock();
            isEmpty = entry.getTotalNumberOfBytes() == 0;
        } finally {
            entry.unlock();
        }

        return isEmpty;
    }
}
