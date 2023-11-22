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

package com.welab.wefe.board.service.api.global_config;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.GlobalConfigMysqlModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zane
 */
@Api(path = "global_config/get", name = "get system global configs")
public class GetGlobalConfigApi extends AbstractApi<GetGlobalConfigApi.Input, Map<String, JSONObject>> {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<Map<String, JSONObject>> handle(Input input) throws StatusCodeWithException {

        Map<String, JSONObject> output = new HashMap<>();

        for (String group : input.groups) {
            List<GlobalConfigMysqlModel> list = globalConfigService.list(group);
            JSONObject json = new JSONObject();
            list.forEach(x -> json.put(x.getName(), x.getValue()));
            output.put(group, json);
        }

        return success(output);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "配置项的组名", require = true)
        public List<String> groups;
    }

}
