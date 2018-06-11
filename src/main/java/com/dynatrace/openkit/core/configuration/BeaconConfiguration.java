package com.dynatrace.openkit.core.configuration;

/**
 * Configuration for a Beacon.
 *
 * <p>
 *     Note: This class shall be immutable.
 *     It is perfectly valid to exchange the configuration over time.
 * </p>
 */
public class BeaconConfiguration {

    /**
     * Multiplicity as received from the server.
     */
    private final int multiplicity;

    public BeaconConfiguration(int multiplicity) {
        this.multiplicity = multiplicity;
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public boolean isCapturingAllowed() {
        return multiplicity > 0;
    }
}
