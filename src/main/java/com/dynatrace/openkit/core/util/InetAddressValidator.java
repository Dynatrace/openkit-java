package com.dynatrace.openkit.core.util;

import java.util.regex.Pattern;

/**
 * THIS FILE HAS BEEN COPIED FROM cdd.gen.ccdd.sdk.agent.shared - ideally it needs to be
 * copied here as part of the build process
 *
 * WARNING: THIS FILE IS COPIED FROM AGENT.SHARED DURING BUILD. DO NOT MODIFY HERE!
 *
 * Utility class for validating an IP address against regular expression patterns.
 *
 * !!!!!!!!!! NOTE !!!!!!!!!!
 * This class is a duplication of com.dynatrace.diagnostics.core.realtime.analyzers.enduser.ClientLocation.InetAddressValidator.
 * Duplication is necessary, otherwise instrumentation of dtserver fails !
 *
 */
public class InetAddressValidator {

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_STD_PATTERN =
            Pattern.compile(
                    "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
            Pattern.compile(
                    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    private static final Pattern IPV6_MIXED_STD_OR_COMPRESSED_PATTERN =
            Pattern.compile(
                    "(?ix)(?<![:.\\w])                                     # Anchor address\n" +
                            "(?:\n" +
                            " (?:[A-F0-9]{1,4}:){6}                                # Non-compressed\n" +
                            "|(?=(?:[A-F0-9]{0,4}:){2,6}                           # Compressed with 2 to 6 colons\n" +
                            "    (?:[0-9]{1,3}\\.){3}[0-9]{1,3}                    #    and 4 bytes\n" +
                            "    (?![:.\\w]))                                      #    and anchored\n" +
                            " (([0-9A-F]{1,4}:){1,5}|:)((:[0-9A-F]{1,4}){1,5}:|:)  #    and at most 1 double colon\n" +
                            "|::(?:[A-F0-9]{1,4}:){5}                              # Compressed with 7 colons and 5 numbers\n" +
                            ")\n" +
                            "(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}  # 255.255.255.\n" +
                            "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])       # 255\n" +
                            "(?![:.\\w])                                           # Anchor address"
            );



    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }


    public static boolean isIPv6HexCompressedAddress(final String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input) || isLinkLocalIPv6WithZoneIndex(input)
                || isIPv6IPv4MappedAddress( input) || isIPv6MixedAddress( input) ;
    }

    // format x:x:x:x:x:x:d.d.d.d
    // InetAddress.getByName(strAddr); returns an IPv6 Address for this format
    public static boolean isIPv6MixedAddress( final String input ) {
        return IPV6_MIXED_STD_OR_COMPRESSED_PATTERN.matcher(input).matches();
    }

    // format ::ffff:d.d.d.d
    // InetAddress.getByName(strAddr); returns an IPv4 address for this format
    public static boolean isIPv6IPv4MappedAddress( final String input) {
        // InetAddress automatically convert this type of address down to an IPv4 address
        // It always starts '::ffff:' then contains an IPv4 address
        if (input.length()>7 && input.substring(0, 7).equalsIgnoreCase("::ffff:")) {
            // then remove the first seven chars and see if we have an IPv4 address
            String lowerPart = input.substring(7);
            return isIPv4Address( lowerPart);
        }
        return false;
    }

    /**
     * getIPv4AddressFromIPv6MixedAddress( )
     * returns the IPv4 part of an IPv6 address assuming it has already been identified that this is a mixed mode address
     *
     * callers should check that this is a mixed mode IPv6 address with ( isIPv6MixedStdOrCompressedAddress(() ) before making the call
     * even though we test for this in the method
     *
     * @param input
     * @return IPv4 address as a string
     */
    public static String getIPv4AddressFromIPv6MixedAddress( final String input ) {
        if ( isIPv6MixedAddress( input ) ) {
            // pull out the IPv4 address from the end of the string
            // find last colon
            int i = input.lastIndexOf(":");
            if (i != -1) {
                // it must find the colon as the regEx test passed
                return input.substring( i + 1 );
            }
        }
        return "";
    }


    /**
     * Check if <code>input</code> is a link local IPv6 addresses starting with "fe80:" and containing
     * a zone index with "%xxx". The zone index will not be checked.
     *
     * @param input ip-address to check
     * @return true if address part is in correct IPv6 notation.
     * @author cwat-plang
     */
    public static boolean isLinkLocalIPv6WithZoneIndex(String input) {
        if (input.length()>5 && input.substring(0, 5).equalsIgnoreCase("fe80:")) {
            int lastIndex = input.lastIndexOf("%");
            if (lastIndex > 0 && lastIndex < (input.length() - 1)) { // input may not start with the zone separator
                String ipPart = input.substring(0, lastIndex);
                return isIPv6StdAddress(ipPart) || isIPv6HexCompressedAddress(ipPart);
            }
        }
        return false;
    }

    /**
     * Returns the long value of the IPv4 address.
     * Returns -1 if the string did not contain a valid IPv4.
     *
     * @param ipAddress
     * @return
     * @author richard.vogl
     */
    public final static long parseIPv4Address(String ipAddress){
        if (ipAddress == null || ipAddress.length() < 1){
            return -1;
        }
        if (ipAddress.endsWith(".")){
            return -1;
        }

        String[] parts = ipAddress.split( "\\." );

        if ( parts.length != 4 ){
            return -1;
        }

        long value = 0;
        long mult = 0x1000000;
        for (int j = 0; j < parts.length; j++) {
            String s = parts[j];

            int i;
            try {
                i = Integer.parseInt( s );
            }catch (NumberFormatException e){
                return -1;
            }

            if ( (i < 0) || (i > 255) ){
                return -1;
            }
            value += i*mult;
            mult /= 0x100;
        }

        return value;
    }

    /**
     * Returns true, if <code>ipAddress</code> is either a valid IPv4 or IPv6 address.
     *
     * @param ipAddress
     * @return
     * @author clemens.fuchs
     */
    public static boolean isValidIP(String ipAddress) {
        if(ipAddress == null || ipAddress.length() == 0) {
            return false;
        }

        return isIPv4Address(ipAddress) || isIPv6Address(ipAddress);
    }
}
