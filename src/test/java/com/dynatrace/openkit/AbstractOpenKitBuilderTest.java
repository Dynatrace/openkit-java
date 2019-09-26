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

package com.dynatrace.openkit;

import com.dynatrace.openkit.api.LogLevel;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.ConfigurationDefaults;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class AbstractOpenKitBuilderTest {

    private static final String ENDPOINT_URL = "https://www.google.at";
    private static final String DEVICE_ID = "777";
    private static final String APPLICATION_VERSION = "application-version";
    private static final String OPERATING_SYSTEM = "ultimate-operating-system";
    private static final String MANUFACTURER = "ACME Inc.";
    private static final String MODEL_ID = "the-model-identifier";
    private static final long MAX_RECORD_AGE_IN_MILLIS = TimeUnit.MINUTES.toMillis(42);
    private static final long LOWER_MEMORY_BOUNDARY_IN_BYTES = 777L;
    private static final long UPPER_MEMORY_BOUNDARY_IN_BYTES = 7777L;

    @Test
    public void constructorInitializesEndpointURL() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // then
        assertThat(target.getEndpointURL(), is(equalTo(ENDPOINT_URL)));
    }

    @Test
    public void constructorInitializesDeviceID() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // then
        assertThat(target.getDeviceID(), is(equalTo(DEVICE_ID)));
    }

    @Test
    public void getLoggerGivesADefaultImplementationIfNoneHasBeenProvided() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(DefaultLogger.class));
    }

    @Test
    public void defaultLoggerHasErrorAndWarningLevelEnabled() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isErrorEnabled(), is(true));
        assertThat(obtained.isWarnEnabled(), is(true));
        assertThat(obtained.isInfoEnabled(), is(false));
        assertThat(obtained.isDebugEnabled(), is(false));
    }

    @Test
    public void whenEnablingVerboseAllLogLevelsAreEnabledInDefaultLogger() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.enableVerbose();
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isErrorEnabled(), is(true));
        assertThat(obtained.isWarnEnabled(), is(true));
        assertThat(obtained.isInfoEnabled(), is(true));
        assertThat(obtained.isDebugEnabled(), is(true));
    }

    @Test
    public void withLogLevelAppliesLogLevelToDefaultLogger() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withLogLevel(LogLevel.INFO);
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isErrorEnabled(), is(true));
        assertThat(obtained.isWarnEnabled(), is(true));
        assertThat(obtained.isInfoEnabled(), is(true));
        assertThat(obtained.isDebugEnabled(), is(false));
    }

    @Test
    public void withLogLevelOnlyAcceptsNonNullValues() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withLogLevel(null);
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isErrorEnabled(), is(true));
        assertThat(obtained.isWarnEnabled(), is(true));
        assertThat(obtained.isInfoEnabled(), is(false));
        assertThat(obtained.isDebugEnabled(), is(false));
    }

    @Test
    public void getLoggerUsesPreviouslySetLogger() {
        // given
        Logger logger = mock(Logger.class);
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withLogger(logger);
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(sameInstance(logger)));
    }

    @Test
    public void getApplicationVersionUsesDefaultIfNotSet() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // then
        assertThat(target.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
    }

    @Test
    public void applicationVersionCanBeChanged() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withApplicationVersion(APPLICATION_VERSION);

        // then
        assertThat(target.getApplicationVersion(), is(equalTo(APPLICATION_VERSION)));
    }

    @Test
    public void applicationVersionCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withApplicationVersion(null);

        // then
        assertThat(target.getApplicationVersion(), is(notNullValue()));
        assertThat(target.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
    }

    @Test
    public void applicationVersionCannotBeChangedToEmptyString() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withApplicationVersion("");

        // then
        assertThat(target.getApplicationVersion(), not(isEmptyString()));
        assertThat(target.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
    }

    @Test
    public void getTrustManagerGivesStrictTrustManagerByDefault() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        SSLTrustManager obtained = target.getTrustManager();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(SSLStrictTrustManager.class));
    }

    @Test
    public void getTrustManagerGivesPreviouslySetTrustManager() {
        // given
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withTrustManager(trustManager);
        SSLTrustManager obtained = target.getTrustManager();

        // then
        assertThat(obtained, is(sameInstance(trustManager)));
    }

    @Test
    public void trustManagerCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withTrustManager(null);
        SSLTrustManager obtained = target.getTrustManager();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(SSLStrictTrustManager.class));
    }

    @Test
    public void getOperatingSystemReturnsADefaultValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, not(isEmptyOrNullString()));
        assertThat(obtained, is(equalTo(OpenKitConstants.DEFAULT_OPERATING_SYSTEM)));
    }

    @Test
    public void getOperatingSystemGivesChangedOperatingSystem() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withOperatingSystem(OPERATING_SYSTEM);
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, is(equalTo(OPERATING_SYSTEM)));
    }

    @Test
    public void operatingSystemCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withOperatingSystem(null);
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void operatingSystemCannotBeChangedToEmptyString() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withOperatingSystem("");
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, not(isEmptyString()));
    }

    @Test
    public void getManufacturerReturnsADefaultValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, not(isEmptyOrNullString()));
        assertThat(obtained, is(equalTo(OpenKitConstants.DEFAULT_MANUFACTURER)));
    }

    @Test
    public void getManufacturerGivesChangedOperatingSystem() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withManufacturer(MANUFACTURER);
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, is(equalTo(MANUFACTURER)));
    }

    @Test
    public void manufacturerCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withManufacturer(null);
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void manufacturerCannotBeChangedToEmptyString() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withManufacturer("");
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, not(isEmptyString()));
    }

    @Test
    public void getModelIDReturnsADefaultValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        String obtained = target.getModelID();

        // then
        assertThat(obtained, not(isEmptyOrNullString()));
        assertThat(obtained, is(equalTo(OpenKitConstants.DEFAULT_MODEL_ID)));
    }

    @Test
    public void getModelIDGivesChangedOperatingSystem() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withModelID(MODEL_ID);
        String obtained = target.getModelID();

        // then
        assertThat(obtained, is(equalTo(MODEL_ID)));
    }

    @Test
    public void modelIDCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withModelID(null);
        String obtained = target.getModelID();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void modelIDCannotBeChangedToEmptyString() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withModelID("");
        String obtained = target.getModelID();

        // then
        assertThat(obtained, not(isEmptyString()));
    }

    @Test
    public void getBeaconCacheMaxRecordAgeReturnsADefaultValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        long obtained = target.getBeaconCacheMaxRecordAge();

        // then
        assertThat(obtained, is(equalTo(ConfigurationDefaults.DEFAULT_MAX_RECORD_AGE_IN_MILLIS)));
    }

    @Test
    public void getBeaconCacheMaxRecordAgeGivesChangedValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withBeaconCacheMaxRecordAge(MAX_RECORD_AGE_IN_MILLIS);
        long obtained = target.getBeaconCacheMaxRecordAge();

        // then
        assertThat(obtained, is(equalTo(MAX_RECORD_AGE_IN_MILLIS)));
    }

    @Test
    public void getBeaconCacheLowerMemoryBoundaryReturnsADefaultValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        long obtained = target.getBeaconCacheLowerMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(ConfigurationDefaults.DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void getBeaconCacheLowerMemoryBoundaryGivesChangedValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withBeaconCacheLowerMemoryBoundary(LOWER_MEMORY_BOUNDARY_IN_BYTES);
        long obtained = target.getBeaconCacheLowerMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(LOWER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void getBeaconCacheLowerUpperBoundaryReturnsADefaultValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        long obtained = target.getBeaconCacheUpperMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(ConfigurationDefaults.DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void getBeaconCacheUpperMemoryBoundaryGivesChangedValue() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withBeaconCacheUpperMemoryBoundary(UPPER_MEMORY_BOUNDARY_IN_BYTES);
        long obtained = target.getBeaconCacheUpperMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(UPPER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void defaultDataCollectionLevelIsUserBehavior() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        DataCollectionLevel obtained = target.getDataCollectionLevel();

        // then
        assertThat(obtained, is(equalTo(DataCollectionLevel.USER_BEHAVIOR)));
    }

    @Test
    public void getDataCollectionLevelReturnsChangedDataCollectionLevel() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withDataCollectionLevel(DataCollectionLevel.PERFORMANCE);
        DataCollectionLevel obtained = target.getDataCollectionLevel();

        // then
        assertThat(obtained, is(equalTo(DataCollectionLevel.PERFORMANCE)));
    }

    @Test
    public void dataCollectionLevelCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withDataCollectionLevel(null);
        DataCollectionLevel obtained = target.getDataCollectionLevel();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void defaultCrashReportingLevelIsOptInCrashes() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        CrashReportingLevel obtained = target.getCrashReportLevel();

        // then
        assertThat(obtained, is(equalTo(CrashReportingLevel.OPT_IN_CRASHES)));
    }

    @Test
    public void getCrashReportLevelReturnsChangedCrashReportingLevel() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withCrashReportingLevel(CrashReportingLevel.OPT_OUT_CRASHES);
        CrashReportingLevel obtained = target.getCrashReportLevel();

        // then
        assertThat(obtained, is(equalTo(CrashReportingLevel.OPT_OUT_CRASHES)));
    }

    @Test
    public void crashReportingLevelCannotBeChangedToNull() {
        // given
        AbstractOpenKitBuilder target = new StubOpenKitBuilder(ENDPOINT_URL, DEVICE_ID);

        // when
        target.withCrashReportingLevel(null);
        CrashReportingLevel obtained = target.getCrashReportLevel();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    /**
     * Stub class for testing purposes only.
     */
    private static final class StubOpenKitBuilder extends AbstractOpenKitBuilder {
        StubOpenKitBuilder(String endpointURL, String deviceID) {
            super(endpointURL, deviceID);
        }

        @Override
        public String getOpenKitType() {
            return null;
        }

        @Override
        public String getApplicationID() {
            return null;
        }

        @Override
        public String getApplicationName() {
            return null;
        }

        @Override
        public int getDefaultServerID() {
            return 0;
        }
    }
}
