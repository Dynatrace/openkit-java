package com.dynatrace.openkit.core.caching;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    public void copyDataForChunkingMovesData() {

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
        target.copyDataForChunking();

        // then the data was moved
        assertThat(target.getEventDataBeingSent(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionDataBeingSent(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventData(), is(empty()));
        assertThat(target.getActionData(), is(empty()));
    }

    @Test
    public void needsDataCopyBeforeChunkingGivesTrueBeforeDataIsCopied() {

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
        assertThat(target.needsDataCopyBeforeChunking(), is(true));
    }

    @Test
    public void needsDataCopyBeforeChunkingGivesFalseAfterDataHasBeenCopied() {

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

        target.copyDataForChunking();

        // when, then
        assertThat(target.needsDataCopyBeforeChunking(), is(false));
    }

    @Test
    public void needsDataCopyBeforeChunkingGivesFalseEvenIfListsAreEmpty() {

        // given
        BeaconCacheEntry target = new BeaconCacheEntry();

        target.copyDataForChunking();

        // when, then
        assertThat(target.needsDataCopyBeforeChunking(), is(false));

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

        target.copyDataForChunking();

        // when retrieving data
        String obtained = target.getChunk("prefix", 1024, '&');

        // then
        assertThat(obtained, is("prefix&" + dataOne.getData() + "&" + dataFour.getData() + "&" + dataTwo.getData() + "&" + dataThree.getData()));

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

        target.copyDataForChunking();

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

        target.copyDataForChunking();

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

        target.copyDataForChunking();

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

        target.copyDataForChunking();

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
        assertThat(target.getEventData(), is(equalTo(Arrays.asList(dataOne, dataFour))));
        assertThat(target.getActionData(), is(equalTo(Arrays.asList(dataTwo, dataThree))));
        assertThat(target.getEventDataBeingSent(), is(nullValue()));
        assertThat(target.getActionDataBeingSent(), is(nullValue()));
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
        assertThat(target.getTotalNumberOfBytes(), is(0));

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
}
