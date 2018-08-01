/**
 * Copyright 2018 Dynatrace LLC
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
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class HTTPClientTest {

    private static final String CHARSET = "UTF-8";
    private static final String APP_ID = "appID";
    private static final int SERVER_ID = 123;
    private static final String BASE_URL = "http://www.example.com";

    private HTTPClientConfiguration configuration;

    private Logger logger;

    @Before
    public void setUp() {
        configuration = mock(HTTPClientConfiguration.class);
        when(configuration.getApplicationID()).thenReturn(APP_ID);
        when(configuration.getServerID()).thenReturn(SERVER_ID);
        when(configuration.getBaseURL()).thenReturn(BASE_URL);

        logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
    }

    @Test
    public void constructor() {
        HTTPClient client = new HTTPClient(logger, configuration);

        // verify
        assertThat(client.getServerID(), is(SERVER_ID));
    }

    @Test
    public void canHandleNullPointerExceptionWhenSendRequest() {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);

        // when
        Response response = client.sendRequest(null, "", null, null, null);

        // then
        assertThat(response, is(nullValue()));
    }

    @Test
    public void canHandleMalformedURLExceptionWhenSendRequest() {
        // given
        when(configuration.getBaseURL()).thenReturn("This is not a valid URL");
        HTTPClient client = new HTTPClient(logger, configuration);

        // when
        Response response = client.sendStatusRequest();

        // then
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendStatusRequestToSomeValidUrl() {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);

        // when
        StatusResponse response = client.sendStatusRequest();

        // then (we use a URL not understanding the beacon protocol, thus unknown error is expected)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendBeaconRequestToSomeValidUrl() {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);

        // when
        StatusResponse response = client.sendBeaconRequest("127.0.0.1", null);

        // then (we use a URL not understanding the beacon protocol, thus unknown error is expected)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(411)));
    }

    @Test
    public void sendTimeSyncRequestToSomeValidUrl() {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);

        // when
        TimeSyncResponse response = client.sendTimeSyncRequest();

        // then (we use a URL not understanding the beacon protocol, thus unknown error is expected)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendNewSessionRequestToSomeValidUrl() {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);

        // when
        StatusResponse response = client.sendNewSessionRequest();

        // then (we use a URL not understanding the beacon protocol, thus unknown error is expected)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendStatusRequestAndReadErrorResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(418);
        InputStream is = new ByteArrayInputStream("err".getBytes(CHARSET));
        when(connection.getErrorStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.STATUS, connection, null, null, "GET");

        // then (verify that for error responses unknown error is returned)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(418)));
    }

    @Test
    public void sendStatusRequestAndReadStatusResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.STATUS, connection, null, null, "GET");

        // then (verify that we properly send the request and parsed the response)
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(200));
    }

    @Test
    public void sendNewSessionRequestAndReadErrorResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(418);
        InputStream is = new ByteArrayInputStream("err".getBytes(CHARSET));
        when(connection.getErrorStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.NEW_SESSION, connection, null, null, "GET");

        // then (verify that for error responses unknown error is returned)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(418)));
    }

    @Test
    public void sendNewSessionRequestAndReadStatusResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.NEW_SESSION, connection, null, null, "GET");

        // then (verify that we properly send the request and parsed the response)
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(200));
    }

    @Test
    public void sendBeaconRequestAndReadStatusResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.BEACON, connection, "127.0.0.1", null, "POST");

        // then (verify that we properly send the request and parsed the response)
        assertThat(response.getResponseCode(), is(200));
    }

    @Test
    public void sendBeaconRequestWithGzippedDataAndReadStatusResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(connection.getOutputStream()).thenReturn(os);
        String data = "type=m";

        // when
        Response response = client.sendRequest(RequestType.BEACON, connection, "127.0.0.1", data.getBytes(), "POST");

        // then
        assertThat(response.getResponseCode(), is(200));
        assertThat(gunzip(os.toByteArray()), is(data));
    }

    /**
     * Local helper function to decompress a GZIP compressed byte array
     */
    private static String gunzip(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, CHARSET));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }

    @Test
    public void sendTimeSyncRequestAndReadStatusResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=mts".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.TIMESYNC, connection, null, null, "GET");

        // then (verify that we properly send the request and parsed the response)
        assertThat(response.getResponseCode(), is(200));
    }

    @Test
    public void sendTimeSyncRequestWithHttps() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=mts".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.TIMESYNC, connection, null, null, "GET");

        // then (verify that we properly send the request and parsed the response)
        assertThat(response.getResponseCode(), is(200));
    }

    /**
     * Tests the retry mechanism in the send method (method eventually shall succeed),
     */
    @Test
    public void sendRequestWithRetrySuccess() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(connection.getOutputStream())//
                .thenThrow(new IOException("Simulate first fail"))//
                .thenThrow(new IOException("Simulate second fail"))//
                .thenReturn(os); // simulate writing worked
        String data = "type=m";

        // when
        Response response = client.sendRequest(RequestType.BEACON, connection, "127.0.0.1", data.getBytes(), "POST");

        // then
        assertThat(response.getResponseCode(), is(200));
        assertThat(gunzip(os.toByteArray()), is(data));
    }

    /**
     * Breaks the retry mechanism (method shall never succeed).
     */
    @Test
    public void sendRequestWithRetryFail() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);
        when(connection.getOutputStream()).thenThrow(new IOException("Always fail"));
        String data = "type=m";

        // when
        Response response = client.sendRequest(RequestType.BEACON, connection, "127.0.0.1", data.getBytes(), "POST");

        // then
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendTimeSyncRequestWithMobileResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=m".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.TIMESYNC, connection, null, null, "GET");

        // then (verify unknown error response)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendStatusRequestWithTimeSyncResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=mts".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);

        // when
        Response response = client.sendRequest(RequestType.STATUS, connection, null, null, "GET");

        // then (verify unknown error response)
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendBeaconRequestWithTimeSyncResponse() throws IOException {
        // given
        HTTPClient client = new HTTPClient(logger, configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        InputStream is = new ByteArrayInputStream("type=mts".getBytes(CHARSET));
        when(connection.getInputStream()).thenReturn(is);
        when(connection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        String data = "type=m";

        // when
        Response response = client.sendRequest(RequestType.BEACON, connection, "127.0.0.1", data.getBytes(), "POST");

        // then
        assertThat(response, is(notNullValue()));
        assertThat(response.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendStatusRequestDoesNotReturnNull() {
        // given
        HTTPClient target = spy(new HTTPClient(logger, configuration));
        doReturn(null).when(target).sendRequest(Mockito.any(RequestType.class), anyString(), anyString(), Mockito.any(byte[].class), anyString());

        // when
        StatusResponse obtained = target.sendStatusRequest();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendNewSessionRequestDoesNotReturnNull() {
        // given
        HTTPClient target = spy(new HTTPClient(logger, configuration));
        doReturn(null).when(target).sendRequest(Mockito.any(RequestType.class), anyString(), anyString(), Mockito.any(byte[].class), anyString());

        // when
        StatusResponse obtained = target.sendNewSessionRequest();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendBeaconRequestDoesNotReturnNull() throws UnsupportedEncodingException {
        // given
        HTTPClient target = spy(new HTTPClient(logger, configuration));
        doReturn(null).when(target).sendRequest(Mockito.any(RequestType.class), anyString(), anyString(), Mockito.any(byte[].class), anyString());

        // when
        StatusResponse obtained = target.sendBeaconRequest("127.0.0.1", "".getBytes(CHARSET));

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    public void sendTimeSyncRequestDoesNotReturnNull() {
        // given
        HTTPClient target = spy(new HTTPClient(logger, configuration));
        doReturn(null).when(target).sendRequest(Mockito.any(RequestType.class), anyString(), anyString(), Mockito.any(byte[].class), anyString());

        // when
        TimeSyncResponse obtained = target.sendTimeSyncRequest();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getResponseCode(), is(equalTo(Integer.MAX_VALUE)));
    }
}
