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

package com.welab.wefe.board.service.service.available.checkpoint;

import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 */
@Service
public class GatewayIntranetCheckpoint extends AbstractCheckpoint {

    @Autowired
    protected GlobalConfigService globalConfigService;

    @Override
    public ServiceType service() {
        return ServiceType.GatewayService;
    }

    @Override
    public String desc() {
        return "检查 board 与 gateway 服务在内网的连通性";
    }

    @Override
    public String value() {
        return globalConfigService.getGatewayConfig().intranetBaseUri;
    }

    @Override
    protected void doCheck(String value) throws Exception {
        GatewayService gatewayService = Launcher.getBean(GatewayService.class);

        // Since the gateway does not currently have an alive interface,
        // temporarily adjust a method to test the connectivity between the board and the gateway.
        gatewayService.refreshMemberBlacklistCache();

    }
}
