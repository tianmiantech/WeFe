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
package com.welab.wefe.common.wefe.checkpoint;

import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.wefe.enums.ServiceType;

/**
 * @author zane
 * @date 2021/12/21
 */
public abstract class AbstractUnionConnectionCheckpoint extends AbstractCheckpoint {
    @Override
    protected ServiceType service() {
        return ServiceType.UnionService;
    }

    @Override
    protected String desc() {
        return "检查与 union 服务的连通性";
    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return "union-service 服务地址为空，请在配置文件 config.properties 中对其进行配置。";
    }

    @Override
    protected void doCheck(String configValue) throws Exception {
        HttpResponse httpResponse = HttpRequest.create(configValue + "/service/alive")
                .closeLog()
                .postJson();

        if (httpResponse.getError() != null) {
            throw httpResponse.getError();
        }
    }
}
