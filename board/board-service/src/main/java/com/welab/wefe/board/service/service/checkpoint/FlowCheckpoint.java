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
package com.welab.wefe.board.service.service.checkpoint;

import com.welab.wefe.board.service.dto.globalconfig.FlowConfigModel;
import com.welab.wefe.board.service.sdk.FlowService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 * @date 2021/12/22
 */
@Service
public class FlowCheckpoint extends AbstractCheckpoint {
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private FlowService flowService;

    @Override
    protected ServiceType service() {
        return ServiceType.FlowService;
    }

    @Override
    protected String desc() {
        return "检查与 flow 服务的连通性";
    }

    @Override
    protected String getConfigValue() {
        FlowConfigModel flowConfig = globalConfigService.getFlowConfig();
        if (flowConfig == null) {
            return null;
        }
        return flowConfig.intranetBaseUri;
    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return "请在[全局设置]-[系统设置]中对 flow 的内网地址进行设置";
    }

    @Override
    protected void doCheck(String value) throws Exception {
        flowService.alive();
    }
}
