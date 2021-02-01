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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class BeaconCacheEntryTest {

    @Test
    public void aDefaultConstructedInstanceHasNoData() {

        // given
        BeaconCacheEntry target = new BeaconCacheEntry();

        // then
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getActionData(), is(empty()));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
    }

    @Test
    public void addingActionData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "foo");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1L, "bar");

        BeaconCacheEntry target = new BeaconCacheEntry();

        // when adding first record
        target.addActionData(dataOne);

        // then
        assertThat(target.getActionData(), is(equalTo(Collections.singletonList(dataOne))));
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));

        // and when adding second record
        target.addActionData(dataTwo);

        // then
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataOne, dataTwo))));
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
    }

    @Test
    public void addingEventData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "foo");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1L, "bar");

        BeaconCacheEntry target = new BeaconCacheEntry();

        // when adding first record
        target.addEventData(dataOne);

        // then
        assertThat(target.getEventData(), is(equalTo(Collections.singletonList(dataOne))));
        assertThat(target.getActionData(), is(empty()));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));

        // and when adding second record
        target.addEventData(dataTwo);

        // then
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataOne, dataTwo))));
        assertThat(target.getActionData(), is(empty()));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
    }

    @Test
    public void copyDataForSendingMovesData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        // when copying data for later chunking
        target.copyDataForSending();

        // then the data was moved
        assertThat(target.getEventDataBeingSent(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionDataBeingSent(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getActionData(), is(empty()));
    }

    @Test
    public void needsDataCopyBeforeSendingGivesTrueBeforeDataIsCopied() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        // when, then
        assertThat(target.needsDataCopyBeforeSending(), is(true));
    }

    @Test
    public void needsDataCopyBeforeSendingGivesFalseAfterDataHasBeenCopied() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when, then
        assertThat(target.needsDataCopyBeforeSending(), is(false));
    }

    @Test
    public void needsDataCopyBeforeSendingGivesTrueIfListsAreEmpty() {

        // given
        BeaconCacheEntry target = new BeaconCacheEntry();

        target.copyDataForSending();

        // when, then
        assertThat(target.needsDataCopyBeforeSending(), is(true));

        // and all the lists are empty
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getActionData(), is(empty()));
        assertThat(target.getEventDataBeingSent(), is(empty()));
        assertThat(target.getActionDataBeingSent(), is(empty()));
    }

    @Test
    public void getChunkMarksRetrievedData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when retrieving data
        String obtained = target.getChunk("prefix", 1024, '&');

        // then
        assertThat(obtained, is("prefix&" + dataOne.getData() + "&" + dataFour.getData() + "&" + dataTwo.getData() + "&" + dataThree
            .getData()));

        // and all of them are marked
        assertThat(dataOne.isMarkedForSending(), is(true));
        assertThat(dataTwo.isMarkedForSending(), is(true));
        assertThat(dataThree.isMarkedForSending(), is(true));
        assertThat(dataFour.isMarkedForSending(), is(true));
    }

    @Test
    public void getChunkGetsChunksFromEventDataBeforeActionData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when getting data to send
        String obtained = target.getChunk("a", 2, '&');

        // then it's the first event data
        assertThat(obtained, is("a&" + dataOne.getData()));

        // and when removing already sent data and getting next chunk
        target.removeDataMarkedForSending();
        obtained = target.getChunk("a", 2, '&');

        // then it's second event data
        assertThat(obtained, is("a&" + dataFour.getData()));

        // and when removing already sent data and getting next chunk
        target.removeDataMarkedForSending();
        obtained = target.getChunk("a", 2, '&');

        // then it's the first action data
        assertThat(obtained, is("a&" + dataTwo.getData()));

        // and when removing already sent data and getting next chunk
        target.removeDataMarkedForSending();
        obtained = target.getChunk("a", 2, '&');

        // then it's the second action data
        assertThat(obtained, is("a&" + dataThree.getData()));

        // and when removing already sent data and getting next chunk
        target.removeDataMarkedForSending();
        obtained = target.getChunk("a", 2, '&');

        // then we get an empty string, since all chunks were sent & deleted
        assertThat(obtained, isEmptyString());
    }

    @Test
    public void getChunkGetsAlreadyMarkedData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when getting data to send
        String obtained = target.getChunk("a", 100, '&');

        // then
        assertThat(obtained, is("a&One&Four&Two&Three"));
        assertThat(dataOne.isMarkedForSending(), is(true));
        assertThat(dataTwo.isMarkedForSending(), is(true));
        assertThat(dataThree.isMarkedForSending(), is(true));
        assertThat(dataFour.isMarkedForSending(), is(true));

        // when getting data to send once more
        obtained = target.getChunk("a", 100, '&');

        // then
        assertThat(obtained, is("a&One&Four&Two&Three"));
        assertThat(dataOne.isMarkedForSending(), is(true));
        assertThat(dataTwo.isMarkedForSending(), is(true));
        assertThat(dataThree.isMarkedForSending(), is(true));
        assertThat(dataFour.isMarkedForSending(), is(true));
    }

    @Test
    public void getChunksTakesSizeIntoAccount() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when requesting first chunk
        String obtained = target.getChunk("prefix", 1, '&');

        // then only prefix is returned, since "prefix".length > maxSize (=1)
        assertThat(obtained, is("prefix"));

        // and when retrieving something which is one character longer than "prefix"
        obtained = target.getChunk("prefix", "prefix".length(), '&');

        // then based on the algorithm prefix and first element are retrieved
        assertThat(obtained, is("prefix&One"));

        // and when retrieving another chunk
        obtained = target.getChunk("prefix", "prefix&One".length(), '&');

        // then
        assertThat(obtained, is("prefix&One&Four"));
    }

    @Test
    public void removeDataMarkedForSendingReturnsIfDataHasNotBeenCopied() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        // when
        target.removeDataMarkedForSending();

        // then
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
    }

    @Test
    public void resetDataMarkedForSendingReturnsIfDataHasNotBeenCopied() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        // when
        target.resetDataMarkedForSending();

        // then
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
    }

    @Test
    public void resetDataMarkedForSendingMovesPreviouslyCopiedDataBack() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when data is reset
        target.resetDataMarkedForSending();

        // then
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
    }

    @Test
    public void resetDataMarkedForSendingResetsMarkedForSendingFlag() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when data is retrieved
        target.getChunk("", 1024, '&');

        // then all records are marked for sending
        assertThat(dataOne.isMarkedForSending(), is(true));
        assertThat(dataTwo.isMarkedForSending(), is(true));
        assertThat(dataThree.isMarkedForSending(), is(true));
        assertThat(dataFour.isMarkedForSending(), is(true));

        // and when
        target.resetDataMarkedForSending();

        // then
        assertThat(dataOne.isMarkedForSending(), is(false));
        assertThat(dataTwo.isMarkedForSending(), is(false));
        assertThat(dataThree.isMarkedForSending(), is(false));
        assertThat(dataFour.isMarkedForSending(), is(false));
    }

    @Test
    public void getTotalNumberOfBytesCountsAddedRecordBytes() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(0L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(0L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(1L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();

        // when getting total number of bytes on an empty entry, then
        assertThat(target.getTotalNumberOfBytes(), is(0L));

        // and when adding first entry
        target.addActionData(dataOne);

        // then
        assertThat(target.getTotalNumberOfBytes(), is(equalTo(dataOne.getDataSizeInBytes())));

        // and when adding next entry
        target.addEventData(dataTwo);

        // then
        assertThat(target.getTotalNumberOfBytes(), is(equalTo(dataOne.getDataSizeInBytes() + dataTwo.getDataSizeInBytes())));

        // and when adding next entry
        target.addEventData(dataThree);

        // then
        assertThat(target.getTotalNumberOfBytes(), is(equalTo(dataOne.getDataSizeInBytes() + dataTwo.getDataSizeInBytes() + dataThree
            .getDataSizeInBytes())));

        // and when adding next entry
        target.addActionData(dataFour);

        // then
        assertThat(target.getTotalNumberOfBytes(), is(equalTo(dataOne.getDataSizeInBytes() + dataTwo.getDataSizeInBytes() + dataThree
            .getDataSizeInBytes() + dataFour.getDataSizeInBytes())));
    }

    @Test
    public void removeRecordsOlderThanRemovesNothingIfNoActionOrEventDataExists() {

        // given
        BeaconCacheEntry target = new BeaconCacheEntry();

        // when
        int obtained = target.removeRecordsOlderThan(0);

        // then
        assertThat(obtained, is(0));
    }

    @Test
    public void removeRecordsOlderThanRemovesRecordsFromActionData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(4000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(3000L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1000L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addActionData(dataOne);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);
        target.addActionData(dataFour);

        // when removing everything older than 3000
        int obtained = target.removeRecordsOlderThan(dataTwo.getTimestamp());

        // then
        assertThat(obtained, is(2)); // two were removed
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataOne, dataTwo))));
    }

    @Test
    public void removeRecordsOlderThanRemovesRecordsFromEventData() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(4000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(3000L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1000L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataTwo);
        target.addEventData(dataThree);
        target.addEventData(dataFour);

        // when removing everything older than 3000
        int obtained = target.removeRecordsOlderThan(dataTwo.getTimestamp());

        // then
        assertThat(obtained, is(2)); // two were removed
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataOne, dataTwo))));
    }

    @Test
    public void removeOldestRecordsRemovesNothingIfEntryIsEmpty() {

        // given
        BeaconCacheEntry target = new BeaconCacheEntry();

        // when
        int obtained = target.removeOldestRecords(1);

        // then
        assertThat(obtained, is(equalTo(0)));
    }

    @Test
    public void removeOldestRecordsRemovesActionDataIfEventDataIsEmpty() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(4000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(3000L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1000L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addActionData(dataOne);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);
        target.addActionData(dataFour);

        // when
        int obtained = target.removeOldestRecords(2);

        // then
        assertThat(obtained, is(2)); // two were removed
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataThree, dataFour))));
    }

    @Test
    public void removeOldestRecordsRemovesEventDataIfActionDataIsEmpty() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(4000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(3000L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1000L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataTwo);
        target.addEventData(dataThree);
        target.addEventData(dataFour);

        // when
        int obtained = target.removeOldestRecords(2);

        // then
        assertThat(obtained, is(2)); // two were removed
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataThree, dataFour))));
    }

    @Test
    public void removeOldestRecordsComparesTopActionAndEventDataAndRemovesOldest() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(1000, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1100L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(950L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1200L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);
        target.addEventData(dataFour);

        // when
        int obtained = target.removeOldestRecords(1);

        // then
        assertThat(obtained, is(1));
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventData(), is(equalTo(Collections.singletonList(dataFour))));

        // when removing the next two
        obtained = target.removeOldestRecords(2);

        // then
        assertThat(obtained, is(2));
        assertThat(target.getActionData(), is(empty()));
        assertThat(target.getEventData(), is(equalTo(Collections.singletonList(dataFour))));
    }

    @Test
    public void removeOldestRecordsRemovesEventDataIfTopEventDataAndActionDataHaveSameTimestamp() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(1000, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1100L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(dataOne.getTimestamp(), "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(dataTwo.getTimestamp(), "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataTwo);
        target.addActionData(dataThree);
        target.addActionData(dataFour);

        // when
        int obtained = target.removeOldestRecords(1);

        // then
        assertThat(obtained, is(1));
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataThree, dataFour))));
        assertThat(target.getEventData(), is(equalTo(Collections.singletonList(dataTwo))));
    }

    @Test
    public void removeOldestRecordsStopsIfListsAreEmpty() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(4000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(3000L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(1000L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataTwo);
        target.addEventData(dataThree);
        target.addEventData(dataFour);

        // when
        int obtained = target.removeOldestRecords(100);

        // then
        assertThat(obtained, is(4));
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getActionData(), is(empty()));
    }

    @Test
    public void removeRecordsOlderThanDoesNotRemoveAnythingFromEventAndActionsBeingSent() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(1000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1500L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(2500L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when
        int obtained = target.removeRecordsOlderThan(10000);

        // then
        assertThat(obtained, is(0));
        assertThat(target.getEventDataBeingSent(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionDataBeingSent(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
    }


    @Test
    public void removeOldestRecordsDoesNotRemoveAnythingFromEventAndActionsBeingSent() {

        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(1000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1500L, "Two");
        BeaconCacheRecord dataThree = new BeaconCacheRecord(2000L, "Three");
        BeaconCacheRecord dataFour = new BeaconCacheRecord(2500L, "Four");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataFour);
        target.addActionData(dataTwo);
        target.addActionData(dataThree);

        target.copyDataForSending();

        // when
        int obtained = target.removeOldestRecords(10000);

        // then
        assertThat(obtained, is(0));
        assertThat(target.getEventDataBeingSent(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionDataBeingSent(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
    }

    @Test
    public void hasDataForSendingReturnsFalseIfDataWasNotCopied() {
        // given
        BeaconCacheRecord dataOne = new BeaconCacheRecord(1000L, "One");
        BeaconCacheRecord dataTwo = new BeaconCacheRecord(1500L, "Two");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(dataOne);
        target.addEventData(dataTwo);

        // when
        boolean obtained = target.hasDataToSend();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void hasDataForSendingReturnsFalseIfNoDataWasAddedBeforeCopying() {
        // given
        BeaconCacheEntry target = new BeaconCacheEntry();
        target.copyDataForSending();

        // when
        boolean obtained = target.hasDataToSend();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void hasDataForSendingReturnsTrueIfEventDataWasAddedBeforeCopying() {
        // given
        BeaconCacheRecord record = new BeaconCacheRecord(1000L, "One");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addEventData(record);

        target.copyDataForSending();

        // when
        boolean obtained = target.hasDataToSend();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void hasDataForSendingReturnsTrueIfActionDataWasAddedBeforeCopying() {
        // given
        BeaconCacheRecord record = new BeaconCacheRecord(1000L, "One");

        BeaconCacheEntry target = new BeaconCacheEntry();
        target.addActionData(record);

        target.copyDataForSending();

        // when
        boolean obtained = target.hasDataToSend();

        // then
        assertThat(obtained, is(true));
    }
}
