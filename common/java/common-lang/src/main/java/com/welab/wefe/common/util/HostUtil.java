/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.regex.Pattern;

/**
 * @author Jervis
 **/
public class HostUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HostUtil.class);

    private static final Pattern PATTERN_MATCH_IP = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
    public static final String UNKNOWN = "unknown";

    /**
     * Get native IP (V4)
     */
    public static String getLocalIp() {

        String[] names = new String[]{"eth0", "en0"};

        for (String name : names) {
            NetworkInterface networkInterface = null;
            try {
                networkInterface = NetworkInterface.getByName(name);
            } catch (SocketException e) {
                continue;
            }
            if (networkInterface == null) {
                continue;
            }

            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                String ip = address.getAddress().getHostAddress();
                if (PATTERN_MATCH_IP.matcher(ip).matches()) {
                    return ip;
                }
            }
        }

        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostName();
            if (PATTERN_MATCH_IP.matcher(ip).matches()) {
                return ip;
            }
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        }

        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            if (PATTERN_MATCH_IP.matcher(ip).matches()) {
                return ip;
            }
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        }
        return UNKNOWN;
    }
}
