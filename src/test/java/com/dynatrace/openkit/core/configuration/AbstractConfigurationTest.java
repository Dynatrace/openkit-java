package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractConfigurationTest {

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

        // when status response to handle is null
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

        // when status response to handle is null
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

        // when status response to handle is null
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(false));
    }

    private final class TestConfiguration extends AbstractConfiguration {

        private TestConfiguration() {
            this(OpenKitType.DYNATRACE, "", "" , 42, "", true);
        }

        private TestConfiguration(OpenKitType openKitType, String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
            super(openKitType, applicationName, applicationID, visitorID, endpointURL, verbose);
            setHttpClientConfiguration(mock(HTTPClientConfiguration.class));
        }

        @Override
        protected String createBaseURL(String endpointURL, String monitorName) {
            return "https://www.dynatrace.com/";
        }
    }
}
