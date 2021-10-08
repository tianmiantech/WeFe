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

import com.welab.wefe.common.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.regex.Matcher;
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

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return UNKNOWN;
        }
    }

    /**
     * Obtain the external IP address of the local host
     */
    public static String getOuterNetIpAddress() {
        String netIp = null;
        boolean getSuccess = isIpAddr(netIp = getIpAddressFromSohu())
                || isIpAddr(netIp = getIpAddressFromIfConfigCo())
                || isIpAddr(netIp = getIpAddressFromIpInfo())
                || isIpAddr(netIp = getIpAddressFromIpCn())
                || isIpAddr(netIp = getIpAddressFromTaobao());

        return netIp;
    }

    /**
     * Get the IP address from Sohu service
     * Address response format: var returnCitySN = {"cip": "183.3.218.18", "CID ": "440113"," Cname ": "Panyu District, Guangzhou City, Guangdong Province "};
     */
    private static String getIpAddressFromSohu() {
        String url = "http://pv.sohu.com/cityjson?ie=utf-8";
        String responseStr = HttpRequest.create(url).get().getBodyAsString();
        if (StringUtil.isEmpty(responseStr)) {
            return null;
        }
        String[] responseArray = responseStr.split("=");
        if (responseArray.length < 2) {
            return null;
        }
        try {
            JObject data = JObject.create(responseArray[1].trim().replace(";", ""));
            return data.getString("cip");
        } catch (Exception e) {
            LOG.error("从Sohu服务中获取外网IP地址失败.", e);
        }
        return null;
    }

    private static final Pattern PATTERN = Pattern.compile("\\{\\S+}");

    /**
     * Obtain IP address from Taobao service
     * Address response format：ipCallback({ip:"183.3.218.18"})
     */
    private static String getIpAddressFromTaobao() {
        String url = "http://www.taobao.com/help/getip.php";
        String responseStr = HttpRequest.create(url).get().getBodyAsString();
        if (StringUtil.isEmpty(responseStr)) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(responseStr);
        String ipStr = null;
        while (matcher.find()) {
            ipStr = matcher.group();
            break;
        }
        try {
            JObject ipJson = JObject.create(ipStr);
            return ipJson.getString("ip");
        } catch (Exception e) {
            LOG.error("Failed to obtain the external IP address from taobao service. Procedure.", e);
        }
        return null;
    }

    /**
     * Get the IP address from the IP service
     * Address the response format: {" rs ": 1," code ": 0," address ":" shenzhen city, guangdong province, China telecom ", "IP" : "183.3.218.18", "isDomain" : 0}
     */
    private static String getIpAddressFromIpCn() {
        String url = "https://www.ip.cn/api/index?ip=&type=0";
        String responseStr = HttpRequest.create(url).get().getBodyAsString();
        if (StringUtil.isEmpty(responseStr)) {
            return null;
        }

        try {
            JObject data = JObject.create(responseStr);
            return data.getString("ip");
        } catch (Exception e) {
            LOG.error("从ip cn服务中获取外网IP地址失败.", e);
        }

        return null;
    }

    /**
     * Get the IP address from the ipinfo service
     * Address response format: 183.3.218.18
     */
    private static String getIpAddressFromIpInfo() {
        String url = "http://ipinfo.io/ip";
        return HttpRequest.create(url).get().getBodyAsString();
    }

    /**
     * Get the IP address from the IfconfigCo service
     * Address response format: 183.3.218.18
     */
    private static String getIpAddressFromIfConfigCo() {
        String url = "https://ifconfig.co/ip";
        return HttpRequest.create(url).get().getBodyAsString();
    }

    /**
     * Simply check if it is in IP address format (because there is no other unknown format returned by third-party APIS)
     */
    private static boolean isIpAddr(String str) {
        return PATTERN_MATCH_IP.matcher(str).matches();
    }
}
