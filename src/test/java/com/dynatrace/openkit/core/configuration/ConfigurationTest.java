package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.test.providers.TestSessionIDProvider;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationTest {

    @Test
    public void aDefaultConstructedConfigurationDisablesCapturing() {

        // given
        TestConfiguration target = new TestConfiguration();

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void enableAndDisableCapturing() {

        // given
        TestConfiguration target = new TestConfiguration();

        // when capturing is enabled
        target.enableCapture();

        // then
        assertThat(target.isCapture(), is(true));

        // and when capturing is disabled again
        target.disableCapture();

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void capturingIsDisabledIfStatusResponseIsNull() {

        // given
        TestConfiguration target = new TestConfiguration();
        target.enableCapture();

        // when status response to handle is null
        target.updateSettings(null);

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void capturingIsDisabledIfResponseCodeIndicatesFailures() {

        // given
        TestConfiguration target = new TestConfiguration();
        target.enableCapture();

        StatusResponse response = mock(StatusResponse.class);
        when(response.isCapture()).thenReturn(true);
        when(response.getResponseCode()).thenReturn(400);

        // when status response indicates erroneous response
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void capturingIsEnabledFromStatusResponse() {
        // given
        TestConfiguration target = new TestConfiguration();
        target.disableCapture();

        StatusResponse response = mock(StatusResponse.class);
        when(response.isCapture()).thenReturn(true);
        when(response.getResponseCode()).thenReturn(200);

        // when capturing is enabled in status response
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(true));
    }

    @Test
    public void capturingIsDisabledFromStatusResponse() {
        // given
        TestConfiguration target = new TestConfiguration();
        target.enableCapture();

        StatusResponse response = mock(StatusResponse.class);
        when(response.isCapture()).thenReturn(false);
        when(response.getResponseCode()).thenReturn(200);

        // when capturing is disabled in status response
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(false));
    }

    private final class TestConfiguration extends Configuration {

        private TestConfiguration() {
            this(OpenKitType.DYNATRACE, "", "", 42, "");
        }

        private TestConfiguration(OpenKitType openKitType, String applicationName, String applicationID, long deviceID, String endpointURL) {
            super(openKitType, applicationName, applicationID, deviceID, endpointURL, new TestSessionIDProvider(), new SSLStrictTrustManager(), new Device("", "", ""), "");
        }
    }
}
