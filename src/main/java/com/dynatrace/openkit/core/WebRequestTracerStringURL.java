/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Setting the Dynatrace tag to the {@link OpenKit#WEBREQUEST_TAG_HEADER} HTTP header has to be done manually by the user.
 * Inherited class of {@link WebRequestTracerBaseImpl} which can be used for tracing and timing of a web request handled by any 3rd party HTTP Client.
 */
public class WebRequestTracerStringURL extends WebRequestTracerBaseImpl {

	// *** constructors ***

	// creates web request tracer with a simple string URL
	public WebRequestTracerStringURL(Beacon beacon, ActionImpl action, String url) {
		super(beacon, action);

		// separate query string from URL
		if (url != null) {
			this.url = url.split("\\?")[0];
		}
	}

}
