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

package com.welab.wefe.board.service.service.checkpoint;

import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 */
@Service
public class GatewayInternetCheckpoint extends AbstractCheckpoint {
    @Autowired
    protected GlobalConfigService globalConfigService;

    @Override
    public ServiceType service() {
        return ServiceType.GatewayService;
    }

    @Override
    public String desc() {
        return "检查 board 与 gateway 服务在公网的连通性";
    }

    @Override
    public String getConfigValue() {
        MemberInfoModel memberInfo = globalConfigService.getMemberInfo();
        if (memberInfo == null) {
            return null;
        }
        return memberInfo.getMemberGatewayUri();
    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return "请在[全局设置]-[成员设置]中对 gateway 的对外通信地址进行设置";
    }

    @Override
    protected void doCheck(String value) throws Exception {

    }
}
