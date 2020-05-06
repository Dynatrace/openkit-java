/**
 * Copyright 2018-2019 Dynatrace LLC
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
package com.dynatrace.openkit.protocol;

/**
 * Defines a response received from the server.
 */
public interface ResponseAttributes {

    /**
     * Returns the maximum POST body size when sending beacons.
     */
    int getMaxBeaconSizeInBytes();

    /**
     * Returns the maximum duration of a session in milliseconds after which a session will be split.
     */
    int getMaxSessionDurationInMilliseconds();

    /**
     * Returns the maximum number of top level actions after which a session will be split.
     */
    int getMaxEventsPerSession();

    /**
     * Returns the idle timeout in milliseconds after which a session will be split.
     */
    int getSessionTimeoutInMilliseconds();

    /**
     * Returns the send interval in milliseconds.
     */
    int getSendIntervalInMilliseconds();

    /**
     * Returns the version of the visit store to be used.
     */
    int getVisitStoreVersion();

    /**
     * Indicator whether capturing data is generally allowed or not.
     */
    boolean isCapture();

    /**
     * Indicator whether crashes should be captured or not.
     */
    boolean isCaptureCrashes();

    /**
     * Indicator whether errors should be captured or not.
     */
    boolean isCaptureErrors();

    /**
     * Returns the ID of the application to which this configuration applies.
     *
     * <p>
     *     This is sent by the JSON configuration only, as sanity check to fix
     *     a weird Jetty bug.
     * </p>
     */
    String getApplicationId();

    /**
     * Returns the multiplicity
     */
    int getMultiplicity();

    /**
     * Returns the ID of the server to where all data should be sent.
     */
    int getServerId();

    /**
     * Returns the status of the new session configuration request.
     */
    String getStatus();

    /**
     * Returns the timestamp of these attributes which were returned from the server.
     *
     * <p>
     *     The timestamp is the duration from January, 1st, 1970
     * </p>
     */
    long getTimestampInMilliseconds();

    /**
     * Checks whether the given attribute was set / sent from the server with this server response.
     *
     * @param attribute the attribute to be checked if it was sent by the server.
     * @return {@code true} if the given attribute was sent from the server with this attributes, {@code false} otherwise.
     */
    boolean isAttributeSet(ResponseAttribute attribute);

    /**
     * Creates a new response attributes object by merging the given response into this one. Single attributes are
     * selectively taken over from the given attributes as long as the respective attribute {@link #isAttributeSet is set}.
     *
     * @param responseAttributes the response attributes which will be merged together with this one into a new response
     *      attributes object.
     * @return a new response attributes instance by merging the given attributes with this attributes.
     */
    ResponseAttributes merge(ResponseAttributes responseAttributes);
}
