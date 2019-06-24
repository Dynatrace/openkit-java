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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
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
    private static final String DEVICE_ID = "deviceID";
    private static final String APP_NAME = "appName";

    private Logger logger;
    private Configuration configuration;
    private TimingProvider timingProvider;
    private ThreadIDProvider threadIDProvider;
    private BeaconCacheImpl beaconCache;
    private BeaconSender beaconSender;
    private BeaconCacheEvictor beaconCacheEvictor;

    @Before
    public void setUp() {

        logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isInfoEnabled()).thenReturn(true);

        configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn(APP_ID);
        when(configuration.getDeviceID()).thenReturn(DEVICE_ID);
        when(configuration.getApplicationName()).thenReturn(APP_NAME);
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.isCapture()).thenReturn(true);
        when(configuration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration());
        when(configuration.getPrivacyConfiguration()).thenReturn(
            new PrivacyConfiguration(PrivacyConfiguration.DEFAULT_DATA_COLLECTION_LEVEL, PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL));

        timingProvider = mock(TimingProvider.class);
        threadIDProvider = mock(ThreadIDProvider.class);
        beaconCache = mock(BeaconCacheImpl.class);
        beaconSender = mock(BeaconSender.class);
        beaconCacheEvictor = mock(BeaconCacheEvictor.class);
    }

    @Test
    public void initializeStartsTheBeaconCacheEvictor() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

        // when
        target.initialize();

        // then
        verify(beaconCacheEvictor, times(1)).start();
        verifyNoMoreInteractions(beaconCacheEvictor);
    }

    @Test
    public void initializeInitializesBeaconSender() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

        // when
        target.initialize();

        // then
        verify(beaconSender, times(1)).initialize();
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void waitForInitCompletionForwardsTheCallToTheBeaconSender() {
        // given
        when(beaconSender.waitForInit()).thenReturn(false, true);
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
    public void getConfigurationReturnsConfigurationPassedInConstructor() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

        // when
        Configuration obtained = target.getConfiguration();

        // then
        assertThat(obtained, is(sameInstance(configuration)));
    }

    @Test
    public void shutdownStopsTheBeaconCacheEvictor() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

        // when
        target.shutdown();

        // then
        verify(beaconCacheEvictor, times(1)).stop();
        verifyNoMoreInteractions(beaconCacheEvictor);
    }

    @Test
    public void shutdownShutsDownBeaconSender() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

        // when
        target.shutdown();

        // then
        verify(beaconSender, times(1)).shutdown();
        verifyNoMoreInteractions(beaconSender);
    }

    @Test
    public void shutdownClosesAllChildObjects() throws IOException {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
    public void closingChildObjectsCatchesIOExceptiuon() throws IOException {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
    public void createSessionReturnsSessionObject() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

        // when
        Session obtained = target.createSession("127.0.0.1");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, instanceOf(SessionImpl.class));
    }

    @Test
    public void createSessionAddsNewlyCreatedSessionToListOfChildren() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
    public void createSessionAfterShutdownHasBeenCalledReturnsNullSession() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);
        target.shutdown();

        // when
        Session obtained = target.createSession("10.0.0.1");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullSession.class)));
        assertThat(obtained, sameInstance(OpenKitImpl.NULL_SESSION));
    }

    @Test
    public void onChildClosedRemovesArgumentFromListOfChildren() {
        // given
        OpenKitImpl target = new OpenKitImpl(logger, configuration, timingProvider, threadIDProvider, beaconCache, beaconSender, beaconCacheEvictor);

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
}
