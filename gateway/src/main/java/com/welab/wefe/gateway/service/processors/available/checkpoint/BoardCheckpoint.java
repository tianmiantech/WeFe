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
package com.welab.wefe.gateway.service.processors.available.checkpoint;

import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.dto.BoardConfigModel;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.sdk.BoardHelper;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author zane
 * @date 2021/12/20
 */
@Service
public class BoardCheckpoint extends AbstractCheckpoint {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ServiceType service() {
        return null;
    }

    @Override
    protected String desc() {
        return "检查 gateway 与 board-service 服务的连通性";
    }

    @Override
    protected String getConfigValue() {
        BoardConfigModel boardConfig = globalConfigService.getBoardConfig();
        if (boardConfig == null) {
            return null;
        }
        return boardConfig.intranetBaseUri;

    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return "请在[全局设置]-[系统设置]中对 board-service 的后台内网地址进行设置";
    }

    @Override
    protected void doCheck(String value) throws Exception {
        MemberEntity selfMember = MemberCache.getInstance().getSelfMember();

        JObject reqBody = JObject.create()
                .append("callerMemberId", selfMember.getId())
                .append("callerMemberName", selfMember.getName())
                .append("api", "service/alive")
                .append("data", JObject.create());

        HttpResponse httpResponse = BoardHelper.push(
                value + "/gateway/redirect",
                BoardHelper.POST,
                new HashMap<>(0),
                BoardHelper.generateReqParam(reqBody.toString())
        );

        if (httpResponse.getError() != null) {
            throw httpResponse.getError();
        }
    }
}
