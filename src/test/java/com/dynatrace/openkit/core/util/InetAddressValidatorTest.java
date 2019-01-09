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

package com.dynatrace.openkit.core.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InetAddressValidatorTest {

    @Test
    public void IPV4AddressIsValid() {
        String validIPV4Address = "122.133.55.22";
        assertThat(InetAddressValidator.isValidIP(validIPV4Address), is(true));
    }

    @Test
    public void IPV4AddressIsInvalidDueToExtraBlock() {
        String invalidIPV4Address = "122.133.55.22.1";
        assertThat(InetAddressValidator.isValidIP(invalidIPV4Address), is(false));
    }

    @Test
    public void IPV4AddressIsInvalidDueToHighNumber() {
        String invalidIPV4Address = "122.133.555.22";
        assertThat(InetAddressValidator.isValidIP(invalidIPV4Address), is(false));
    }

    @Test
    public void IPV4AddressIsInvalidDueToNegativeNumber() {
        String invalidIPV4Address = "122.133.555.-22";
        assertThat(InetAddressValidator.isValidIP(invalidIPV4Address), is(false));
    }

    @Test
    public void IPV4AddressIsInvalidDueToLetter() {
        String invalidIPV4Address = "122.133.555.e33";
        assertThat(InetAddressValidator.isValidIP(invalidIPV4Address), is(false));
    }


    @Test
    public void IPV4AddressIsInvalidDueToMissingBlock() {
        String invalidIPV4Address = "122.133.555";
        assertThat(InetAddressValidator.isValidIP(invalidIPV4Address), is(false));
    }

    @Test
    public void IPV6AddressIsValid() {
        String validIPV6Address = "2045:FEFE:0D22:0123:DAD2:3345:ABB2:0003";
        assertThat(InetAddressValidator.isValidIP(validIPV6Address), is(true));
    }

    @Test
    public void IPV6AddressIsInvalidDueToExtraBlock() {
        String invalidIPV6Address = "2045:FEFE:0D22:0123:DAD2:3345:ABB2:0003:1001";
        assertThat(InetAddressValidator.isValidIP(invalidIPV6Address), is(false));
    }

    @Test
    public void IPV6AddressIsInvalidDueTo5HexDigitNumber() {
        String invalidIPV6Address = "2045:FEFE3:0D22:0123:DAD2:3345:ABB2:0003";
        assertThat(InetAddressValidator.isValidIP(invalidIPV6Address), is(false));
    }

    @Test
    public void IPV6AddressIsInvalidDueToNonHexLetter() {
        String invalidIPV6Address = "2045:GEFE:0D22:0123:DAD2:3345:ABB2:0003";
        assertThat(InetAddressValidator.isValidIP(invalidIPV6Address), is(false));
    }

    @Test
    public void IPV6AddressWithLessThanEightBlocksIsValid() {
        String invalidIPV6Address = "2045:defe:d22:123::1241:1444";
        assertThat(InetAddressValidator.isValidIP(invalidIPV6Address), is(true));
    }

    @Test
    public void IPAddressEmptyIsInvalid() {
        String invalidIPAddress = "";
        assertThat(InetAddressValidator.isValidIP(invalidIPAddress), is(false));
    }

    @Test
    public void IPAddressNullIsInvalid() {
        String invalidIPAddress = null;
        assertThat(InetAddressValidator.isValidIP(invalidIPAddress), is(false));
    }

}
