/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ComplexSessionTestShared;

public class ComplexSessionTest extends AbstractLocalAppMonTest {

	@Test
	public void test() {
		ComplexSessionTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration.getVisitorID() + "&sn=1&tv=1002000&ts=1001000&tx=1016000&et=12&na=IntegerValue&it=1&pa=1&s0=2&t0=3000&vl=45&et=13&na=DoubleValue&it=1&pa=1&s0=3&t0=4000&vl=9.2&et=11&na=String&it=1&pa=1&s0=4&t0=5000&vl=This+is+a+string&et=40&na=errorName&it=1&pa=1&s0=5&t0=6000&ev=22&rs=meaningful+reason&et=40&na=FATAL+ERROR&it=1&pa=1&s0=6&t0=7000&ev=42&rs=valid+reason&et=10&na=EventOne&it=1&pa=2&s0=8&t0=9000&et=30&na=http%3A%2F%2Fmydomain%2Fapp%2Fsearch.php&it=1&pa=1&s0=10&t0=11000&s1=11&t1=1000&et=19&it=1&pa=0&s0=13&t0=14000&et=1&na=ActionTwo&it=1&ca=2&pa=1&s0=7&t0=8000&s1=9&t1=2000&et=1&na=ActionOne&it=1&ca=1&pa=0&s0=1&t0=2000&s1=12&t1=11000";
		validateDefaultRequests(sentRequests, expectedBeacon);
	}

}
