package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.protocol.Beacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        try {
            // lock and add the data
            entry.lock();
            entry.addActionData(record);
        } finally {
            entry.unlock();
        }

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

    /**
     * Get the next chunk for sending to the backend.
     *
     * <p>
     *     Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param beaconID The beacon id for which to get the next chunk.
     * @param chunkPrefix Prefix to append to the beginning of the chunk.
     * @param maxSize Maximum chunk size. As soon as chunk's size >= maxSize result is returned.
     * @param delimiter Delimiter between consecutive chunks.
     *
     * @return {@code null} if given {@code beaconID} does not exist, an empty string, if there is no more data to send
     * or the next chunk to send.
     */
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
     *     Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param beaconID The beacon id for which to remove already chunked data.
     */
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
