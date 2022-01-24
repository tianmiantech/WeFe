/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.gateway.cache;

import com.welab.wefe.common.util.IpAddressUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.dto.GatewayConfigModel;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * IP whitelist cache
 *
 * @author aaron.li
 **/
public class SystemConfigCache {
    private final Logger LOG = LoggerFactory.getLogger(SystemConfigCache.class);

    private static SystemConfigCache cache = new SystemConfigCache();

    /**
     * IP address list
     */
    private static ConcurrentSkipListSet<String> IP_ADDRESS_LIST = new ConcurrentSkipListSet<>();

    private SystemConfigCache() {
    }

    public static SystemConfigCache getInstance() {
        return cache;
    }


    /**
     * Does the IP address exist in the whitelist
     */
    public boolean isExistIp(String ip) {
        if (StringUtil.isEmpty(ip)) {
            return false;
        }
        // wildcard,matching all
        String widCard = "*";
        for (String ipAddr : IP_ADDRESS_LIST) {
            // Starting with a wildcard indicates full matching
            if (ipAddr.startsWith(widCard)) {
                return true;
            }
            int widCardIndex = ipAddr.indexOf(widCard);
            // Wildcard exists
            if (widCardIndex > -1) {
                ipAddr = ipAddr.substring(0, widCardIndex);
                if (ip.startsWith(ipAddr)) {
                    return true;
                }
            } else {
                // The absence of wildcards indicates an accurate match
                if (ipAddr.equals(ip)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the cache is empty
     */
    public boolean cacheIsEmpty() {
        return IP_ADDRESS_LIST.isEmpty();
    }

    /**
     * Refresh cache
     */
    public synchronized boolean refreshCache() {
        try {
            GlobalConfigService service = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
            GatewayConfigModel gatewayConfig = service.getGatewayConfig();
            if (null == gatewayConfig) {
                IP_ADDRESS_LIST.clear();
                return true;
            }
            String gatewayIpWhiteStr = gatewayConfig.ipWhiteList;
            gatewayIpWhiteStr = StringUtil.isEmpty(gatewayIpWhiteStr) ? "" : gatewayIpWhiteStr.trim();

            // Extract IP address list
            List<String> gatewayIpWhiteList = IpAddressUtil.parseStringToIpList(gatewayIpWhiteStr);
            if (CollectionUtils.isEmpty(gatewayIpWhiteList)) {
                IP_ADDRESS_LIST.clear();
                return true;
            }

            List<String> delIpList = new ArrayList<>();
            for (String ip : IP_ADDRESS_LIST) {
                if (!gatewayIpWhiteList.contains(ip)) {
                    delIpList.add(ip);
                }
            }

            IP_ADDRESS_LIST.removeAll(delIpList);
            IP_ADDRESS_LIST.addAll(gatewayIpWhiteList);

            return true;
        } catch (Exception e) {
            LOG.error("Refreshing IP whitelist cache exception ：", e);

        }
        return false;
    }


}
