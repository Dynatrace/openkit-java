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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class BeaconCacheImplTest {

    private Logger logger;

    private Observer observer;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
        observer = mock(Observer.class);
    }

    @Test
    public void aDefaultConstructedCacheDoesNotContainBeacons() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);

        // then
        assertThat(target.getBeaconKeys(), is(empty()));
        assertThat(target.getNumBytesInCache(), is(0L));
    }

    @Test
    public void addEventDataAddsBeaconKeyToCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(2, 0);

        // when adding beacon with id 1
        target.addEventData(keyOne, 1000L, "a");

        // then
        assertThat(target.getBeaconKeys(), is(Collections.singleton(keyOne)));
        assertThat(target.getEvents(keyOne), is(equalTo(new String[]{"a"})));

        // and when adding beacon with id 2
        target.addEventData(keyTwo, 1100L, "b");

        // then
        assertThat(target.getBeaconKeys(), containsInAnyOrder(keyOne, keyTwo));
        assertThat(target.getEvents(keyOne), is(equalTo(new String[]{"a"})));
        assertThat(target.getEvents(keyTwo), is(equalTo(new String[]{"b"})));
    }

    @Test
    public void addEventDataAddsDataToAlreadyExistingBeaconId() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        // when adding beacon with id 1
        target.addEventData(key, 1000L, "a");

        // then
        assertThat(target.getBeaconKeys(), is(Collections.singleton(key)));
        assertThat(target.getEvents(key), is(equalTo(new String[]{"a"})));

        // and when adding other data with beacon id 1
        target.addEventData(key, 1100L, "bc");

        // then
        assertThat(target.getBeaconKeys(), is(Collections.singleton(key)));
        assertThat(target.getEvents(key), is(equalTo(new String[]{"a", "bc"})));
    }

    @Test
    public void addEventDataIncreasesCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        // when adding some data
        target.addEventData(keyOne, 1000L, "a");
        target.addEventData(keyTwo, 1000L, "z");
        target.addEventData(keyOne, 1000L, "iii");

        // then
        assertThat(target.getNumBytesInCache(), is(new BeaconCacheRecord(1000L, "a").getDataSizeInBytes() + new BeaconCacheRecord(1000L, "z")
            .getDataSizeInBytes() + new BeaconCacheRecord(1000L, "iii").getDataSizeInBytes()));
    }

    @Test
    public void addEventDataNotifiesObserver() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(666, 0);

        target.addObserver(observer);

        // when adding an element
        target.addEventData(keyOne, 1000L, "a");

        // then verify observer got notified
        verify(observer, times(1)).update(target, null);

        // when adding some more data
        target.addEventData(keyOne, 1100L, "b");
        target.addEventData(keyTwo, 1200L, "xyz");

        // then verify observer got notified another two times
        verify(observer, times(3)).update(target, null);
    }

    @Test
    public void addActionDataAddsBeaconIdToCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(2, 0);

        // when adding beacon with id 1
        target.addActionData(keyOne, 1000L, "a");

        // then
        assertThat(target.getBeaconKeys(), is(Collections.singleton(keyOne)));
        assertThat(target.getActions(keyOne), is(equalTo(new String[]{"a"})));

        // and when adding beacon with id 2
        target.addActionData(keyTwo, 1100L, "b");

        // then
        assertThat(target.getBeaconKeys(), containsInAnyOrder(keyOne, keyTwo));
        assertThat(target.getActions(keyOne), is(equalTo(new String[]{"a"})));
        assertThat(target.getActions(keyTwo), is(equalTo(new String[]{"b"})));
    }

    @Test
    public void addActionDataAddsDataToAlreadyExistingBeaconId() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        // when adding beacon with id 1
        target.addActionData(key, 1000L, "a");

        // then
        assertThat(target.getBeaconKeys(), is(Collections.singleton(key)));
        assertThat(target.getActions(key), is(equalTo(new String[]{"a"})));

        // and when adding other data with beacon id 1
        target.addActionData(key, 1100L, "bc");

        // then
        assertThat(target.getBeaconKeys(), is(Collections.singleton(key)));
        assertThat(target.getActions(key), is(equalTo(new String[]{"a", "bc"})));
    }

    @Test
    public void addActionDataIncreasesCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        // when adding some data
        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyTwo, 1000L, "z");
        target.addActionData(keyOne, 1000L, "iii");

        // then
        assertThat(target.getNumBytesInCache(), is(new BeaconCacheRecord(1000L, "a").getDataSizeInBytes() + new BeaconCacheRecord(1000L, "z")
            .getDataSizeInBytes() + new BeaconCacheRecord(1000L, "iii").getDataSizeInBytes()));
    }

    @Test
    public void addActionDataNotifiesObserver() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(666, 0);

        target.addObserver(observer);

        // when adding an element
        target.addActionData(keyOne, 1000L, "a");

        // then verify observer got notified
        verify(observer, times(1)).update(target, null);

        // when adding some more data
        target.addActionData(keyOne, 1100L, "b");
        target.addActionData(keyTwo, 1200L, "xyz");

        // then verify observer got notified another two times
        verify(observer, times(3)).update(target, null);
    }

    @Test
    public void deleteCacheEntryRemovesTheGivenBeacon() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyTwo, 1000L, "z");
        target.addEventData(keyOne, 1000L, "iii");

        // when removing beacon with id 1
        target.deleteCacheEntry(keyOne);

        // then
        assertThat(target.getBeaconKeys(), is(contains(keyTwo)));

        // and when removing beacon with id 42
        target.deleteCacheEntry(keyTwo);

        // then
        assertThat(target.getBeaconKeys(), is(empty()));
    }

    @Test
    public void deleteCacheEntryDecrementsCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);
        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyTwo, 1000L, "z");
        target.addEventData(keyOne, 1000L, "iii");

        // when deleting entry with beacon id 42
        target.deleteCacheEntry(keyTwo);

        // then
        assertThat(target.getNumBytesInCache(), is(equalTo(new BeaconCacheRecord(1000L, "a").getDataSizeInBytes() + new BeaconCacheRecord(1000L, "iii")
            .getDataSizeInBytes())));
    }

    @Test
    public void deleteCacheEntryDoesNotNotifyObservers() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyTwo, 1000L, "z");
        target.addEventData(keyOne, 1000L, "iii");

        target.addObserver(observer);

        // when deleting both entries
        target.deleteCacheEntry(keyOne);
        target.deleteCacheEntry(keyTwo);

        // then
        verifyZeroInteractions(observer);
    }

    @Test
    public void deleteCacheEntriesDoesNothingIfGivenBeaconIDIsNotInCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);
        BeaconKey keyThree = new BeaconKey(666, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyTwo, 1000L, "z");
        target.addEventData(keyOne, 1000L, "iii");

        target.addObserver(observer);

        long cachedSize = target.getNumBytesInCache();

        // when
        target.deleteCacheEntry(keyThree);

        // then
        assertThat(target.getBeaconKeys(), containsInAnyOrder(keyOne, keyTwo));
        assertThat(target.getNumBytesInCache(), is(equalTo(cachedSize)));

        verifyZeroInteractions(observer);
    }

    @Test
    public void getNextBeaconChunkReturnsNullIfGivenBeaconIDDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyTwo, 1000L, "z");
        target.addEventData(keyOne, 1000L, "iii");

        // when
        String obtained = target.getNextBeaconChunk(new BeaconKey(666, 0), "", 1024, '&');

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void getNextBeaconChunkCopiesDataForSending() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyOne, 1001L, "iii");
        target.addActionData(keyTwo, 2000L, "z");
        target.addEventData(keyOne, 1000L, "b");
        target.addEventData(keyOne, 1001L, "jjj");

        // when
        String obtained = target.getNextBeaconChunk(keyOne, "prefix", 0, '&');

        // then
        assertThat(obtained, is("prefix"));

        assertThat(target.getActions(keyOne), is(emptyArray()));
        assertThat(target.getEvents(keyOne), is(emptyArray()));
        assertThat(target.getActionsBeingSent(keyOne), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        assertThat(target.getEventsBeingSent(keyOne), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "b"), new BeaconCacheRecord(1001L, "jjj")))));
    }

    @Test
    public void getNextBeaconChunkDecreasesBeaconCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyOne, 1001L, "iii");
        target.addActionData(keyTwo, 2000L, "z");
        target.addEventData(keyOne, 1000L, "b");
        target.addEventData(keyOne, 1001L, "jjj");

        // when
        target.getNextBeaconChunk(keyOne, "prefix", 0, '&');

        // cache stats are also adjusted
        assertThat(target.getNumBytesInCache(), is(new BeaconCacheRecord(2000L, "z").getDataSizeInBytes()));
    }

    @Test
    public void getNextBeaconChunkRetrievesNextChunk() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyOne, 1001L, "iii");
        target.addActionData(keyTwo, 2000L, "z");
        target.addEventData(keyOne, 1000L, "b");
        target.addEventData(keyOne, 1001L, "jjj");

        // when retrieving the first chunk
        String obtained = target.getNextBeaconChunk(keyOne, "prefix", 10, '&');

        // then
        assertThat(obtained, is("prefix&b&jjj"));

        // then
        assertThat(target.getActionsBeingSent(keyOne), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        List<BeaconCacheRecord> expectedEventRecords = Arrays.asList(new BeaconCacheRecord(1000L, "b"), new BeaconCacheRecord(1001L, "jjj"));
        for (BeaconCacheRecord record : expectedEventRecords) {
            record.markForSending();
        }
        assertThat(target.getEventsBeingSent(keyOne), is(equalTo(expectedEventRecords)));
    }

    @Test
    public void removeChunkedDataClearsAlreadyRetrievedChunks() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyOne, 1001L, "iii");
        target.addActionData(keyTwo, 2000L, "z");
        target.addEventData(keyOne, 1000L, "b");
        target.addEventData(keyOne, 1001L, "jjj");

        // when retrieving the first chunk and removing retrieved chunks
        String obtained = target.getNextBeaconChunk(keyOne, "prefix", 10, '&');
        target.removeChunkedData(keyOne);

        // then
        assertThat(obtained, is("prefix&b&jjj"));

        assertThat(target.getActionsBeingSent(keyOne), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        assertThat(target.getEventsBeingSent(keyOne), is(empty()));

        // when retrieving the second chunk and removing retrieved chunks
        obtained = target.getNextBeaconChunk(keyOne, "prefix", 10, '&');
        target.removeChunkedData(keyOne);

        // then
        assertThat(obtained, is("prefix&a&iii"));

        assertThat(target.getActionsBeingSent(keyOne), is(empty()));
        assertThat(target.getEventsBeingSent(keyOne), is(empty()));
    }

    @Test
    public void removeChunkedDataDoesNothingIfCalledWithNonExistingBeaconID() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey keyOne = new BeaconKey(1, 0);
        BeaconKey keyTwo = new BeaconKey(42, 0);

        target.addActionData(keyOne, 1000L, "a");
        target.addActionData(keyOne, 1001L, "iii");
        target.addActionData(keyTwo, 2000L, "z");
        target.addEventData(keyOne, 1000L, "b");
        target.addEventData(keyOne, 1001L, "jjj");

        // when retrieving the first chunk and removing the wrong beacon chunk
        target.getNextBeaconChunk(keyOne, "prefix", 10, '&');
        target.removeChunkedData(keyTwo);

        // then
        assertThat(target.getActionsBeingSent(keyOne), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        List<BeaconCacheRecord> expectedEventRecords = Arrays.asList(new BeaconCacheRecord(1000L, "b"), new BeaconCacheRecord(1001L, "jjj"));
        for (BeaconCacheRecord record : expectedEventRecords) {
            record.markForSending();
        }
        assertThat(target.getEventsBeingSent(keyOne), is(equalTo(expectedEventRecords)));
    }

    @Test
    public void resetChunkedRestoresData() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(key, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(key, 6666L, "123");
        target.addEventData(key, 6666L, "987");

        // and when resetting the previously copied data
        target.resetChunkedData(key);

        // then
        assertThat(target.getActionsBeingSent(key), is(nullValue()));
        assertThat(target.getEventsBeingSent(key), is(nullValue()));
        assertThat(target.getActions(key), is(equalTo(new String[]{"a", "iii", "123"})));
        assertThat(target.getEvents(key), is(equalTo(new String[]{"b", "jjj", "987"})));
    }

    @Test
    public void resetChunkedRestoresCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(key, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(key, 6666L, "123");
        target.addEventData(key, 6666L, "987");

        // and when resetting the previously copied data
        target.resetChunkedData(key);

        // then
        assertThat(target.getNumBytesInCache(), is(28L));
    }

    @Test
    public void resetChunkedNotifiesObservers() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(key, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(key, 6666L, "123");
        target.addEventData(key, 6666L, "987");

        target.addObserver(observer);

        // and when resetting the previously copied data
        target.resetChunkedData(key);

        // then
        verify(observer, times(1)).update(target, null);
    }

    @Test
    public void resetChunkedDoesNothingIfEntryDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(key, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(key, 6666L, "123");
        target.addEventData(key, 6666L, "987");

        target.addObserver(observer);

        // and when resetting the previously copied data
        target.resetChunkedData(new BeaconKey(666, 0));

        // then
        assertThat(target.getNumBytesInCache(), is(12L));
        verifyZeroInteractions(observer);
    }

    @Test
    public void evictRecordsByAgeDoesNothingAndReturnsZeroIfBeaconIDDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByAge(new BeaconKey(666, 0), 0);

        // then
        assertThat(obtained, is(0));
    }

    @Test
    public void evictRecordsByAge() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByAge(key, 1001);

        // then
        assertThat(obtained, is(2));
    }

    @Test
    public void evictRecordsByNumberDoesNothingAndReturnsZeroIfBeaconIDDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByNumber(new BeaconKey(666, 0), 100);

        // then
        assertThat(obtained, is(0));
    }

    @Test
    public void evictRecordsByNumber() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByNumber(key, 2);

        // then
        assertThat(obtained, is(2));
    }

    @Test
    public void isEmptyGivesTrueIfBeaconDoesNotExistInCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addActionData(key, 1001L, "iii");
        target.addEventData(key, 1000L, "b");
        target.addEventData(key, 1001L, "jjj");

        // then
        assertThat(target.isEmpty(new BeaconKey(666, 0)), is(true));
    }

    @Test
    public void isEmptyGivesFalseIfBeaconDataSizeIsNotEqualToZero() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addEventData(key, 1000L, "b");

        // then
        assertThat(target.isEmpty(key), is(false));
    }

    @Test
    public void isEmptyGivesTrueIfBeaconDoesNotContainActiveData() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl(logger);
        BeaconKey key = new BeaconKey(1, 0);

        target.addActionData(key, 1000L, "a");
        target.addEventData(key, 1000L, "b");

        target.getNextBeaconChunk(key, "prefix", 0, '&');

        // then
        assertThat(target.isEmpty(key), is(true));
    }
}
