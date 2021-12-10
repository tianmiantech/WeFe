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
import com.welab.wefe.board.service.database.entity.data_set.AbstractDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.AbstractDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.dto.entity.data_set.AbstractDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.data_set.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.data_set.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DataResourceType;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.stereotype.Service;


/**
 * @author Zane
 */
@Service
public class UnionService extends AbstractUnionService {
    public void updateImageDataSetLabelInfo(ImageDataSetMysqlModel dataSet) {
        // TODO: Zane 待补充
    }


    private void uploadDataSet(AbstractDataSetMysqlModel model, JObject params) throws StatusCodeWithException {
        MemberInfoModel member = globalConfigService.getMemberInfo();
        // If data exposure is prohibited globally, it will not be reported.
        if (!member.getMemberAllowPublicDataSet() || member.getMemberHidden()) {
            return;
        }

        // If this data set is not publicly available to anyone
        if (model.getPublicLevel() == DataSetPublicLevel.OnlyMyself) {
            // Notify union to remove the data set
            dontPublicDataSet(model);
            return;
        }


        CommonThreadPool.run(() -> {

            String api = null;
            if (model instanceof ImageDataSetMysqlModel) {
                api = "image_data_set/put";
            } else if (model instanceof DataSetMysqlModel) {
                api = "data_set/put";
            }

            try {
                request(api, params);
            } catch (StatusCodeWithException e) {
                super.log(e);
            }
        });

    }

    public void uploadImageDataSet(ImageDataSetMysqlModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create(model)
                .put("data_set_id", model.getId());

        uploadDataSet(model, params);
    }

    /**
     * Report data set information
     */
    public void uploadTableDataSet(DataSetMysqlModel model) throws StatusCodeWithException {

        JObject params = JObject
                .create(model)
                .put("data_set_id", model.getId());

        uploadDataSet(model, params);

    }

    /**
     * Hidden data set
     */
    public void dontPublicDataSet(AbstractDataSetMysqlModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getId())
                .put("data_set_id", model.getId());


        String api = null;
        if (model instanceof ImageDataSetMysqlModel) {
            api = "image_data_set/delete";
        } else if (model instanceof DataSetMysqlModel) {
            api = "data_set/delete";
        }

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

    public JSONObject queryMemberById(String id) throws StatusCodeWithException {
        return queryMember(id, "");
    }

    public JSONObject queryMember(String id, String name) throws StatusCodeWithException {
        return queryMemberByPage(0, 0, id, name);
    }

    public JSONObject queryMember(int pageIndex, int pageSize) throws StatusCodeWithException {
        return queryMemberByPage(pageIndex, pageSize, "", "");
    }

    public JSONObject queryMemberByPage(int pageIndex, int pageSize, String id, String name) throws StatusCodeWithException {
        JObject params = JObject.create()
                .put("page_index", pageIndex)
                .put("page_size", pageSize);

        if (StringUtil.isNotEmpty(id)) {
            params.put("id", id);
        }

        if (StringUtil.isNotEmpty(name)) {
            params.put("name", name);
        }

        return request("member/query", params);
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
     * Get details of a single data set
     */
    public ImageDataSetOutputModel getImageDataSetDetail(String id) throws StatusCodeWithException {
        return (ImageDataSetOutputModel) getDataSetDetail(id, DataResourceType.ImageDataSet);
    }

    /**
     * Get details of a single data set
     */
    public TableDataSetOutputModel getTableDataSetDetail(String id) throws StatusCodeWithException {
        return (TableDataSetOutputModel) getDataSetDetail(id, DataResourceType.TableDataSet);
    }

    /**
     * Get details of a single data set
     */
    public AbstractDataSetOutputModel getDataSetDetail(String id, DataResourceType dataResourceType) throws StatusCodeWithException {

        String key = id + dataResourceType;
        if (CACHE_MAP.containsKey(key)) {
            return (AbstractDataSetOutputModel) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create()
                .put("id", id)
                .put("data_set_id", id);

        String api = null;
        switch (dataResourceType) {
            case TableDataSet:
                api = "data_set/detail";
                break;
            case ImageDataSet:
                api = "image_data_set/detail";
                break;
            default:
        }

        JSONObject result = request(api, params);

        JSONObject data = result.getJSONObject("data");

        if (data == null || data.isEmpty()) {
            return null;
        }

        AbstractDataSetOutputModel output = null;
        switch (dataResourceType) {
            case TableDataSet:
                output = data.toJavaObject(TableDataSetOutputModel.class);
                break;
            case ImageDataSet:
                output = data.toJavaObject(ImageDataSetOutputModel.class);
                break;
            default:
                StatusCode.UNEXPECTED_ENUM_CASE.throwException();
        }
        CACHE_MAP.put(key, output);
        return output;
    }

}
