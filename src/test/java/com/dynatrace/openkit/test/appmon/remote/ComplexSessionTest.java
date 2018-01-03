/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.remote;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ComplexSessionTestShared;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

@Ignore("Integration tests are ignored")
public class ComplexSessionTest extends AbstractRemoteAppMonTest {

    @Test
    public void test() {
        ComplexSessionTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        validateDefaultRequests(sentRequests, null);
    }

}
