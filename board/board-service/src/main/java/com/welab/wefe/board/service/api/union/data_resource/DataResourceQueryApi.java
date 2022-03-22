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
package com.welab.wefe.board.service.api.union.data_resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author zane
 * @date 2021/12/17
 */
@Api(path = "union/data_resource/query", name = "query data resource from union service")
public class DataResourceQueryApi extends AbstractApi<DataResourceQueryApi.Input, Object> {

    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<Object> handle(Input input) throws StatusCodeWithException, IOException {
        JSONObject result = unionService.request(
                "data_resource/query",
                input.rawRequestParams
        );

        JSONObject data = result.getJSONObject("data");
        if (data != null) {
            JSONArray list = data.getJSONArray("list");
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    JSONObject extraData = item.getJSONObject("extra_data");
                    if (extraData != null) {
                        item.putAll(extraData);
                        item.remove("extra_data");
                    }
                }
            }
        }

        return super.unionApiResultToBoardApiResult(result);
    }

    public static class Input extends AbstractApiInput {

    }
}
