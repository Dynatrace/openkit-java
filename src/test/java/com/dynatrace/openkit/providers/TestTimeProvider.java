/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import java.util.concurrent.atomic.AtomicLong;

import com.dynatrace.openkit.providers.TimeProvider;

public class TestTimeProvider extends TimeProvider {

	private AtomicLong currentTimestamp = new AtomicLong(1000000);

	@Override
	protected long provideTimestamp() {
		return currentTimestamp.addAndGet(1000);
	}

}
