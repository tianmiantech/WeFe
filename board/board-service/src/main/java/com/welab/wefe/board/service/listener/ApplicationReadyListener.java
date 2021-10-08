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

package com.welab.wefe.board.service.listener;

import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.util.HostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * IP address reporting
 *
 * @author aaron.li
 **/
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReadyListener.class);

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private GatewayService gatewayService;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        appendIpAddressToGatewayWhiteList();
    }

    private void appendIpAddressToGatewayWhiteList() {
        try {
            // Intranet IP
            String localIP = HostUtil.getLocalIp();
            globalConfigService.appendIpToWhiteList(
                    localIP,
                    "board 内网IP地址，由 board 自主上报。",
                    true
            );

            // Internet IP
            String internetIP = HostUtil.getOuterNetIpAddress();
            globalConfigService.appendIpToWhiteList(
                    internetIP,
                    "board 外网IP地址，由 board 自主上报。",
                    false
            );

            LOG.info("上报IP地址完成.");
            // Notify the gateway to update the IP whitelist cache
            gatewayService.refreshIpWhiteListCache();
        } catch (Exception e) {
            LOG.error("IP地址上报异常：", e);
        }
    }


}
