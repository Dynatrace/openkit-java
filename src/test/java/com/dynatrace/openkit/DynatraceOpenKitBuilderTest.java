/**
 * Copyright 2018-2021 Dynatrace LLC
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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.concurrent.TimeUnit;

import com.dynatrace.openkit.api.LogLevel;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.api.http.HttpRequestInterceptor;
import com.dynatrace.openkit.api.http.HttpResponseInterceptor;
import com.dynatrace.openkit.core.configuration.ConfigurationDefaults;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.core.util.StringUtil;
import com.dynatrace.openkit.protocol.http.NullHttpRequestInterceptor;
import com.dynatrace.openkit.protocol.http.NullHttpResponseInterceptor;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;

public class DynatraceOpenKitBuilderTest {

    private static final String ENDPOINT_URL = "https://www.google.at";
    private static final String APPLICATION_ID = "the-application-identifier";
    private static final String APPLICATION_NAME = "the-application-name";
    private static final long DEVICE_ID = 777;
    private static final String APPLICATION_VERSION = "application-version";
    private static final String OPERATING_SYSTEM = "ultimate-operating-system";
    private static final String MANUFACTURER = "ACME Inc.";
    private static final String MODEL_ID = "the-model-identifier";
    private static final long MAX_RECORD_AGE_IN_MILLIS = TimeUnit.MINUTES.toMillis(42);
    private static final long LOWER_MEMORY_BOUNDARY_IN_BYTES = 777L;
    private static final long UPPER_MEMORY_BOUNDARY_IN_BYTES = 7777L;

    @Test
    public void constructorInitializesApplicationID() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getApplicationID(), is(equalTo(APPLICATION_ID)));
    }

    @Test
    public void constructorInitializesDeviceIdString() {
        // given, when
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, String.valueOf(DEVICE_ID));

        // then
        assertThat(target.getDeviceID(), is(DEVICE_ID));
        assertThat(target.getOrigDeviceID(), is(String.valueOf(DEVICE_ID)));
    }

    @Test
    public void constructorInitializesEndpointURL() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getEndpointURL(), is(equalTo(ENDPOINT_URL)));
    }

    @Test
    public void constructorInitializesDeviceID() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getDeviceID(), is(equalTo(DEVICE_ID)));
    }

    @Test
    public void constructorInitializesAndHashesDeviceIdString() {
        // given
        final String deviceIdAsString = "stringDeviceID";
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, deviceIdAsString);

        // when, then
        long hashedDeviceId = StringUtil.to64BitHash(deviceIdAsString);
        assertThat(target.getDeviceID(), is(hashedDeviceId));
        assertThat(target.getOrigDeviceID(), is(deviceIdAsString));
    }

    @Test
    public void constructorInitializesNumericDeviceIdString() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, String.valueOf(DEVICE_ID));

        // when, then
        Assert.assertThat(target.getDeviceID(), is(DEVICE_ID));
        Assert.assertThat(target.getOrigDeviceID(), is(String.valueOf(DEVICE_ID)));
    }

    @Test
    public void constructorTrimsDeviceIdString() {
        // given
        final String deviceIdString = " 42 ";
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, deviceIdString);

        // when, then
        assertThat(target.getDeviceID(), is(42L));
        assertThat(target.getOrigDeviceID(), is(deviceIdString));
    }

    @Test
    public void getOpenKitTypeGivesAppropriateValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getOpenKitType(), is(equalTo(DynatraceOpenKitBuilder.OPENKIT_TYPE)));
    }

    @Test
    public void getDefaultServerIDGivesAppropriateValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getDefaultServerID(), is(equalTo(DynatraceOpenKitBuilder.DEFAULT_SERVER_ID)));
    }

    @Test
    public void defaultApplicationNameIsNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getApplicationName(), is(nullValue()));
    }

    @Test
    public void getApplicationNameGivesPreviouslySetApplicationName() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);
        target.withApplicationName(APPLICATION_NAME);

        // then
        assertThat(target.getApplicationName(), is(equalTo(APPLICATION_NAME)));
    }

    @Test
    public void withApplicationNameAllowsOverwritingWithNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);
        target.withApplicationName(APPLICATION_NAME); // initialize with non-null value

        // when
        target.withApplicationName(null);

        // then
        assertThat(target.getApplicationName(), is(nullValue()));
    }

    @Test
    public void getLoggerGivesADefaultImplementationIfNoneHasBeenProvided() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(DefaultLogger.class));
    }

    @Test
    public void defaultLoggerHasErrorAndWarningLevelEnabled() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

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
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

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
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

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
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

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
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withLogger(logger);
        Logger obtained = target.getLogger();

        // then
        assertThat(obtained, is(sameInstance(logger)));
    }

    @Test
    public void getApplicationVersionUsesDefaultIfNotSet() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
    }

    @Test
    public void applicationVersionCanBeChanged() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withApplicationVersion(APPLICATION_VERSION);

        // then
        assertThat(target.getApplicationVersion(), is(equalTo(APPLICATION_VERSION)));
    }

    @Test
    public void applicationVersionCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withApplicationVersion(null);

        // then
        assertThat(target.getApplicationVersion(), is(notNullValue()));
        assertThat(target.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
    }

    @Test
    public void applicationVersionCannotBeChangedToEmptyString() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withApplicationVersion("");

        // then
        assertThat(target.getApplicationVersion(), not(isEmptyString()));
        assertThat(target.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
    }

    @Test
    public void getTrustManagerGivesStrictTrustManagerByDefault() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

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
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withTrustManager(trustManager);
        SSLTrustManager obtained = target.getTrustManager();

        // then
        assertThat(obtained, is(sameInstance(trustManager)));
    }

    @Test
    public void trustManagerCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

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
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, not(isEmptyOrNullString()));
        assertThat(obtained, is(equalTo(OpenKitConstants.DEFAULT_OPERATING_SYSTEM)));
    }

    @Test
    public void getOperatingSystemGivesChangedOperatingSystem() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withOperatingSystem(OPERATING_SYSTEM);
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, is(equalTo(OPERATING_SYSTEM)));
    }

    @Test
    public void operatingSystemCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withOperatingSystem(null);
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void operatingSystemCannotBeChangedToEmptyString() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withOperatingSystem("");
        String obtained = target.getOperatingSystem();

        // then
        assertThat(obtained, not(isEmptyString()));
    }

    @Test
    public void getManufacturerReturnsADefaultValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, not(isEmptyOrNullString()));
        assertThat(obtained, is(equalTo(OpenKitConstants.DEFAULT_MANUFACTURER)));
    }

    @Test
    public void getManufacturerGivesChangedManufacturer() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withManufacturer(MANUFACTURER);
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, is(equalTo(MANUFACTURER)));
    }

    @Test
    public void manufacturerCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withManufacturer(null);
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void manufacturerCannotBeChangedToEmptyString() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withManufacturer("");
        String obtained = target.getManufacturer();

        // then
        assertThat(obtained, not(isEmptyString()));
    }

    @Test
    public void getModelIDReturnsADefaultValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        String obtained = target.getModelID();

        // then
        assertThat(obtained, not(isEmptyOrNullString()));
        assertThat(obtained, is(equalTo(OpenKitConstants.DEFAULT_MODEL_ID)));
    }

    @Test
    public void getModelIDGivesChangedModelID() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withModelID(MODEL_ID);
        String obtained = target.getModelID();

        // then
        assertThat(obtained, is(equalTo(MODEL_ID)));
    }

    @Test
    public void modelIDCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withModelID(null);
        String obtained = target.getModelID();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void modelIDCannotBeChangedToEmptyString() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withModelID("");
        String obtained = target.getModelID();

        // then
        assertThat(obtained, not(isEmptyString()));
    }

    @Test
    public void getBeaconCacheMaxRecordAgeReturnsADefaultValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        long obtained = target.getBeaconCacheMaxRecordAge();

        // then
        assertThat(obtained, is(equalTo(ConfigurationDefaults.DEFAULT_MAX_RECORD_AGE_IN_MILLIS)));
    }

    @Test
    public void getBeaconCacheMaxRecordAgeGivesChangedValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withBeaconCacheMaxRecordAge(MAX_RECORD_AGE_IN_MILLIS);
        long obtained = target.getBeaconCacheMaxRecordAge();

        // then
        assertThat(obtained, is(equalTo(MAX_RECORD_AGE_IN_MILLIS)));
    }

    @Test
    public void getBeaconCacheLowerMemoryBoundaryReturnsADefaultValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        long obtained = target.getBeaconCacheLowerMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(ConfigurationDefaults.DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void getBeaconCacheLowerMemoryBoundaryGivesChangedValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withBeaconCacheLowerMemoryBoundary(LOWER_MEMORY_BOUNDARY_IN_BYTES);
        long obtained = target.getBeaconCacheLowerMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(LOWER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void getBeaconCacheUpperMemoryBoundaryReturnsADefaultValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        long obtained = target.getBeaconCacheUpperMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(ConfigurationDefaults.DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void getBeaconCacheUpperMemoryBoundaryGivesChangedValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withBeaconCacheUpperMemoryBoundary(UPPER_MEMORY_BOUNDARY_IN_BYTES);
        long obtained = target.getBeaconCacheUpperMemoryBoundary();

        // then
        assertThat(obtained, is(equalTo(UPPER_MEMORY_BOUNDARY_IN_BYTES)));
    }

    @Test
    public void defaultDataCollectionLevelIsUserBehavior() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        DataCollectionLevel obtained = target.getDataCollectionLevel();

        // then
        assertThat(obtained, is(equalTo(DataCollectionLevel.USER_BEHAVIOR)));
    }

    @Test
    public void getDataCollectionLevelReturnsChangedDataCollectionLevel() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withDataCollectionLevel(DataCollectionLevel.PERFORMANCE);
        DataCollectionLevel obtained = target.getDataCollectionLevel();

        // then
        assertThat(obtained, is(equalTo(DataCollectionLevel.PERFORMANCE)));
    }

    @Test
    public void dataCollectionLevelCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withDataCollectionLevel(null);
        DataCollectionLevel obtained = target.getDataCollectionLevel();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void defaultCrashReportingLevelIsOptInCrashes() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        CrashReportingLevel obtained = target.getCrashReportLevel();

        // then
        assertThat(obtained, is(equalTo(CrashReportingLevel.OPT_IN_CRASHES)));
    }

    @Test
    public void getCrashReportLevelReturnsChangedCrashReportingLevel() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withCrashReportingLevel(CrashReportingLevel.OPT_OUT_CRASHES);
        CrashReportingLevel obtained = target.getCrashReportLevel();

        // then
        assertThat(obtained, is(equalTo(CrashReportingLevel.OPT_OUT_CRASHES)));
    }

    @Test
    public void crashReportingLevelCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withCrashReportingLevel(null);
        CrashReportingLevel obtained = target.getCrashReportLevel();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void getHttpRequestInterceptorGivesNullHttpRequestInterceptorByDefault() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        HttpRequestInterceptor obtained = target.getHttpRequestInterceptor();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(NullHttpRequestInterceptor.class));
    }

    @Test
    public void getHttpRequestInterceptorGivesPreviouslySetHttpRequestInterceptor() {
        // given
        HttpRequestInterceptor httpRequestInterceptor = mock(HttpRequestInterceptor.class);
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withHttpRequestInterceptor(httpRequestInterceptor);
        HttpRequestInterceptor obtained = target.getHttpRequestInterceptor();

        // then
        assertThat(obtained, is(sameInstance(httpRequestInterceptor)));
    }

    @Test
    public void httpRequestInterceptorCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withHttpRequestInterceptor(null);
        HttpRequestInterceptor obtained = target.getHttpRequestInterceptor();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(NullHttpRequestInterceptor.class));
    }

    @Test
    public void getHttpResponseInterceptorGivesNullHttpResponseInterceptorByDefault() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        HttpResponseInterceptor obtained = target.getHttpResponseInterceptor();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(NullHttpResponseInterceptor.class));
    }

    @Test
    public void getHttpResponseInterceptorGivesPreviouslySetHttpResponseInterceptor() {
        // given
        HttpResponseInterceptor httpResponseInterceptor = mock(HttpResponseInterceptor.class);
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withHttpResponseInterceptor(httpResponseInterceptor);
        HttpResponseInterceptor obtained = target.getHttpResponseInterceptor();

        // then
        assertThat(obtained, is(sameInstance(httpResponseInterceptor)));
    }

    @Test
    public void httpResponseInterceptorCannotBeChangedToNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // when
        target.withHttpResponseInterceptor(null);
        HttpResponseInterceptor obtained = target.getHttpResponseInterceptor();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(NullHttpResponseInterceptor.class));
    }
}
