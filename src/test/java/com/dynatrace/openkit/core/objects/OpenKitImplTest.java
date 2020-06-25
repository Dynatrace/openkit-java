/**
 * Copyright 2018-2020 Dynatrace LLC
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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.SessionWatchdog;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.ConfigurationDefaults;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link OpenKitImpl} implementation having some knowledge of the sessions.
 */
@SuppressWarnings("resource")
public class OpenKitImplTest {

    private static final String APP_ID = "appID";
    private static final long DEVICE_ID = 1234;
    private static final String APP_NAME = "appName";

    private Logger logger;
    private PrivacyConfiguration privacyConfiguration;
    private OpenKitConfiguration openKitConfiguration;
    private TimingProvider timingProvider;
    private ThreadIDProvider threadIdProvider;
    private SessionIDProvider sessionIdProvider;
    private BeaconCacheImpl beaconCache;
    private BeaconSender beaconSender;
    private BeaconCacheEvictor beaconCacheEvictor;
    private SessionWatchdog sessionWatchdog;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isInfoEnabled()).thenReturn(true);

        privacyConfiguration = mock(PrivacyConfiguration.class);
        when(privacyConfiguration.getDataCollectionLevel()).thenReturn(ConfigurationDefaults.DEFAULT_DATA_COLLECTION_LEVEL);
        when(privacyConfiguration.getCrashReportingLevel()).thenReturn(ConfigurationDefaults.DEFAULT_CRASH_REPORTING_LEVEL);

        openKitConfiguration = mock(OpenKitConfiguration.class);
        when(openKitConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(openKitConfiguration.getDeviceID()).thenReturn(DEVICE_ID);
        when(openKitConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(openKitConfiguration.getOperatingSystem()).thenReturn("");
        when(openKitConfiguration.getManufacturer()).thenReturn("");
        when(openKitConfiguration.getModelID()).thenReturn("");

        timingProvider = mock(TimingProvider.class);
        threadIdProvider = mock(ThreadIDProvider.class);
        sessionIdProvider = mock(SessionIDProvider.class);
        beaconCache = mock(BeaconCacheImpl.class);
        beaconSender = mock(BeaconSender.class);
        beaconCacheEvictor = mock(BeaconCacheEvictor.class);
        sessionWatchdog = mock(SessionWatchdog.class);
    }

    @Test
    public void initializeStartsTheBeaconCacheEvictor() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        target.initialize();

        // then
        verify(beaconCacheEvictor, times(1)).start();
        verifyNoMoreInteractions(beaconCacheEvictor);
    }

    @Test
    public void initializeInitializesBeaconSender() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        target.initialize();

        // then
        verify(beaconSender, times(1)).initialize();
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void initializeInitializesSessionWatchdog() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        target.initialize();

        // then
        verify(sessionWatchdog, times(1)).initialize();
        verifyNoMoreInteractions(sessionWatchdog);
    }

    @Test
    public void waitForInitCompletionForwardsTheCallToTheBeaconSender() {
        // given
        when(beaconSender.waitForInit()).thenReturn(false, true);
        OpenKitImpl target = createOpenKit().build();

        // when called first time
        boolean obtained = target.waitForInitCompletion();

        // then
        assertThat(obtained, is(false));

        // when called second time
        obtained = target.waitForInitCompletion();

        // then
        assertThat(obtained, is(true));

        verify(beaconSender, times(2)).waitForInit();
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void waitForInitCompletionWithTimeoutForwardsTheCallToTheBeaconSender() {
        // given
        when(beaconSender.waitForInit(anyInt())).thenReturn(false, true);
        OpenKitImpl target = createOpenKit().build();

        // when called first time
        boolean obtained = target.waitForInitCompletion(100L);

        // then
        assertThat(obtained, is(false));

        // when called second time
        obtained = target.waitForInitCompletion(200L);

        // then
        assertThat(obtained, is(true));

        verify(beaconSender, times(1)).waitForInit(100L);
        verify(beaconSender, times(1)).waitForInit(200L);
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void isInitializedForwardsCallToTheBeaconSender() {
        // given
        when(beaconSender.isInitialized()).thenReturn(false, true);
        OpenKitImpl target = createOpenKit().build();

        // when called first time
        boolean obtained = target.isInitialized();

        // then
        assertThat(obtained, is(false));

        // when called second time
        obtained = target.isInitialized();

        // then
        assertThat(obtained, is(true));

        verify(beaconSender, times(2)).isInitialized();
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void shutdownStopsTheBeaconCacheEvictor() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        target.shutdown();

        // then
        verify(beaconCacheEvictor, times(1)).stop();
        verifyNoMoreInteractions(beaconCacheEvictor);
    }

    @Test
    public void shutdownShutsDownBeaconSender() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        target.shutdown();

        // then
        verify(beaconSender, times(1)).shutdown();
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void shutdownShutsDownSessionWatchdog() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        target.shutdown();

        // then
        verify(sessionWatchdog, times(1)).shutdown();
        verifyNoMoreInteractions(sessionWatchdog);
    }

    @Test
    public void shutdownClosesAllChildObjects() throws IOException {
        // given
        OpenKitImpl target = createOpenKit().build();

        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.shutdown();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();
    }

    @Test
    public void closeCallsShutdown() {
        // given
        OpenKitImpl target = spy(createOpenKit().build());

        // when
        target.close();

        // then
        verify(target, times(1)).shutdown();
    }

    @Test
    public void closingChildObjectsCatchesIOException() throws IOException {
        // given
        OpenKitImpl target = createOpenKit().build();

        IOException exception = new IOException("oops");
        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectOne).close();
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectTwo).close();
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.shutdown();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();
    }

    @Test
    public void callingShutdownASecondTimeReturnsImmediately() throws IOException {
        // given
        OpenKitImpl target = createOpenKit().build();

        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when calling shutdown two times
        target.shutdown();
        target.shutdown();

        // then the all invocations only happen once
        verify(beaconCacheEvictor, times(1)).stop();
        verify(beaconSender, times(1)).shutdown();
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();

        verifyNoMoreInteractions(beaconCacheEvictor, beaconSender, childObjectOne, childObjectTwo);
    }

    @Test
    public void createSessionReturnsSessionProxyObject() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        Session obtained = target.createSession("127.0.0.1");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(SessionProxyImpl.class));
    }

    @Test
    public void createSessionWithoutIpAddressReturnsSessionProxyObject() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when
        Session obtained = target.createSession();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(SessionProxyImpl.class));
    }

    @Test
    public void createSessionAddsNewlyCreatedSessionToListOfChildren() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when first session is created
        Session sessionOne = target.createSession("127.0.0.1");

        // then
        assertThat(sessionOne, is(notNullValue()));
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject)sessionOne))));

        // when second session is created
        Session sessionTwo = target.createSession("192.168.0.1");

        // then
        assertThat(sessionTwo, is(notNullValue()));
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Arrays.asList((OpenKitObject) sessionOne, (OpenKitObject) sessionTwo))));
    }

    @Test
    public void createSessionWithoutIpAddsNewlyCreatedSessionToListOfChildren() {
        // given
        OpenKitImpl target = createOpenKit().build();

        // when first session is created
        Session sessionOne = target.createSession();

        // then
        assertThat(sessionOne, is(notNullValue()));
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject)sessionOne))));

        // when second session is created
        Session sessionTwo = target.createSession();

        // then
        assertThat(sessionTwo, is(notNullValue()));
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Arrays.asList((OpenKitObject) sessionOne, (OpenKitObject) sessionTwo))));
    }

    @Test
    public void createSessionAfterShutdownHasBeenCalledReturnsNullSession() {
        // given
        OpenKitImpl target = createOpenKit().build();
        target.shutdown();

        // when
        Session obtained = target.createSession("10.0.0.1");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullSession.class)));
        assertThat((NullSession)obtained, sameInstance(NullSession.INSTANCE));
    }

    @Test
    public void createSessionWithoutIpAfterShutdownHasBeenCalledReturnsNullSession() {
        // given
        OpenKitImpl target = createOpenKit().build();
        target.shutdown();

        // when
        Session obtained = target.createSession();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullSession.class)));
        assertThat((NullSession)obtained, sameInstance(NullSession.INSTANCE));
    }

    @Test
    public void onChildClosedRemovesArgumentFromListOfChildren() {
        // given
        OpenKitImpl target = createOpenKit().build();

        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when first child is removed
        target.onChildClosed(childObjectOne);
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList(childObjectTwo))));

        // when second child is removed
        target.onChildClosed(childObjectTwo);
        assertThat(target.getCopyOfChildObjects(), is(empty()));
    }

    private OpenKitImplBuilder createOpenKit() {
        OpenKitImplBuilder builder = new OpenKitImplBuilder();
        builder.logger = logger;
        builder.privacyConfiguration = privacyConfiguration;
        builder.openKitConfiguration = openKitConfiguration;
        builder.threadIdProvider = threadIdProvider;
        builder.timingProvider = timingProvider;
        builder.sessionIdProvider = sessionIdProvider;
        builder.beaconCache = beaconCache;
        builder.beaconSender = beaconSender;
        builder.beaconCacheEvictor = beaconCacheEvictor;
        builder.sessionWatchdog = sessionWatchdog;

        return builder;
    }

    private static class OpenKitImplBuilder {
        private Logger logger;
        private PrivacyConfiguration privacyConfiguration;
        private OpenKitConfiguration openKitConfiguration;
        private ThreadIDProvider threadIdProvider;
        private TimingProvider timingProvider;
        private SessionIDProvider sessionIdProvider;
        private BeaconCache beaconCache;
        private BeaconSender beaconSender;
        private BeaconCacheEvictor beaconCacheEvictor;
        private SessionWatchdog sessionWatchdog;

        private OpenKitImplBuilder with(PrivacyConfiguration privacyConfiguration) {
            this.privacyConfiguration = privacyConfiguration;
            return this;
        }

        private OpenKitImplBuilder with(OpenKitConfiguration openKitConfiguration) {
            this.openKitConfiguration = openKitConfiguration;
            return this;
        }

        private OpenKitImpl build() {
            OpenKitInitializer initializer = mock(OpenKitInitializer.class);
            when(initializer.getLogger()).thenReturn(logger);
            when(initializer.getPrivacyConfiguration()).thenReturn(privacyConfiguration);
            when(initializer.getOpenKitConfiguration()).thenReturn(openKitConfiguration);
            when(initializer.getTimingProvider()).thenReturn(timingProvider);
            when(initializer.getThreadIdProvider()).thenReturn(threadIdProvider);
            when(initializer.getSessionIdProvider()).thenReturn(sessionIdProvider);
            when(initializer.getBeaconCache()).thenReturn(beaconCache);
            when(initializer.getBeaconCacheEvictor()).thenReturn(beaconCacheEvictor);
            when(initializer.getBeaconSender()).thenReturn(beaconSender);
            when(initializer.getSessionWatchdog()).thenReturn(sessionWatchdog);

            return new OpenKitImpl(initializer);
        }
    }
}
