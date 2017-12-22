/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.Response;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;

public class TestHTTPClient extends HTTPClient {

	public class Request {

		private RequestType requestType;
		private String url;
		private String clientIPAddress;
		private String decodedData;
		private String method;

		public Request(RequestType requestType, String url, String clientIPAddress, byte[] data, String method) {
			this.requestType = requestType;
			this.url = url;
			this.clientIPAddress = clientIPAddress;
			this.method = method;

			String decodedData = "";
			if (data != null) {
				try {
					decodedData = new String(data, Beacon.CHARSET);
				} catch (UnsupportedEncodingException e) {
					// must not happen, as UTF-8 should *really* be supported
				}
			}
			this.decodedData = decodedData;
		}

		public RequestType getRequestType() {
			return requestType;
		}

		public String getURL() {
			return url;
		}

		public String getClientIPAddress() {
			return clientIPAddress;
		}

		public String getDecodedData() {
			return decodedData;
		}

		public String getMethod() {
			return method;
		}

	}

	private boolean remoteTest;
	private ArrayList<Request> sentRequests = new ArrayList<Request>();

	private String rawStatusResponse = null;
	private StatusResponse statusResponse = null;
	private String rawTimeSyncResponse = null;
	private TimeSyncResponse timeSyncResponse = null;

	public TestHTTPClient(String baseURL, String applicationID, int serverID, boolean remoteTest) {
		super(new DefaultLogger(true), new HTTPClientConfiguration(baseURL, serverID, applicationID, new SSLStrictTrustManager()));
		this.remoteTest = remoteTest;
	}

	@Override
	protected Response sendRequest(RequestType requestType, String url, String clientIPAddress, byte[] data, String method) {
    	Request request = new Request(requestType, url, clientIPAddress, data, method);
		sentRequests.add(request);

		if (remoteTest) {
    		return super.sendRequest(requestType, url, clientIPAddress, data, method);
    	} else {
			System.out.println("Local HTTP " + requestType.getRequestName() + " Request: " + url);

			String rawResponse = null;
			Response response = null;
    		switch (requestType) {
    			case STATUS:
    				rawResponse = rawStatusResponse;
    				response = statusResponse;
    				break;
    			case BEACON:
					System.out.println("Local Beacon Payload: " + request.getDecodedData());

    				rawResponse = rawStatusResponse;
					response = statusResponse;
					break;
    			case TIMESYNC:
    				rawResponse = rawTimeSyncResponse;
    				response = timeSyncResponse;
    				break;
    		}

    		System.out.println("Local HTTP Response: " + rawResponse);
			if (response != null) {
	    		System.out.println("Local HTTP Response Code: " + response.getResponseCode());
			}

			return response;
    	}
	}

	public ArrayList<Request> getSentRequests() {
		return sentRequests;
	}

	public void setStatusResponse(String response, int responseCode) {
		this.rawStatusResponse = response;
		this.statusResponse = new StatusResponse(response, responseCode);
	}

	public void setTimeSyncResponse(String response, int responseCode) {
		this.rawTimeSyncResponse = response;
		this.timeSyncResponse = new TimeSyncResponse(response, responseCode);
	}

}
