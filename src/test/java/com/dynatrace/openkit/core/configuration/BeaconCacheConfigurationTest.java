/**
 * Copyright 2018-2020 Dynatrace LLC
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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeaconCacheConfigurationTest {

    @Test
    public void beaconCacheConfigFromNullReturnsNull() {
        // given, when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(null);

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void positiveMaxOrderAgeIsTakenOverFromOpenKitBuilder() {
        // given
        long maxRecordAge = 73;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheMaxRecordAge()).thenReturn(maxRecordAge);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheMaxRecordAge();
        assertThat(obtained.getMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void negativeMaxOrderAgeIsTakenOverFromOpenKitBuilder() {
        // given
        long maxRecordAge = -73;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheMaxRecordAge()).thenReturn(maxRecordAge);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheMaxRecordAge();
        assertThat(obtained.getMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void zeroMaxOrderAgeIsTakenOverFromOpenKitBuilder() {
        // given
        long maxRecordAge = 0;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheMaxRecordAge()).thenReturn(maxRecordAge);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheMaxRecordAge();
        assertThat(obtained.getMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void positiveLowerCacheSizeBoundIsTakenOverFromOpenKitBuilder() {
        // given
        long lowerBound = 73;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheLowerMemoryBoundary()).thenReturn(lowerBound);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheLowerMemoryBoundary();
        assertThat(obtained.getCacheSizeLowerBound(), is(lowerBound));
    }

    @Test
    public void negativeLowerCacheSizeBoundIsTakenOverFromOpenKitBuilder() {
        // given
        long lowerBound = -73;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheLowerMemoryBoundary()).thenReturn(lowerBound);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheLowerMemoryBoundary();
        assertThat(obtained.getCacheSizeLowerBound(), is(lowerBound));
    }

    @Test
    public void zeroLowerCacheSizeBoundIsTakenOverFromOpenKitBuilder() {
        // given
        long lowerBound = 0;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheLowerMemoryBoundary()).thenReturn(lowerBound);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheLowerMemoryBoundary();
        assertThat(obtained.getCacheSizeLowerBound(), is(lowerBound));
    }

    @Test
    public void positiveUpperCacheSizeBoundIsTakenOverFromOpenKitBuilder() {
        // given
        long upperBound = 73;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheUpperMemoryBoundary()).thenReturn(upperBound);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheUpperMemoryBoundary();
        assertThat(obtained.getCacheSizeUpperBound(), is(upperBound));
    }

    @Test
    public void negativeUpperCacheSizeBoundIsTakenOverFromOpenKitBuilder() {
        // given
        long upperBound = -73;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheUpperMemoryBoundary()).thenReturn(upperBound);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheUpperMemoryBoundary();
        assertThat(obtained.getCacheSizeUpperBound(), is(upperBound));
    }

    @Test
    public void zeroUpperCacheSizeBoundIsTakenOverFromOpenKitBuilder() {
        // given
        long upperBound = 0;
        AbstractOpenKitBuilder builder = mock(AbstractOpenKitBuilder.class);
        when(builder.getBeaconCacheUpperMemoryBoundary()).thenReturn(upperBound);

        // when
        BeaconCacheConfiguration obtained = BeaconCacheConfiguration.from(builder);

        // then
        verify(builder, times(1)).getBeaconCacheUpperMemoryBoundary();
        assertThat(obtained.getCacheSizeUpperBound(), is(upperBound));
    }
}
