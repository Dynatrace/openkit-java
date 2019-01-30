/**
 * Copyright 2018-2019 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.util.PercentEncoder;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.providers.HttpURLConnectionWrapper;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.GZIPOutputStream;

/**
 * HTTP client helper which abstracts the 3 basic request types:
 * - status check
 * - beacon send
 * - time sync
 */
public class HTTPClient {

    public enum RequestType {

        STATUS("Status"),                // status check
        BEACON("Beacon"),                // beacon send
        TIMESYNC("TimeSync");            // time sync

        private String requestName;

        RequestType(String requestName) {
            this.requestName = requestName;
        }

        public String getRequestName() {
            return requestName;
        }

    }

    // request type constants
    private static final String REQUEST_TYPE_MOBILE = "type=m";
    private static final String REQUEST_TYPE_TIMESYNC = "type=mts";

    // query parameter constants
    private static final String QUERY_KEY_SERVER_ID = "srvid";
    private static final String QUERY_KEY_APPLICATION = "app";
    private static final String QUERY_KEY_VERSION = "va";
    private static final String QUERY_KEY_PLATFORM_TYPE = "pt";
    private static final String QUERY_KEY_AGENT_TECHNOLOGY_TYPE = "tt";

    // constant query parameter values
    private static final String PLATFORM_TYPE_OPENKIT = "1";
    private static final String AGENT_TECHNOLOGY_TYPE = "okjava";

    // additional reserved characters for URL encoding
    private static final char[] QUERY_RESERVED_CHARACTERS = {'_'};

    // connection constants
    private static final int MAX_SEND_RETRIES = 3;
    private static final int RETRY_SLEEP_TIME = 200;        // retry sleep time in ms
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 30000;

    // URLs for requests
    private final String monitorURL;
    private final String timeSyncURL;

    private final int serverID;

    private final SSLTrustManager sslTrustManager;

    private final Logger logger;

    // *** constructors ***

    public HTTPClient(Logger logger, HTTPClientConfiguration configuration) {
        this.logger = logger;
        serverID = configuration.getServerID();
        monitorURL = buildMonitorURL(configuration.getBaseURL(), configuration.getApplicationID(), serverID);
        timeSyncURL = buildTimeSyncURL(configuration.getBaseURL());
        sslTrustManager = configuration.getSSLTrustManager();
    }

    // *** public methods ***

    // sends a status check request and returns a status response
    public StatusResponse sendStatusRequest() {
        return (StatusResponse) sendRequest(RequestType.STATUS, monitorURL, null, null, "GET");
    }

    // sends a beacon send request and returns a status response
    public StatusResponse sendBeaconRequest(String clientIPAddress, byte[] data) {
        return (StatusResponse) sendRequest(RequestType.BEACON, monitorURL, clientIPAddress, data, "POST");
    }

    // sends a time sync request and returns a time sync response
    public TimeSyncResponse sendTimeSyncRequest() {
        return (TimeSyncResponse) sendRequest(RequestType.TIMESYNC, timeSyncURL, null, null, "GET");
    }

    // *** protected methods ***

    // generic request send with some verbose output and exception handling
    // protected because it's overridden by the TestHTTPClient
    protected Response sendRequest(RequestType requestType, String url, String clientIPAddress, byte[] data, String method) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("HTTP " + requestType.getRequestName() + " Request: " + url);
            }
            HttpURLConnectionWrapper httpURLConnectionWrapper = new HttpURLConnectionWrapperImpl(url, MAX_SEND_RETRIES);
            return sendRequestInternal(requestType, httpURLConnectionWrapper, clientIPAddress, data, method);
        } catch (Exception e) {
            logger.error("ERROR: " + requestType.getRequestName() + " Request failed!", e);
        }
        return null;
    }

    // *** private methods ***

    // generic internal request send
    private Response sendRequestInternal(RequestType requestType, HttpURLConnectionWrapper httpURLConnectionWrapper, String clientIPAddress,
            byte[] data, String method) throws IOException, GeneralSecurityException {
        while (true) {
            try {
                HttpURLConnection connection = httpURLConnectionWrapper.getHttpURLConnection();

                // specific handling for HTTPS
                if (connection instanceof HttpsURLConnection) {
                    applySSLTrustManager((HttpsURLConnection) connection);
                }

                if (clientIPAddress != null) {
                    connection.addRequestProperty("X-Client-IP", clientIPAddress);
                }
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod(method);

                // gzip beacon data, if available
                if ((data != null) && (data.length > 0)) {
                    byte[] gzippedData = gzip(data);

                    String decodedData = "";
                    try {
                        decodedData = new String(data, Beacon.CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        logger.error("JRE does not support UTF-8", e);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Beacon Payload: " + decodedData);
                    }

                    connection.setRequestProperty("Content-Encoding", "gzip");
                    connection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(gzippedData);
                    outputStream.close();
                }

                return handleResponse(connection);


            } catch (IOException exception) {
                if (!httpURLConnectionWrapper.isRetryAllowed()) {
                    throw exception;
                }

                logger.info(String.format(
                        "Exception occurred during connection establishment. Cause : %s . Retry in progress.", exception.toString()));

                try {
                    Thread.sleep(RETRY_SLEEP_TIME);
                } catch (InterruptedException e) {
                    // TODO thomas.grassauer@dynatrace.com - check exception handling
                }
            }
        }
    }

    private Response handleResponse(HttpURLConnection connection) throws IOException {
        // get response code
        int responseCode = connection.getResponseCode();

        // check response code
        if (responseCode >= 400) {
            // process error

            // read error response
            String response = readResponse(connection.getErrorStream()); // input stream is closed in readResponse

            if (logger.isDebugEnabled()) {
                logger.debug("HTTP Response: " + response);
                logger.debug("HTTP Response Code: " + responseCode);
            }

            // return null if error occurred
            return null;

        } else {
            // process status response

            // reading HTTP response
            String response = readResponse(connection.getInputStream()); // input stream is closed in readResponse


            if (logger.isDebugEnabled()) {
                logger.debug("HTTP Response: " + response);
                logger.debug("HTTP Response Code: " + responseCode);
            }

            // create typed response based on response content
            if (response.startsWith(REQUEST_TYPE_TIMESYNC)) {
                return new TimeSyncResponse(response, responseCode);
            } else if (response.startsWith(REQUEST_TYPE_MOBILE)) {
                return new StatusResponse(response, responseCode);
            } else {
                return null;
            }
        }
    }

    private void applySSLTrustManager(HttpsURLConnection connection) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        X509TrustManager x509TrustManager;
        if (sslTrustManager == null || sslTrustManager.getX509TrustManager() == null) {
            // if provided trust manager is null use a strict one by default
            x509TrustManager = new SSLStrictTrustManager().getX509TrustManager();
        } else {
            x509TrustManager = sslTrustManager.getX509TrustManager();
        }
        context.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
        connection.setSSLSocketFactory(context.getSocketFactory());

        if (sslTrustManager != null && sslTrustManager.getHostnameVerifier() != null) {
            connection.setHostnameVerifier(sslTrustManager.getHostnameVerifier());
        }
    }

    // build URL used for status check and beacon send requests
    private String buildMonitorURL(String baseURL, String applicationID, int serverID) {
        StringBuilder monitorURLBuilder = new StringBuilder();

        monitorURLBuilder.append(baseURL);
        monitorURLBuilder.append('?');
        monitorURLBuilder.append(REQUEST_TYPE_MOBILE);

        appendQueryParam(monitorURLBuilder, QUERY_KEY_SERVER_ID, Integer.toString(serverID));
        appendQueryParam(monitorURLBuilder, QUERY_KEY_APPLICATION, applicationID);
        appendQueryParam(monitorURLBuilder, QUERY_KEY_VERSION, Beacon.OPENKIT_VERSION);
        appendQueryParam(monitorURLBuilder, QUERY_KEY_PLATFORM_TYPE, PLATFORM_TYPE_OPENKIT);
        appendQueryParam(monitorURLBuilder, QUERY_KEY_AGENT_TECHNOLOGY_TYPE, AGENT_TECHNOLOGY_TYPE);

        return monitorURLBuilder.toString();
    }

    // build URL used for time sync requests
    private String buildTimeSyncURL(String baseURL) {
        StringBuilder timeSyncURLBuilder = new StringBuilder();

        timeSyncURLBuilder.append(baseURL);
        timeSyncURLBuilder.append('?');
        timeSyncURLBuilder.append(REQUEST_TYPE_TIMESYNC);

        return timeSyncURLBuilder.toString();
    }

    // helper method for appending query parameters
    private static void appendQueryParam(StringBuilder urlBuilder, String key, String value) {
        urlBuilder.append('&');
        urlBuilder.append(key);
        urlBuilder.append('=');
        urlBuilder.append(PercentEncoder.encode(value, "UTF-8", QUERY_RESERVED_CHARACTERS));
    }

    // helper method for gzipping beacon data
    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(data);
        gzipOutputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    // *** getter methods ***

    public int getServerID() {
        return serverID;
    }

    private static String readResponse(InputStream inputStream) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        // reading HTTP response
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                responseBuilder.append(new String(buffer, 0, length, Beacon.CHARSET));
            }
        } finally {
            inputStream.close();
        }

        return responseBuilder.toString();
    }

    // A wrapper class to hold url and create HttpURLConnection on-demand.
    // This allows us to generate HttpURLConnections for failed attempts as well as test the HTTPClient with desired
    // output values
    public static class HttpURLConnectionWrapperImpl implements HttpURLConnectionWrapper {
        private final URL httpURL;
        private final int maxCount;
        private int connectCount;

        public HttpURLConnectionWrapperImpl(String url, int maxCount) throws MalformedURLException {
            this.httpURL= new URL(url);
            this.maxCount = maxCount;
        }

        @Override
        public HttpURLConnection getHttpURLConnection() throws IOException {
            this.connectCount += 1;
            return (HttpURLConnection) httpURL.openConnection();
        }

        @Override
        public boolean isRetryAllowed() {
            return this.maxCount > this.connectCount;
        }
    }
}
