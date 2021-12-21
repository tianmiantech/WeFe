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

import java.util.HashMap;

/**
 * @author zane
 * @date 2021/12/20
 */
public class BoardCheckpoint extends AbstractCheckpoint {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ServiceType service() {
        return null;
    }

    @Override
    protected String desc() {
        return null;
    }

    @Override
    protected String value() {
        BoardConfigModel boardConfig = globalConfigService.getBoardConfig();
        if (boardConfig == null) {
            return null;
        }
        return boardConfig.intranetBaseUri;

    }

    @Override
    protected void doCheck() throws Exception {
        MemberEntity selfMember = MemberCache.getInstance().getSelfMember();
        String boardBaseUrl = selfMember.getBoardUri();

        JObject reqBody = JObject.create()
                .append("callerMemberId", selfMember.getId())
                .append("api", "server/alive")
                .append("data", JObject.create());

        HttpResponse httpResponse = BoardHelper.push(
                boardBaseUrl + "/gateway/redirect",
                BoardHelper.POST,
                new HashMap<>(0),
                BoardHelper.generateReqParam(reqBody.toString())
        );

        if (httpResponse.getError() != null) {
            throw httpResponse.getError();
        }
    }
}
