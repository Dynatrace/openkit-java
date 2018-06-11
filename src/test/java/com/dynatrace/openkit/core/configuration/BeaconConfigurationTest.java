package com.dynatrace.openkit.core.configuration;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BeaconConfigurationTest {

    @Test
    public void getMultiplicityReturnsMultiplicitySetInConstructor() {

        // when multiplicity is positive, then
        assertThat(new BeaconConfiguration(4).getMultiplicity(), is(equalTo(4)));
        // when multiplicity is zero, then
        assertThat(new BeaconConfiguration(0).getMultiplicity(), is(equalTo(0)));
        // when multiplicity is negative, then
        assertThat(new BeaconConfiguration(-3).getMultiplicity(), is(equalTo(-3)));
    }

    @Test
    public void capturingIsAllowedWhenMultiplicityIsGreaterThanZero() {

        // when, then
        assertThat(new BeaconConfiguration(1).isCapturingAllowed(), is(true));
        assertThat(new BeaconConfiguration(2).isCapturingAllowed(), is(true));
        assertThat(new BeaconConfiguration(Integer.MAX_VALUE).isCapturingAllowed(), is(true));
    }

    @Test
    public void capturingIsDisallowedWhenMultiplicityIsLessThanOrEqualToZero() {

        // when, then
        assertThat(new BeaconConfiguration(0).isCapturingAllowed(), is(false));
        assertThat(new BeaconConfiguration(-1).isCapturingAllowed(), is(false));
        assertThat(new BeaconConfiguration(Integer.MIN_VALUE).isCapturingAllowed(), is(false));
    }
}
