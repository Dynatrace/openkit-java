package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.WebRequestTracerStringURL;
import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.RootActionImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.providers.DefaultTimingProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BeaconTest {

    private static final String APP_ID = "appID";
    private static final String APP_NAME = "appName";

    private Configuration configuration;
    private ThreadIDProvider threadIDProvider;

    private Logger logger;

    @Before
    public void setUp() {
        configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn(APP_ID);
        when(configuration.getApplicationName()).thenReturn(APP_NAME);
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.isCapture()).thenReturn(true);

        HTTPClientConfiguration mockHTTPClientConfiguration = mock(HTTPClientConfiguration.class);
        when(configuration.getHttpClientConfig()).thenReturn(mockHTTPClientConfiguration);

        threadIDProvider = mock(ThreadIDProvider.class);

        logger = new DefaultLogger(true);
    }


    @Test
    public void canAddUserIdentifyEvent() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String userID = "myTestUser";

        // when
        beacon.identifyUser(userID);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=60&na=" + userID + "&it=0&pa=0&s0=1&t0=0" })));
    }

    @Test
    public void canAddSentBytesToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesSent = 12321;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String.valueOf(bytesSent) })));
    }

    @Test
    public void canAddSentBytesValueZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesSent = 0;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String.valueOf(bytesSent) })));
    }

    @Test
    public void cannotAddSentBytesWithInvalidValueSmallerZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);

        // when
        webRequest.start().setBytesSent(-5).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0" })));
    }

    @Test
    public void canAddReceivedBytesToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesReceived = 12321;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0&br=" + String.valueOf(bytesReceived) })));
    }

    @Test
    public void canAddReceivedBytesValueZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesReceived = 0;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0&br=" + String.valueOf(bytesReceived) })));
    }

    @Test
    public void cannotAddReceivedBytesWithInvalidValueSmallerZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);

        // when
        webRequest.start().setBytesReceived(-1).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0" })));
    }

    @Test
    public void canAddBothSentBytesAndReceivedBytesToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesReceived = 12321;
        int bytesSent = 123;

        // when
        webRequest.start().setBytesSent(bytesSent).setBytesReceived(bytesReceived).stop(); //stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=0&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String.valueOf(bytesSent) + "&br=" + String.valueOf(bytesReceived) })));
    }

    @Test
    public void canAddRootActionIfCaptureIsOn() {
        // given
        when(configuration.isCapture()).thenReturn(true);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        // when
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        beacon.addAction(rootAction);

        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(equalTo(new String[] { "et=1&na=" + actionName + "&it=0&ca=0&pa=0&s0=0&t0=0&s1=0&t1=0" })));
    }

    @Test
    public void cannotAddRootActionIfCaptureIsOff() {
        // given
        when(configuration.isCapture()).thenReturn(false);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        // when
        Beacon beacon = new Beacon(logger, configuration, "127.0.0.1", threadIDProvider, new NullTimeProvider());
        beacon.addAction(rootAction);

        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(arrayWithSize(0)));
    }

    private class NullTimeProvider extends DefaultTimingProvider {

        public long provideTimestampInMilliseconds() {
            return 0;
        }
    }
}
