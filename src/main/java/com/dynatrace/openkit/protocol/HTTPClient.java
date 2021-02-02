/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * HTTP client helper which abstracts the 2 basic request types:
 * - status check
 * - beacon send
 */
public class HTTPClient {

    public enum RequestType {

        STATUS("Status"),                // status check
        BEACON("Beacon"),                // beacon send
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

    // query parameter constants
    private static final String QUERY_KEY_SERVER_ID = "srvid";
    private static final String QUERY_KEY_APPLICATION = "app";
    private static final String QUERY_KEY_VERSION = "va";
    private static final String QUERY_KEY_PLATFORM_TYPE = "pt";
    private static final String QUERY_KEY_AGENT_TECHNOLOGY_TYPE = "tt";
    private static final String QUERY_KEY_RESPONSE_TYPE = "resp";
    private static final String QUERY_KEY_CONFIG_TIMESTAMP = "cts";
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

    private final int serverID;

    private final SSLTrustManager sslTrustManager;

    private final Logger logger;

    // *** constructors ***

    public HTTPClient(Logger logger, HTTPClientConfiguration configuration) {
        this.logger = logger;
        serverID = configuration.getServerID();
        monitorURL = buildMonitorURL(configuration.getBaseURL(), configuration.getApplicationID(), serverID);
        newSessionURL = buildNewSessionURL(configuration.getBaseURL(), configuration.getApplicationID(), serverID);
        sslTrustManager = configuration.getSSLTrustManager();
    }

    // *** public methods ***

    // sends a status check request and returns a status response
    public StatusResponse sendStatusRequest(AdditionalQueryParameters additionalParameters) {
        String url = appendAdditionalQueryParameters(monitorURL, additionalParameters);
        StatusResponse response = sendRequest(RequestType.STATUS, url, null, null, "GET");
        return response == null
                ? StatusResponse.createErrorResponse(logger, Integer.MAX_VALUE)
                : response;
    }

    public StatusResponse sendNewSessionRequest(AdditionalQueryParameters additionalParameters) {
        String url = appendAdditionalQueryParameters(newSessionURL, additionalParameters);
        StatusResponse response = sendRequest(RequestType.NEW_SESSION, url, null, null, "GET");
        return response == null
                ? StatusResponse.createErrorResponse(logger, Integer.MAX_VALUE)
                : response;
    }

    // sends a beacon send request and returns a status response
    public StatusResponse sendBeaconRequest(
            String clientIPAddress,
            byte[] data,
            AdditionalQueryParameters additionalParameters) {
        String url = appendAdditionalQueryParameters(monitorURL, additionalParameters);
        StatusResponse response = sendRequest(RequestType.BEACON, url, clientIPAddress, data, "POST");
        return response == null
                ? StatusResponse.createErrorResponse(logger, Integer.MAX_VALUE)
                : response;
    }

    // *** protected methods ***

    // generic request send with some verbose output and exception handling
    // protected because it's overridden by the TestHTTPClient
    StatusResponse sendRequest(RequestType requestType, String url, String clientIPAddress, byte[] data, String method) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName() + " sendRequest() - HTTP " + requestType.getRequestName() + " Request: " + url);
            }
            HttpURLConnectionWrapper httpURLConnectionWrapper = new HttpURLConnectionWrapperImpl(url, MAX_SEND_RETRIES);
            return sendRequestInternal(requestType, httpURLConnectionWrapper, clientIPAddress, data, method);
        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + " sendRequest() - ERROR: " + requestType + " Request failed!", e);
        }
        return unknownErrorResponse(requestType);
    }

    // *** private methods ***

    // only for unit testing the HTTPClient
    StatusResponse sendRequest(RequestType requestType, HttpURLConnectionWrapper httpURLConnectionWrapper, String clientIPAddress, byte[] data,
                               String method) {
        try {
            return sendRequestInternal(requestType, httpURLConnectionWrapper, clientIPAddress, data, method);
        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + "sendRequest() - ERROR: " + requestType + " Request failed!", e);
        }
        return unknownErrorResponse(requestType);
    }

    // generic internal request send
    private StatusResponse sendRequestInternal(RequestType requestType, HttpURLConnectionWrapper httpURLConnectionWrapper, String clientIPAddress,
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

                // write the post body data
                writePostBodyData(connection, data);

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

    private void writePostBodyData(HttpURLConnection connection, byte[] data) throws IOException {

        // gzip beacon data, if available
        if (data == null || data.length == 0) {
            return;
        }

        byte[] gzippedData = gzip(data);

        String decodedData = decodeData(data);

        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " sendRequestInternal() - Beacon Payload: " + decodedData);
        }

        connection.setRequestProperty("Content-Encoding", "gzip");
        connection.setRequestProperty("Content-Length", String.valueOf(gzippedData.length));
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(gzippedData);
        outputStream.close();
    }

    private String decodeData(byte[] data) {
        try {
            return new String(data, Beacon.CHARSET);
        } catch (UnsupportedEncodingException e) {
            logger.error(getClass().getSimpleName() + " sendRequestInternal() - JRE does not support UTF-8", e);
            return "";
        }
    }

    private StatusResponse handleResponse(RequestType requestType, HttpURLConnection connection) throws IOException {
        // get response code
        int responseCode = connection.getResponseCode();

        String response = responseCode >= 400
                ? null
                : readResponse(connection.getInputStream()); // input stream is closed in readResponse

        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " handleResponse() - HTTP Response: " + response);
            logger.debug(getClass().getSimpleName() + " handleResponse() - HTTP Response Code: " + responseCode);
        }

        // create typed response based on request type and response content
        if (requestType == RequestType.BEACON
                || requestType == RequestType.STATUS
                || requestType == RequestType.NEW_SESSION) {
            return responseCode >= 400
                    ? StatusResponse.createErrorResponse(logger, responseCode, connection.getHeaderFields())
                    : parseStatusResponse(response, responseCode, connection.getHeaderFields());
        } else {
            logger.warning(getClass().getSimpleName() + " handleResponse() - Unknown request type " + requestType + " - ignoring response");
            return unknownErrorResponse(requestType);
        }
    }

    private StatusResponse parseStatusResponse(String response, int responseCode, Map<String, List<String>> headers) {
        try {
            ResponseAttributes parsedAttributes = ResponseParser.parseResponse(response);
            return StatusResponse.createSuccessResponse(logger, parsedAttributes,responseCode, headers);
        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + " parseStatusResponse() - Failed to parse StatusResponse", e);
            return StatusResponse.createErrorResponse(logger, Integer.MAX_VALUE);
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
        appendQueryParam(monitorURLBuilder, QUERY_KEY_RESPONSE_TYPE, ProtocolConstants.RESPONSE_TYPE);

        return monitorURLBuilder.toString();
    }

    private static String buildNewSessionURL(String baseURL, String applicationID, int serverID) {
        StringBuilder monitorURLBuilder = new StringBuilder(buildMonitorURL(baseURL, applicationID, serverID));

        appendQueryParam(monitorURLBuilder, QUERY_KEY_NEW_SESSION, "1");

        return monitorURLBuilder.toString();
    }

    private static String appendAdditionalQueryParameters(String baseUrl, AdditionalQueryParameters parameters) {
        if (parameters == null) {
            return baseUrl;
        }

        StringBuilder builder = new StringBuilder(baseUrl);
        appendQueryParam(builder, QUERY_KEY_CONFIG_TIMESTAMP, Long.toString(parameters.getConfigurationTimestamp()));

        return builder.toString();
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
        if (inputStream == null) {
            return "";
        }

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

    private StatusResponse unknownErrorResponse(RequestType requestType) {

        if (requestType == null) {
            return null;
        }

        switch (requestType) {
            case STATUS:
            case BEACON: // fallthrough
            case NEW_SESSION: // fallthrough
                return StatusResponse.createErrorResponse(logger,Integer.MAX_VALUE);
            default:
                // should not be reached
                return null;
        }
    }

    /**
     * A wrapper class to hold url and create {@link HttpURLConnection} on-demand.
     * This allows to generate {@link HttpURLConnection} for failed attempts.
     */
    private static class HttpURLConnectionWrapperImpl implements HttpURLConnectionWrapper {
        private final URL httpURL;
        private final int maxCount;
        private int connectCount;

        HttpURLConnectionWrapperImpl(String url, int maxCount) throws MalformedURLException {
            this.httpURL = new URL(url);
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
