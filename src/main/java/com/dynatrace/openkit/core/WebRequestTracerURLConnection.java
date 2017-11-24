/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import java.net.URL;
import java.net.URLConnection;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Inherited class of {@link WebRequestTracerBaseImpl} which can be used for tracing and timing of a web request provided as a URLConnection.
 */
public class WebRequestTracerURLConnection extends WebRequestTracerBaseImpl {

	// *** constructors ***

	// creates web request tag with a URLConnection
	public WebRequestTracerURLConnection(Beacon beacon, ActionImpl action, URLConnection connection) {
		super(beacon, action);

		// only set tag header and URL if connection is not null
		if (connection != null) {
			setTagOnConnection(connection);

			// separate query string from URL
			URL connectionURL = connection.getURL();
			if (connectionURL != null) {
				this.url = connectionURL.toString().split("\\?")[0];
			}
		}
	}

	// *** private methods ***

	// set the Dynatrace tag on the provided URLConnection
	private void setTagOnConnection(URLConnection connection) {
		// check if header is already set
		String existingTag = connection.getRequestProperty(OpenKit.WEBREQUEST_TAG_HEADER);
		if (existingTag == null) {
			// if not yet set -> set it now
			try {
				connection.setRequestProperty(OpenKit.WEBREQUEST_TAG_HEADER, getTag());
			} catch (Exception e) {
				// if it does not work -> simply ignore
			}
		}
	}

}
