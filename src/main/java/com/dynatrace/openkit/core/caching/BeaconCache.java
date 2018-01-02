package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.protocol.Beacon;

import java.util.HashMap;
import java.util.Map;
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
public class BeaconCache {

    private final ReadWriteLock globalCacheLock;
    private final Map<Integer, BeaconCacheEntry> beacons;
    private final BeaconCacheStats cacheStats;

    public BeaconCache() {
        globalCacheLock = new ReentrantReadWriteLock();
        beacons = new HashMap<Integer, BeaconCacheEntry>();
        cacheStats = new BeaconCacheStats();
    }

    /**
     * Add event data for a given {@code beaconID} to this cache.
     *
     * @param beaconID The beacon's ID (aka Session ID) for which to add event data.
     * @param timestamp The data's timestamp.
     * @param data serialized event data to add.
     */
    public void addEventData(Integer beaconID, long timestamp, String data) {

        BeaconCacheEntry entry = getCachedEntryOrInsert(beaconID);

        // add event data for that beacon
        BeaconCacheRecord record = new BeaconCacheRecord(timestamp, data);
        entry.addEventData(record);

        // update cache stats
        cacheStats.numBytesAdded(record.getDataSizeInBytes());
    }

    /**
     * Add action data for a given {@code beaconID} to this cache.
     *
     * @param beaconID The beacon's ID (aka Session ID) for which to add action data.
     * @param timestamp The data's timestamp.
     * @param data serialized action data to add.
     */
    public void addActionData(Integer beaconID, long timestamp, String data) {

        BeaconCacheEntry entry = getCachedEntryOrInsert(beaconID);

        // add event data for that beacon
        BeaconCacheRecord record = new BeaconCacheRecord(timestamp, data);
        entry.addActionData(record);

        // update cache stats
        cacheStats.numBytesAdded(record.getDataSizeInBytes());
    }

    /**
     * Delete a cache entry for given BeaconID.
     *
     * @param beaconID The beacon's ID (aka Session ID) which to delete.
     */
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
     * Get cached {@link BeaconCacheEntry} or {@code null} if nothing exists for given {@code beaconID}.
     *
     * @param beaconID The beacon id to search for.
     *
     * @return The cached entry or {@code null}.
     */
    BeaconCacheEntry getCachedEntry(Integer beaconID) {

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
     * Cache statistics
     */
    private static final class BeaconCacheStats {

        private int totalNumBytes = 0;

        public synchronized void numBytesAdded(int numBytes) {
            totalNumBytes += numBytes;
        }

        public synchronized void numBytesRemoved(int numBytes) {
            totalNumBytes -= numBytes;
        }

        public synchronized int getNumBytesInCache() {
            return totalNumBytes;
        }
    }

}
