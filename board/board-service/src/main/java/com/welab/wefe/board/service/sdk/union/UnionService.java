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

package com.welab.wefe.board.service.sdk.union;


import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.BloomFilterOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.sdk.AbstractUnionService;
import com.welab.wefe.board.service.sdk.union.dto.MemberBaseInfo;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceAvailableCheckOutput;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DataSetPublicLevel;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;


/**
 * @author Zane
 */
@Service
public class UnionService extends AbstractUnionService {

    public LinkedHashMap<String, MemberBaseInfo> getMemberMap() throws StatusCodeWithException {
        JSONObject result = request("member/map");
        JSONObject data = result.getJSONObject("data");

        LinkedHashMap<String, MemberBaseInfo> map = new LinkedHashMap<>();
        for (String memberId : data.keySet()) {
            map.put(
                    memberId,
                    data.getJSONObject(memberId).toJavaObject(MemberBaseInfo.class)
            );
        }

        return map;
    }

    public ServiceAvailableCheckOutput getAvailable() throws StatusCodeWithException {
        JSONObject result = request("service/available");

        return result
                .getJSONObject("data")
                .toJavaObject(ServiceAvailableCheckOutput.class);
    }

    /**
     * 更新资源信息，使用此接口更新时，数据不会立即更新，有延迟。
     */
    public void lazyUpdateDataResource(DataResourceMysqlModel model) throws StatusCodeWithException {
        MemberInfoModel member = globalConfigService.getMemberInfo();
        if (!member.getMemberAllowPublicDataSet() || member.getMemberHidden()) {
            return;
        }

        CommonThreadPool.run(() -> {
            try {
                JObject params = JObject
                        .create(model)
                        .append("data_resource_id", model.getId())
                        // union 目前用的 data_set_id 为主键，但这是不科学的，这里临时迁就。
                        .append("data_set_id", model.getId());

                request("data_resource/lazy_update", params);
            } catch (StatusCodeWithException e) {
                super.log(e);
            }
        });

    }

    public void upsertDataResource(DataResourceMysqlModel model) {
        JObject params = JObject.create(model)
                .append("data_resource_id", model.getId());

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
                    doNotPublicDataSet(model);
                    return;
                }

                request(StringUtil.stringToUnderLineLowerCase(model.getDataResourceType().name()) + "/put", params);
            } catch (StatusCodeWithException e) {
                super.log(e);
            }
        });

    }

    /**
     * Hidden data set
     */
    public void doNotPublicDataSet(DataResourceMysqlModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("data_resource_id", model.getId());

        request("data_resource/delete", params);
    }


    public <OUT> OUT getDataResourceDetail(String dataResourceId, Class<OUT> outputClass) throws StatusCodeWithException {
        DataResourceType type = null;
        if (outputClass == ImageDataSetOutputModel.class) {
            type = DataResourceType.ImageDataSet;
        } else if (outputClass == TableDataSetOutputModel.class) {
            type = DataResourceType.TableDataSet;
        } else if (outputClass == BloomFilterOutputModel.class) {
            type = DataResourceType.BloomFilter;
        }
        return getDataResourceDetail(dataResourceId, type, outputClass);
    }

    /**
     * 获取数据资源详情
     */
    public <OUT> OUT getDataResourceDetail(String dataResourceId, DataResourceType dataResourceType, Class<OUT> outputClass) throws StatusCodeWithException {
        String key = dataResourceId + "getDataResourceDetail" + outputClass.getSimpleName();
        if (CACHE_MAP.containsKey(key)) {
            return (OUT) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create()
                .put("data_resource_id", dataResourceId)
                .put("data_resource_type", dataResourceType);
        JSONObject result = request("data_resource/detail", params);

        JSONObject data = result.getJSONObject("data");
        if (data == null || data.isEmpty()) {
            return null;
        }

        JSONObject extraData = data.getJSONObject("extra_data");
        if (extraData != null) {
            data.putAll(extraData);
            data.remove("extra_data");
        }

        OUT output = data.toJavaObject(outputClass);

        CACHE_MAP.put(key, output);
        return output;
    }
}
