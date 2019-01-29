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

import com.dynatrace.openkit.api.LogLevel;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.util.PercentEncoder;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.providers.HttpURLConnectionWrapper;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        TIMESYNC("TimeSync"),            // time sync
        NEW_SESSION("NewSession");       // new session request

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
    private static final String QUERY_KEY_NEW_SESSION = "ns";

    // additional reserved characters for URL encoding
    private static final char[] QUERY_RESERVED_CHARACTERS = {'_'};

    // connection constants
    private static final int MAX_SEND_RETRIES = 3;
    private static final int RETRY_SLEEP_TIME = 200;        // retry sleep time in ms
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 30000;

    // URLs for requests
    private final String monitorURL;
    private final String newSessionURL;
    private final String timeSyncURL;

    private final int serverID;

    private final SSLTrustManager sslTrustManager;

    private final Logger logger;

    // *** constructors ***

    public HTTPClient(Logger logger, HTTPClientConfiguration configuration) {
        this.logger = logger;
        serverID = configuration.getServerID();
        monitorURL = buildMonitorURL(configuration.getBaseURL(), configuration.getApplicationID(), serverID);
        newSessionURL = buildNewSessionURL(configuration.getBaseURL(), configuration.getApplicationID(), serverID);
        timeSyncURL = buildTimeSyncURL(configuration.getBaseURL());
        sslTrustManager = configuration.getSSLTrustManager();
    }

    // *** public methods ***

    // sends a status check request and returns a status response
    public StatusResponse sendStatusRequest() {
        Response response = sendRequest(RequestType.STATUS, monitorURL, null, null, "GET");
        return response == null
            ? new StatusResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap())
            : (StatusResponse)response;
    }

    public StatusResponse sendNewSessionRequest() {
        Response response = sendRequest(RequestType.NEW_SESSION, newSessionURL, null, null, "GET");
        return response == null
            ? new StatusResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap())
            : (StatusResponse)response;
    }

    // sends a beacon send request and returns a status response
    public StatusResponse sendBeaconRequest(String clientIPAddress, byte[] data) {
        Response response = sendRequest(RequestType.BEACON, monitorURL, clientIPAddress, data, "POST");
        return response == null
            ? new StatusResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap())
            : (StatusResponse)response;
    }

    // sends a time sync request and returns a time sync response
    public TimeSyncResponse sendTimeSyncRequest() {
        Response response = sendRequest(RequestType.TIMESYNC, timeSyncURL, null, null, "GET");
        return response == null
            ? new TimeSyncResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap())
            : (TimeSyncResponse)response;
    }

    // *** protected methods ***

    // generic request send with some verbose output and exception handling
    // protected because it's overridden by the TestHTTPClient
    protected Response sendRequest(RequestType requestType, String url, String clientIPAddress, byte[] data, String method) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName() + " sendRequest() - HTTP " + requestType.getRequestName() + " Request: " + url);
            }
            HttpURLConnectionWrapperImpl httpURLConnectionWrapperImpl = new HttpURLConnectionWrapperImpl(url, MAX_SEND_RETRIES);
            return sendRequestInternal(requestType, httpURLConnectionWrapperImpl, clientIPAddress, data, method);
        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + " sendRequest() - ERROR: " + requestType + " Request failed!", e);
        }
        return unknownErrorResponse(requestType);
    }

    // *** private methods ***

    // only for unit testing the HTTPClient
    Response sendRequest(RequestType requestType, HttpURLConnectionWrapper httpURLConnectionWrapper, String clientIPAddress, byte[] data,
                         String method) {
        try {
            return sendRequestInternal(requestType, httpURLConnectionWrapper, clientIPAddress, data, method);
        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + "sendRequest() - ERROR: " + requestType + " Request failed!", e);
        }
        return unknownErrorResponse(requestType);
    }

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
                if (data != null && data.length > 0) {
                    byte[] gzippedData = gzip(data);

                    String decodedData = "";
                    try {
                        decodedData = new String(data, Beacon.CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        logger.error(getClass().getSimpleName() + " sendRequestInternal() - JRE does not support UTF-8", e);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(getClass().getSimpleName() + " sendRequestInternal() - Beacon Payload: " + decodedData);
                    }

                    connection.setRequestProperty("Content-Encoding", "gzip");
                    connection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(gzippedData);
                    outputStream.close();
                }

                return handleResponse(requestType, connection);


            } catch (IOException exception) {
                if (!httpURLConnectionWrapper.isRetryAllowed()) {
                    throw exception;
                }

                logger.log(LogLevel.INFO, "Exception occurred during connection establishment. Retry in progress.", exception);

                try {
                    Thread.sleep(RETRY_SLEEP_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return unknownErrorResponse(requestType);
                }
            }
        }
    }

    private Response handleResponse(RequestType requestType, HttpURLConnection connection) throws IOException {
        // get response code
        int responseCode = connection.getResponseCode();

        String response = responseCode >= 400
            ? readResponse(connection.getErrorStream()) // error stream is closed in readResponse
            : readResponse(connection.getInputStream()); // input stream is closed in readResponse

        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " handleResponse() - HTTP Response: " + response);
            logger.debug(getClass().getSimpleName() + " handleResponse() - HTTP Response Code: " + responseCode);
        }

        // create typed response based on request type and response content
        if (requestType.getRequestName().equals(RequestType.TIMESYNC.getRequestName())) {
            return responseCode >= 400
                ? new TimeSyncResponse(logger, "", responseCode, Collections.<String, List<String>>emptyMap())
                : parseTimeSyncResponse(response, responseCode, connection.getHeaderFields());
        }
        else if ((requestType.getRequestName().equals(RequestType.BEACON.getRequestName()))
            || (requestType.getRequestName().equals(RequestType.STATUS.getRequestName()))
            || (requestType.getRequestName().equals(RequestType.NEW_SESSION.getRequestName()))) {
            return responseCode >= 400
                ? new StatusResponse(logger, "", responseCode, Collections.<String, List<String>>emptyMap())
                : parseStatusResponse(response, responseCode, connection.getHeaderFields());
        }
        else {
            logger.warning(getClass().getSimpleName() + " handleResponse() - Unknown request type " + requestType + " - ignoring response");
            return unknownErrorResponse(requestType);
        }
    }

    private Response parseTimeSyncResponse(String response, int responseCode, Map<String, List<String>> headers) {
        if (isTimeSyncResponse(response)) {
            try {
                return new TimeSyncResponse(logger, response, responseCode, responseHeadersWithLowerCaseKeys(headers));
            }
            catch(Exception e) {
                logger.error(getClass().getSimpleName() + " parseTimeSyncResponse() - Failed to parse TimeSyncResponse", e);
                return new TimeSyncResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap());
            }
        }

        // invalid/unexpected response
        logger.warning(getClass().getSimpleName() + " parseTimeSyncResponse() - The HTTPResponse \"" + response + "\" is not a valid time sync response");
        return new TimeSyncResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap());
    }

    private Response parseStatusResponse(String response, int responseCode, Map<String, List<String>> headers) {
        if (isStatusResponse(response) && !isTimeSyncResponse(response)) {
            try {
                return new StatusResponse(logger, response, responseCode, responseHeadersWithLowerCaseKeys(headers));
            }
            catch (Exception e) {
                logger.error(getClass().getSimpleName() + " parseStatusResponse() - Failed to parse StatusResponse", e);
                return new StatusResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap());
            }
        }

        // invalid/unexpected response
        logger.warning(getClass().getSimpleName() + " parseStatusResponse() - The HTTPResponse \"" + response + "\" is not a valid status response");
        return new StatusResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap());
    }

    private static Map<String, List<String>> responseHeadersWithLowerCaseKeys(Map<String, List<String>> headers) {

        Map<String, List<String>> result = new HashMap<String, List<String>>(headers.size());
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    private static  boolean isStatusResponse(String response) {
        return response.startsWith(REQUEST_TYPE_MOBILE);
    }

    private static boolean isTimeSyncResponse(String response) {
        return response.startsWith(REQUEST_TYPE_TIMESYNC);
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
    private static String buildMonitorURL(String baseURL, String applicationID, int serverID) {
        StringBuilder monitorURLBuilder = new StringBuilder();

        monitorURLBuilder.append(baseURL);
        monitorURLBuilder.append('?');
        monitorURLBuilder.append(REQUEST_TYPE_MOBILE);

        appendQueryParam(monitorURLBuilder, QUERY_KEY_SERVER_ID, Integer.toString(serverID));
        appendQueryParam(monitorURLBuilder, QUERY_KEY_APPLICATION, applicationID);
        appendQueryParam(monitorURLBuilder, QUERY_KEY_VERSION, ProtocolConstants.OPENKIT_VERSION);
        appendQueryParam(monitorURLBuilder, QUERY_KEY_PLATFORM_TYPE, String.valueOf(ProtocolConstants.PLATFORM_TYPE_OPENKIT));
        appendQueryParam(monitorURLBuilder, QUERY_KEY_AGENT_TECHNOLOGY_TYPE, ProtocolConstants.AGENT_TECHNOLOGY_TYPE);

        return monitorURLBuilder.toString();
    }

    private static String buildNewSessionURL(String baseURL, String applicationID, int serverID) {
        StringBuilder monitorURLBuilder = new StringBuilder(buildMonitorURL(baseURL, applicationID, serverID));

        appendQueryParam(monitorURLBuilder, QUERY_KEY_NEW_SESSION, "1");

        return monitorURLBuilder.toString();
    }

    // build URL used for time sync requests
    private static String buildTimeSyncURL(String baseURL) {

        return baseURL + '?' + REQUEST_TYPE_TIMESYNC;
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

    int getServerID() {
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

    private  Response unknownErrorResponse(RequestType requestType) {

        if (requestType == null) {
            return null;
        }

        switch (requestType) {
            case STATUS:
            case BEACON: // fallthrough
            case NEW_SESSION: // fallthrough
                return new StatusResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap());
            case TIMESYNC:
                return new TimeSyncResponse(logger, "", Integer.MAX_VALUE, Collections.<String, List<String>>emptyMap());
            default:
                // should not be reached
                return null;
        }
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

        public HttpURLConnection getHttpURLConnection() throws IOException {
            this.connectCount += 1;
            return (HttpURLConnection) httpURL.openConnection();
        }

        public boolean isRetryAllowed() {
            return this.maxCount > this.connectCount;
        }
    }
}
