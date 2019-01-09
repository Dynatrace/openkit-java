/**
 * Copyright 2018-2019 Dynatrace LLC
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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconCacheImplTest {

    private Observer observer;

    @Before
    public void setUp() {
        observer = mock(Observer.class);
    }

    @Test
    public void aDefaultConstructedCacheDoesNotContainBeacons() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // then
        assertThat(target.getBeaconIDs(), is(empty()));
        assertThat(target.getNumBytesInCache(), is(0L));
    }

    @Test
    public void addEventDataAddsBeaconIdToCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // when adding beacon with id 1
        target.addEventData(1, 1000L, "a");

        // then
        assertThat(target.getBeaconIDs(), is(Collections.singleton(1)));
        assertThat(target.getEvents(1), is(equalTo(new String[]{"a"})));

        // and when adding beacon with id 2
        target.addEventData(2, 1100L, "b");

        // then
        assertThat(target.getBeaconIDs(), containsInAnyOrder(1, 2));
        assertThat(target.getEvents(1), is(equalTo(new String[]{"a"})));
        assertThat(target.getEvents(2), is(equalTo(new String[]{"b"})));
    }

    @Test
    public void addEventDataAddsDataToAlreadyExistingBeaconId() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // when adding beacon with id 1
        target.addEventData(1, 1000L, "a");

        // then
        assertThat(target.getBeaconIDs(), is(Collections.singleton(1)));
        assertThat(target.getEvents(1), is(equalTo(new String[]{"a"})));

        // and when adding other data with beacon id 1
        target.addEventData(1, 1100L, "bc");

        // then
        assertThat(target.getBeaconIDs(), is(Collections.singleton(1)));
        assertThat(target.getEvents(1), is(equalTo(new String[]{"a", "bc"})));
    }

    @Test
    public void addEventDataIncreasesCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // when adding some data
        target.addEventData(1, 1000L, "a");
        target.addEventData(42, 1000L, "z");
        target.addEventData(1, 1000L, "iii");

        // then
        assertThat(target.getNumBytesInCache(), is(new BeaconCacheRecord(1000L, "a").getDataSizeInBytes() + new BeaconCacheRecord(1000L, "z")
            .getDataSizeInBytes() + new BeaconCacheRecord(1000L, "iii").getDataSizeInBytes()));
    }

    @Test
    public void addEventDataNotifiesObserver() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        target.addObserver(observer);

        // when adding an element
        target.addEventData(1, 1000L, "a");

        // then verify observer got notified
        verify(observer, times(1)).update(target, null);

        // when adding some more data
        target.addEventData(1, 1100L, "b");
        target.addEventData(666, 1200L, "xyz");

        // then verify observer got notified another two times
        verify(observer, times(3)).update(target, null);
    }

    @Test
    public void addActionDataAddsBeaconIdToCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // when adding beacon with id 1
        target.addActionData(1, 1000L, "a");

        // then
        assertThat(target.getBeaconIDs(), is(Collections.singleton(1)));
        assertThat(target.getActions(1), is(equalTo(new String[]{"a"})));

        // and when adding beacon with id 2
        target.addActionData(2, 1100L, "b");

        // then
        assertThat(target.getBeaconIDs(), containsInAnyOrder(1, 2));
        assertThat(target.getActions(1), is(equalTo(new String[]{"a"})));
        assertThat(target.getActions(2), is(equalTo(new String[]{"b"})));
    }

    @Test
    public void addActionDataAddsDataToAlreadyExistingBeaconId() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // when adding beacon with id 1
        target.addActionData(1, 1000L, "a");

        // then
        assertThat(target.getBeaconIDs(), is(Collections.singleton(1)));
        assertThat(target.getActions(1), is(equalTo(new String[]{"a"})));

        // and when adding other data with beacon id 1
        target.addActionData(1, 1100L, "bc");

        // then
        assertThat(target.getBeaconIDs(), is(Collections.singleton(1)));
        assertThat(target.getActions(1), is(equalTo(new String[]{"a", "bc"})));
    }

    @Test
    public void addActionDataIncreasesCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        // when adding some data
        target.addActionData(1, 1000L, "a");
        target.addActionData(42, 1000L, "z");
        target.addActionData(1, 1000L, "iii");

        // then
        assertThat(target.getNumBytesInCache(), is(new BeaconCacheRecord(1000L, "a").getDataSizeInBytes() + new BeaconCacheRecord(1000L, "z")
            .getDataSizeInBytes() + new BeaconCacheRecord(1000L, "iii").getDataSizeInBytes()));
    }

    @Test
    public void addActionDataNotifiesObserver() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();

        target.addObserver(observer);

        // when adding an element
        target.addActionData(1, 1000L, "a");

        // then verify observer got notified
        verify(observer, times(1)).update(target, null);

        // when adding some more data
        target.addActionData(1, 1100L, "b");
        target.addActionData(666, 1200L, "xyz");

        // then verify observer got notified another two times
        verify(observer, times(3)).update(target, null);
    }

    @Test
    public void deleteCacheEntryRemovesTheGivenBeacon() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(42, 1000L, "z");
        target.addEventData(1, 1000L, "iii");

        // when removing beacon with id 1
        target.deleteCacheEntry(1);

        // then
        assertThat(target.getBeaconIDs(), is(contains(42)));

        // and when removing beacon with id 42
        target.deleteCacheEntry(42);

        // then
        assertThat(target.getBeaconIDs(), is(empty()));
    }

    @Test
    public void deleteCacheEntryDecrementsCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(42, 1000L, "z");
        target.addEventData(1, 1000L, "iii");

        // when deleting entry with beacon id 42
        target.deleteCacheEntry(42);

        // then
        assertThat(target.getNumBytesInCache(), is(equalTo(new BeaconCacheRecord(1000L, "a").getDataSizeInBytes() + new BeaconCacheRecord(1000L, "iii")
            .getDataSizeInBytes())));
    }

    @Test
    public void deleteCacheEntryDoesNotNotifyObservers() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(42, 1000L, "z");
        target.addEventData(1, 1000L, "iii");

        target.addObserver(observer);

        // when deleting both entries
        target.deleteCacheEntry(1);
        target.deleteCacheEntry(42);

        // then
        verifyZeroInteractions(observer);
    }

    @Test
    public void deleteCacheEntriesDoesNothingIfGivenBeaconIDIsNotInCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(42, 1000L, "z");
        target.addEventData(1, 1000L, "iii");

        target.addObserver(observer);

        long cachedSize = target.getNumBytesInCache();

        // when
        target.deleteCacheEntry(666);

        // then
        assertThat(target.getBeaconIDs(), containsInAnyOrder(1, 42));
        assertThat(target.getNumBytesInCache(), is(equalTo(cachedSize)));

        verifyZeroInteractions(observer);
    }

    @Test
    public void getNextBeaconChunkReturnsNullIfGivenBeaconIDDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(42, 1000L, "z");
        target.addEventData(1, 1000L, "iii");

        // when
        String obtained = target.getNextBeaconChunk(666, "", 1024, '&');

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void getNextBeaconChunkCopiesDataForSending() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addActionData(42, 2000L, "z");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when
        String obtained = target.getNextBeaconChunk(1, "prefix", 0, '&');

        // then
        assertThat(obtained, is("prefix"));

        assertThat(target.getActions(1), is(emptyArray()));
        assertThat(target.getEvents(1), is(emptyArray()));
        assertThat(target.getActionsBeingSent(1), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        assertThat(target.getEventsBeingSent(1), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "b"), new BeaconCacheRecord(1001L, "jjj")))));
    }

    @Test
    public void getNextBeaconChunkDecreasesBeaconCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addActionData(42, 2000L, "z");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when
        target.getNextBeaconChunk(1, "prefix", 0, '&');

        // cache stats are also adjusted
        assertThat(target.getNumBytesInCache(), is(new BeaconCacheRecord(2000L, "z").getDataSizeInBytes()));
    }

    @Test
    public void getNextBeaconChunkRetrievesNextChunk() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addActionData(42, 2000L, "z");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when retrieving the first chunk
        String obtained = target.getNextBeaconChunk(1, "prefix", 10, '&');

        // then
        assertThat(obtained, is("prefix&b&jjj"));

        // then
        assertThat(target.getActionsBeingSent(1), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        List<BeaconCacheRecord> expectedEventRecords = Arrays.asList(new BeaconCacheRecord(1000L, "b"), new BeaconCacheRecord(1001L, "jjj"));
        for (BeaconCacheRecord record : expectedEventRecords) {
            record.markForSending();
        }
        assertThat(target.getEventsBeingSent(1), is(equalTo(expectedEventRecords)));
    }

    @Test
    public void removeChunkedDataClearsAlreadyRetrievedChunks() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addActionData(42, 2000L, "z");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when retrieving the first chunk and removing retrieved chunks
        String obtained = target.getNextBeaconChunk(1, "prefix", 10, '&');
        target.removeChunkedData(1);

        // then
        assertThat(obtained, is("prefix&b&jjj"));

        assertThat(target.getActionsBeingSent(1), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        assertThat(target.getEventsBeingSent(1), is(empty()));

        // when retrieving the second chunk and removing retrieved chunks
        obtained = target.getNextBeaconChunk(1, "prefix", 10, '&');
        target.removeChunkedData(1);

        // then
        assertThat(obtained, is("prefix&a&iii"));

        assertThat(target.getActionsBeingSent(1), is(empty()));
        assertThat(target.getEventsBeingSent(1), is(empty()));
    }

    @Test
    public void removeChunkedDataDoesNothingIfCalledWithNonExistingBeaconID() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addActionData(42, 2000L, "z");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when retrieving the first chunk and removing the wrong beacon chunk
        target.getNextBeaconChunk(1, "prefix", 10, '&');
        target.removeChunkedData(2);

        // then
        assertThat(target.getActionsBeingSent(1), is(equalTo(Arrays.asList(new BeaconCacheRecord(1000L, "a"), new BeaconCacheRecord(1001L, "iii")))));
        List<BeaconCacheRecord> expectedEventRecords = Arrays.asList(new BeaconCacheRecord(1000L, "b"), new BeaconCacheRecord(1001L, "jjj"));
        for (BeaconCacheRecord record : expectedEventRecords) {
            record.markForSending();
        }
        assertThat(target.getEventsBeingSent(1), is(equalTo(expectedEventRecords)));
    }

    @Test
    public void resetChunkedRestoresData() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(1, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(1, 6666L, "123");
        target.addEventData(1, 6666L, "987");

        // and when resetting the previously copied data
        target.resetChunkedData(1);

        // then
        assertThat(target.getActionsBeingSent(1), is(nullValue()));
        assertThat(target.getEventsBeingSent(1), is(nullValue()));
        assertThat(target.getActions(1), is(equalTo(new String[]{"a", "iii", "123"})));
        assertThat(target.getEvents(1), is(equalTo(new String[]{"b", "jjj", "987"})));
    }

    @Test
    public void resetChunkedRestoresCacheSize() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(1, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(1, 6666L, "123");
        target.addEventData(1, 6666L, "987");

        // and when resetting the previously copied data
        target.resetChunkedData(1);

        // then
        assertThat(target.getNumBytesInCache(), is(28L));
    }

    @Test
    public void resetChunkedNotifiesObservers() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(1, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(1, 6666L, "123");
        target.addEventData(1, 6666L, "987");

        target.addObserver(observer);

        // and when resetting the previously copied data
        target.resetChunkedData(1);

        // then
        verify(observer, times(1)).update(target, null);
    }

    @Test
    public void resetChunkedDoesNothingIfEntryDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // do same step we'd do when we send the
        target.getNextBeaconChunk(1, "prefix", 10, '&');

        // data has been copied, but still add some new event & action data
        target.addActionData(1, 6666L, "123");
        target.addEventData(1, 6666L, "987");

        target.addObserver(observer);

        // and when resetting the previously copied data
        target.resetChunkedData(666);

        // then
        assertThat(target.getNumBytesInCache(), is(12L));
        verifyZeroInteractions(observer);
    }

    @Test
    public void evictRecordsByAgeDoesNothingAndReturnsZeroIfBeaconIDDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByAge(666, 0);

        // then
        assertThat(obtained, is(0));
    }

    @Test
    public void evictRecordsByAge() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByAge(1, 1001);

        // then
        assertThat(obtained, is(2));
    }

    @Test
    public void evictRecordsByNumberDoesNothingAndReturnsZeroIfBeaconIDDoesNotExist() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByNumber(666, 100);

        // then
        assertThat(obtained, is(0));
    }

    @Test
    public void evictRecordsByNumber() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // when
        int obtained = target.evictRecordsByNumber(1, 2);

        // then
        assertThat(obtained, is(2));
    }

    @Test
    public void isEmptyGivesTrueIfBeaconDoesNotExistInCache() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addActionData(1, 1001L, "iii");
        target.addEventData(1, 1000L, "b");
        target.addEventData(1, 1001L, "jjj");

        // then
        assertThat(target.isEmpty(666), is(true));
    }

    @Test
    public void isEmptyGivesFalseIfBeaconDataSizeIsNotEqualToZero() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addEventData(1, 1000L, "b");

        // then
        assertThat(target.isEmpty(1), is(false));
    }

    @Test
    public void isEmptyGivesTrueIfBeaconDoesNotContainActiveData() {

        // given
        BeaconCacheImpl target = new BeaconCacheImpl();
        target.addActionData(1, 1000L, "a");
        target.addEventData(1, 1000L, "b");

        target.getNextBeaconChunk(1, "prefix", 0, '&');

        // then
        assertThat(target.isEmpty(1), is(true));
    }
}
