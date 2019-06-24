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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InetAddressValidatorTest {

    @Test
    public void ipV4AddressIsValid()
    {
        //given
        String ipv4TestString = "122.133.55.22";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(true));
    }

    @Test
    public void ipV4AddressIsValidAllZero()
    {
        //given
        String ipv4TestString = "0.0.0.0";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(true));
    }

    @Test
    public void ipV4AddressIsValidAllEigtht()
    {
        //given
        String ipv4TestString = "8.8.8.8";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(true));
    }

    @Test
    public void ipV4AddressIsValidHighestPossible()
    {
        //given
        String ipv4TestString = "255.255.255.255";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(true));
    }

    @Test
    public void ipV4AddressIsInvalidBecauseOfOverflow()
    {
        //given
        String ipv4TestString = "255.255.255.256";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDoubleColonsInsteadOfPoints()
    {
        //given
        String ipv4TestString = "255:255:255:255";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDueToAdditionalCharacterInFirstBlock()
    {
        //given
        String ipv4TestString = "122x.133.55.22";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDueToAdditionalCharacterInSecondBlock()
    {
        //given
        String ipv4TestString = "122.133x.55.22";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDueToAdditionalCharacterInThirdBlock()
    {
        //given
        String ipv4TestString = "122.133.55x.22";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDueToAdditionalCharacterInFourthBlock()
    {
        //given
        String ipv4TestString = "122.133.55.22x";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDueToIllegalValueOverrun()
    {
        //given
        String ipv4TestString = "122.133.256.22";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV4AddressIsInvalidDueToIllegalValueNegative()
    {
        //given
        String ipv4TestString = "122.133.256.-22";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv4TestString), is(false));
    }

    @Test
    public void ipV6AddressIsValid()
    {
        //given
        String ipv6TestString = "23fe:33af:1232:5522:abcd:2532:1a2b:1";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressIsInvalidOverflow()
    {
        //given
        String ipv6TestString = "23fec:33af:1232:5522:abcd:2532:1a2b:1";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressIsInvalidIllegalCharacter()
    {
        //given
        String ipv6TestString = "23fl:33af:1232:5522:abcd:2532:1a2b:1";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressIsInvalidTooManyBlocks()
    {
        //given
        String ipv6TestString = "23fl:33af:1232:5522:abcd:2532:1a2b:1:2:3";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressHexCompressedIsValidBlock4()
    {
        //given
        String ipv6TestString = "2001:db:85:b::1A";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressHexCompressedIsValidBlock3()
    {
        //given
        String ipv6TestString = "2001:db:85::b:1A";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressHexCompressedIsValidBlock2()
    {
        //given
        String ipv6TestString = "2001:db::85:b:1A";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressHexCompressedIsValidBlock1()
    {
        //given
        String ipv6TestString = "2001::db:85:b:1A";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressHexCompressedIsValidShortestPossible()
    {
        //given
        String ipv6TestString = "2001::b1A";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressHexCompressedIsInvalidTwoCompressedBlocks()
    {
        //given
        String ipv6TestString = "2001::db:85::b1A";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressHexCompressedIsInvalidFirstBlockMissing()
    {
        //given
        String ipv6TestString = ":4::5:6";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

   @Test
    public void ipV6AddressMixedNotationIsValid_ZerosIPv6NonCompressed()
    {
        //given
        String ipv6TestString = "0:0:0:0:0:0:172.12.55.18";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressMixedNotationIsValid_ZerosIPv6Compressed()
    {
        //given
        String ipv6TestString = "::172.12.55.18";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressMixedNotationIsValid_NonZeroIPv6NonCompressed()
    {
        //given
        String ipv6TestString = "1:2:3:4:5:6:172.12.55.18";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressMixedNotationIsValid_NonZeroIPv6Compressed()
    {
        //given
        String ipv6TestString = "2018:f::172.12.55.18";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressMixedNotationIsInvalidOnly3IPv4Blocks()
    {
        //given
        String ipv6TestString = "0::FF:FF:172.12.55";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressMixedNotationIsValidIPV6PartInvalid()
    {
        //given
        String ipv6TestString = "0::FF::FF:172.12.55.34";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressMixedNotationIsValidIPV6()
    {
        //given
        String ipv6TestString = "0::FF:FF:FF:172.12.55.34";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressMixedNotationIsValidStartingWithDoubleColon()
    {
        //given
        String ipv6TestString = "::FF:FF:172.12.55.43";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressMixedNotationInvalid_Compressed3Colon()
    {
        //given
        String ipv6TestString = "123:::172.12.55.43";

        //then
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(false));
    }

    @Test
    public void ipV6AddressLinkLocalIsValid()
    {
        //given
        String ipv6TestStringLinkLocal = "fe80::208:74ff:feda:625c%5";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestStringLinkLocal), is(true));
    }

    @Test
    public void ipV6AddressLinkLocalIsValidVeryShortLinkLocal()
    {
        //given
        String ipv6TestStringLinkLocal = "fe80::625c%5";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestStringLinkLocal), is(true));
    }

    @Test
    public void ipV6AddressLinkLocalIsInvalidTooManyBlocks()
    {
        //given
        String ipv6TestStringLinkLocal = "fe80:34:208:74ff:feda:dada:625c:8976:abcd%5";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestStringLinkLocal), is(false));
    }

    @Test
    public void ipV6AddressLinkLocalIsInvalidIllegalNonHexCharacter()
    {
        //given
        String ipv6TestStringLinkLocal = "fe80::208t:74ff:feda:dada:625c%5";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestStringLinkLocal), is(false));
    }

    @Test
    public void ipV6AddressLinkLocalIsInvalidDueToTwoDoubleColonsInAddress()
    {
        //given
        String ipv6TestStringLinkLocal = "fe80::208:74ff::dada:625c%5";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestStringLinkLocal), is(false));
    }

    @Test
    public void ipV6AddressLinkLocalIsInvalidZoneIndexUsedWithInvalidPrefix()
    {
        //given
        String ipv6TestStringLinkLocal = "fedd::208:74ff::dada:625c%5";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestStringLinkLocal), is(false));
    }

    // the following two addresses are not valid according to RFC5952 but are accepted by glibc's implementation and also ours

    @Test
    public void ipV6AddressValid_RFCLeadingZeros()
    {
        //given
        String ipv6TestString = "2001:0db8::0001";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressValid_RFCEmptyBlockNotShortened()
    {
        //given
        String ipv6TestString = "2001:db8::0:1";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressValid_RFCExample()
    {
        //given
        String ipv6TestString = "2001:db8::1:0:0:1";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressValid_CharactersOnlyLowerCase()
    {
        //given
        String ipv6TestString = "20ae:db8::1f:4edd:344f:1abc";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressValid_CharactersMixedCase()
    {
        //given
        String ipv6TestString = "20aE:Db8::1f:4EDd:344f:1aBc";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

    @Test
    public void ipV6AddressValid_CharactersUpperCase()
    {
        //given
        String ipv6TestString = "20AE:DB8::1F:4EDD:344F:1ABC";

        //then 
        assertThat(InetAddressValidator.isValidIP(ipv6TestString), is(true));
    }

}
