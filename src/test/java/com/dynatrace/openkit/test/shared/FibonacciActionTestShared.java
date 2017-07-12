/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class FibonacciActionTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);
		Action rootAction = session.enterAction("FibonacciAction");

		calculateFibonacci(7, rootAction);

		rootAction.leaveAction();

		session.end();

		openKit.shutdown();
	}

	private static int calculateFibonacci(int number, Action parentAction) {
		if (number < 0) {
			return -1;
		}

		Action action = parentAction.enterAction("CalculateAction");

		int returnValue;
		if (number == 0) {
			returnValue = 0;
		} else if (number == 1) {
			returnValue = 1;
		} else {
			returnValue = calculateFibonacci(number - 2, action) + calculateFibonacci(number - 1, action);
		}

		action.reportValue("Fibonacci No. " + number, returnValue);
		action.leaveAction();

		return returnValue;
	}

}
