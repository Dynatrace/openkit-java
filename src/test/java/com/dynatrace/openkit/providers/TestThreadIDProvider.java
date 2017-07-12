/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.providers.ThreadIDProvider;

public class TestThreadIDProvider extends ThreadIDProvider {

	@Override
	protected long provideThreadID() {
		return 1;
	}

}
