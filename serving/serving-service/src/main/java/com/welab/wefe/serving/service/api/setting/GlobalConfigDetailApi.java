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

package com.welab.wefe.serving.service.api.setting;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.GlobalConfigMysqlModel;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.serving.service.utils.ServiceUtil;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zane
 */
@Api(path = "global_config/detail", name = "get global settings", login = false)
public class GlobalConfigDetailApi extends AbstractApi<GlobalConfigDetailApi.Input, Map<String, JSONObject>> {


    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<Map<String, JSONObject>> handle(Input input) throws StatusCodeWithException {

        Map<String, JSONObject> output = new HashMap<>();

        for (String group : input.groups) {
            List<GlobalConfigMysqlModel> list = globalConfigService.list(group);
            JSONObject json = new JSONObject();
            list.forEach(x ->
            {
                if(!x.getName().equals("rsa_public_key") && !x.getName().equals("rsa_private_key")) {
                    json.put(x.getName(), x.getValue());   
                }
                else {
                    json.put(x.getName(), ServiceUtil.around(x.getValue(), 10, 10));
                }
            });
            output.put(group, json);
        }

        return success(output);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "配置项的组名", require = true)
        public List<String> groups;
    }
}
