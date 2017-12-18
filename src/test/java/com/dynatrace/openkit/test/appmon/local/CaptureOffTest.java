/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.CaptureOffTestShared;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class CaptureOffTest extends AbstractLocalAppMonTest {

    public void setUp() throws InterruptedException {
        CaptureOffTestShared.setUp(testConfiguration);
        super.setUp();
    }

    @Test
    public void test() {
        CaptureOffTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        Assert.assertEquals(2, sentRequests.size());
        validateRequest(sentRequests.get(0), RequestType.STATUS, "GET", getDefaultEndpoint(), null, "");
        validateRequest(sentRequests.get(1), RequestType.TIMESYNC, "GET", getDefaultTimeSyncEndpoint(), null, "");
    }

}
