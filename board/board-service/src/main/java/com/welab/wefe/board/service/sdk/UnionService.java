/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.sdk;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.union.data_set.DataSetTagListApi;
import com.welab.wefe.board.service.api.union.data_set.DefaultTagListApi;
import com.welab.wefe.board.service.api.union.data_set.QueryDataSetApi;
import com.welab.wefe.board.service.api.union.image_data_set.QueryImageDataSetApi;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import org.springframework.stereotype.Service;


/**
 * @author Zane
 */
@Service
public class UnionService extends AbstractUnionService {
    public void updateImageDataSetLabelInfo(ImageDataSetMysqlModel dataSet) {
        // TODO: Zane 待补充
    }


    public void updateDataResourceBaseInfo(DataResourceMysqlModel model) throws StatusCodeWithException {
        // TODO: Zane 待补充
    }

    public void uploadDataResource(DataResourceMysqlModel model) {
        JObject params = JObject
                .create(model);

        MemberInfoModel member = globalConfigService.getMemberInfo();
        // If data exposure is prohibited globally, it will not be reported.
        if (!member.getMemberAllowPublicDataSet() || member.getMemberHidden()) {
            return;
        }

        CommonThreadPool.run(() -> {
            try {
                // If this data set is not publicly available to anyone
                if (model.getPublicLevel() == DataSetPublicLevel.OnlyMyself) {
                    // Notify union to remove the data set
                    dontPublicDataSet(model);
                    return;
                }

                request("data_resource/put", params);
            } catch (StatusCodeWithException e) {
                super.log(e);
            }
        });

    }

    /**
     * Hidden data set
     */
    public void dontPublicDataSet(DataResourceMysqlModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getId());

        String api = "data_resource/delete";
        request(api, params);
    }


    public JSONObject queryImageDataSetTags() throws StatusCodeWithException {
        String key = "queryImageDataSetTags";
        if (CACHE_MAP.containsKey(key)) {
            return (JSONObject) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create();

        JSONObject response = request("image_data_set/tags/query", params);
        CACHE_MAP.put(key, response);
        return response;
    }

    /**
     * Paging query data set tag
     */
    public JSONObject queryDataSetTags(DataSetTagListApi.Input input) throws StatusCodeWithException {
        String key = "queryDataSetTags" + JSON.toJSONString(input);
        if (CACHE_MAP.containsKey(key)) {
            return (JSONObject) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create()
                .put("page_index", input.getPageIndex())
                .put("page_size", input.getPageSize())
                .put("tag_name", input.getTag());

        JSONObject response = request("data_set/tags/query", params);
        CACHE_MAP.put(key, response);
        return response;
    }

    /**
     * Pagination query default tags
     */
    public JSONObject queryTags(DefaultTagListApi.Input input) throws StatusCodeWithException {

        String key = "queryTags" + JSON.toJSONString(input);
        if (CACHE_MAP.containsKey(key)) {
            return (JSONObject) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create()
                .put("page_index", input.getPageIndex())
                .put("page_size", input.getPageSize());

        JSONObject response = request("default_tag/query", params);
        CACHE_MAP.put(key, response);
        return response;
    }


    /**
     * Paging query data set
     */
    public JSONObject queryDataSets(QueryDataSetApi.Input input) throws StatusCodeWithException {
        JObject data = JObject.create()
                .put("id", input.getDataSetId())
                .put("data_set_id", input.getDataSetId());

        return request("data_set/query", data);
    }

    public JSONObject queryImageDataSets(QueryImageDataSetApi.Input input) throws StatusCodeWithException {
        return request("image_data_set/query", JObject.create(input));
    }

    /**
     * 获取数据资源详情
     */
    public <OUT extends DataResourceOutputModel> OUT getDataResourceDetail(String dataResourceId, Class<OUT> outputClass) throws StatusCodeWithException {
        String key = dataResourceId + "getDataResourceDetail";
        if (CACHE_MAP.containsKey(key)) {
            return (OUT) CACHE_MAP.get(key);
        }

        JObject params = JObject.create("id", dataResourceId);
        JSONObject result = request("data_resource/detail", params);

        JSONObject data = result.getJSONObject("data");
        if (data == null || data.isEmpty()) {
            return null;
        }

        OUT output = data.toJavaObject(outputClass);

        CACHE_MAP.put(key, output);
        return output;
    }
}
