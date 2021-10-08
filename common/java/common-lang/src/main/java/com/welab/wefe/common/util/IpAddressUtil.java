/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * IP address utility class
 *
 * @author aaron.li
 **/
public class IpAddressUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddressUtil.class);


    /**
     * Check whether the local and remote IP addresses are on the same network segment
     *
     * @param localInetSocketAddress  Local address
     * @param remoteInetSocketAddress The remote address
     * @return True: same network segment. False: Different network segments
     */
    public static boolean isSameNetworkSegment(InetSocketAddress localInetSocketAddress, InetSocketAddress remoteInetSocketAddress) {
        try {
            String localIpAddr = getIpAddress(localInetSocketAddress);
            String remoteIpAddr = getIpAddress(remoteInetSocketAddress);
            String localSubnetMask = getSubnetMask(localInetSocketAddress);
            boolean result = isSameNetworkSegment(localIpAddr, remoteIpAddr, localSubnetMask);
            LOG.info("isSameNetworkSegment, localIpAddr: {}, remoteIpAddr: {}, localSubnetMask: {}, result: {}.", localIpAddr, remoteIpAddr, localSubnetMask, result);
            return result;
        } catch (Exception e) {
            LOG.error("比较本地地址与远程地址是否同一网段异常：", e);
            return false;
        }
    }


    /**
     * Check whether the two IP addresses are on the same network segment
     *
     * @param ip1        IP address 1
     * @param ip2        IP address 2
     * @param subnetMask Subnet mask
     * @return True: same network segment. False: Different network segments
     */
    public static boolean isSameNetworkSegment(String ip1, String ip2, String subnetMask) {
        return getIpAndSubnetMaskLogic(ip1, subnetMask).equals(getIpAndSubnetMaskLogic(ip2, subnetMask));
    }

    /**
     * Obtaining an IP Address
     *
     * @param inetSocketAddress SOCKET Network address object
     * @return The IP address
     */
    public static String getIpAddress(InetSocketAddress inetSocketAddress) {
        if (null == inetSocketAddress) {
            return "";
        }
        return getIpAddress(inetSocketAddress.getAddress());
    }

    /**
     * Obtaining an IP Address
     */
    public static String getIpAddress(InetAddress inetAddress) {
        return inetAddress.getHostAddress();
    }


    /**
     * Gets the logical & result character string for the IP address and subnet mask
     *
     * @param ip         The IP address
     * @param subnetMask Subnet mask
     * @return Logical & result character string for IP address and subnet mask
     */
    private static String getIpAndSubnetMaskLogic(String ip, String subnetMask) {
        StringBuilder result = new StringBuilder();
        String binaryIp = getBinaryIp(ip);
        String binarySubnetMask = getBinaryIp(subnetMask);
        for (int i = 0; i < 32; i++) {
            byte ipByte = Byte.parseByte(String.valueOf(binaryIp.charAt(i)));
            byte subnetMaskByte = Byte.parseByte(String.valueOf(binarySubnetMask.charAt(i)));
            result.append(ipByte & subnetMaskByte);
        }
        return result.toString();
    }

    /**
     * Gets the binary string of IP
     *
     * @param ip IP address or subnet mask
     */
    private static String getBinaryIp(String ip) {
        String[] ipSections = ip.split("\\.");
        String binaryIp = "";
        for (String ipSection : ipSections) {
            long signIp = Long.parseLong(ipSection);
            String binary = Long.toBinaryString(signIp);
            long binaryInt = Long.parseLong(binary);
            binaryIp += String.format("%08d", binaryInt);
        }
        return binaryIp;
    }

    /**
     * Obtain the subnet mask
     *
     * @param inetSocketAddress SOCKET Network address object
     * @return Subnet mask
     */
    private static String getSubnetMask(InetSocketAddress inetSocketAddress) throws SocketException {
        String subnetMask = null;
        try {
            InetAddress inetAddress = inetSocketAddress.getAddress();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            List<InterfaceAddress> list = networkInterface.getInterfaceAddresses();
            if (list.size() > 0) {
                // The number of binary ones in the subnet mask
                int mask = list.get(0).getNetworkPrefixLength();
                StringBuilder maskStr = new StringBuilder();
                int[] maskIp = new int[4];
                for (int i = 0; i < maskIp.length; i++) {
                    maskIp[i] = (mask >= 8) ? 255 : (mask > 0 ? (mask & 0xff) : 0);
                    mask -= 8;
                    maskStr.append(maskIp[i]);
                    if (i < maskIp.length - 1) {
                        maskStr.append(".");
                    }
                }
                subnetMask = maskStr.toString();
            }
        } catch (Exception e) {
            LOG.error("Obtaining the subnet mask failed. Procedure：", e);
            throw e;
        }

        return subnetMask;
    }

    /**
     * Extracting IP Addresses
     * <p>
     * #The following is the IP address whitelist configuration
     * * # In this case, the full matching configuration mode
     * </p>
     *
     * @param str IP address configuration character text, in the format of \n as a newline character, annotated with #, wildcard character *, one IP address in a row
     */
    public static List<String> parseStringToIpList(String str) {
        List<String> list = new ArrayList<>();

        if (StringUtil.isEmpty(str)) {
            return list;
        }

        // Annotation symbols
        String annotationSymbol = "#";

        String splitSymbol = "\n";

        // Split with a newline character
        String[] array = str.split(splitSymbol);
        for (int i = 0; i < array.length; i++) {

            String line = array[i].trim();

            // Skip empty lines and comment lines
            if (StringUtil.isEmpty(line) || line.startsWith(annotationSymbol)) {
                continue;
            }

            // If there are inline comments, cut and remove comments.
            int annotationSymbolIndex = line.indexOf(annotationSymbol);
            if (annotationSymbolIndex > -1) {
                line = line.substring(0, annotationSymbolIndex).trim();
            }

            if (StringUtil.isNotEmpty(line)) {
                list.add(line);
            }
        }

        return list;
    }
}
