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

package com.welab.wefe.common.web.util;

import com.welab.wefe.common.util.HostUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @des Gets the IP address of the request for the provider
 * @author eval
 **/
public class HttpServletRequestUtil {

    private static List<String> IP_HEADERS = Arrays.asList(
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    private static String UNKNOWN = "unknown";

    /**
     * Get HTTP client IP address
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = null;

        // First get from the header
        for (String header : IP_HEADERS) {
            ip = request.getHeader(header);
            if (!isEmptyIp(ip)) {
                break;
            }
        }

        //If the header cannot be retrieved, the remote address will be retrieved
        if (isEmptyIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // In the case of multiple agents, the first IP is the real IP of the client, and multiple IPS are divided according to ','
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }

        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            ip = HostUtil.getLocalIp();
        }

        return ip;
    }

    public static boolean isEmptyIp(String ipAddress) {
        return ipAddress == null
                || ipAddress.length() == 0
                || UNKNOWN.equalsIgnoreCase(ipAddress);
    }
}
