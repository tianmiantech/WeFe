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
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.union.DataSetTagListApi;
import com.welab.wefe.board.service.api.union.MemberListApi;
import com.welab.wefe.board.service.api.union.QueryDataSetApi;
import com.welab.wefe.board.service.api.union.TagListApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.data_set.AbstractDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetOutputModel;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 * @author Zane
 */
@Service
public class UnionService extends AbstractService {

    /**
     * cache
     */
    private static final ExpiringMap<String, Object> CACHE_MAP = ExpiringMap
            .builder()
            .expiration(60, TimeUnit.SECONDS)
            .maxSize(500)
            .build();

    @Autowired
    private Config config;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * initialize wefe system
     */
    public void initializeSystem(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("member_id", model.getMemberId())
                .put("name", model.getMemberName())
                .put("mobile", model.getMemberMobile())
                .put("allow_open_data_set", model.getMemberAllowPublicDataSet())
                .put("public_key", model.getRsaPublicKey())
                .put("email", model.getMemberEmail())
                .put("gateway_uri", model.getMemberGatewayUri())
                .put("logo", model.getMemberLogo())
                .put("hidden", model.getMemberHidden());

        request("member/add", params, false);
    }

    /**
     * Report member information
     */
    public void uploadMemberInfo(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("name", model.getMemberName())
                .put("mobile", model.getMemberMobile())
                .put("allow_open_data_set", model.getMemberAllowPublicDataSet())
                .put("public_key", model.getRsaPublicKey())
                .put("email", model.getMemberEmail())
                .put("gateway_uri", model.getMemberGatewayUri())
                .put("logo", model.getMemberLogo())
                .put("hidden", model.getMemberHidden());

        request("member/update", params);
    }


    /**
     * Reset key
     */
    public void resetPublicKey(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("public_key", model.getRsaPublicKey());

        request("member/update_public_key", params);
    }

    /**
     * Update member information (not including logo)
     */
    public void uploadMemberInfoExcludeLogo(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("name", model.getMemberName())
                .put("mobile", model.getMemberMobile())
                .put("allow_open_data_set", model.getMemberAllowPublicDataSet())
                .put("public_key", model.getRsaPublicKey())
                .put("email", model.getMemberEmail())
                .put("gateway_uri", model.getMemberGatewayUri())
                .put("hidden", model.getMemberHidden());

        request("member/update_exclude_logo", params);
    }

    /**
     * Update member information logo
     */
    public void updateMemberLogo(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("logo", model.getMemberLogo());

        request("member/update_logo", params);
    }

    private void uploadDataSet(AbstractDataSetMysqlModel model, JObject params) throws StatusCodeWithException {
        MemberInfoModel member = globalConfigService.getMemberInfo();
        // If data exposure is prohibited globally, it will not be reported.
        if (!member.getMemberAllowPublicDataSet()) {
            return;
        }

        // If this data set is not publicly available to anyone
        if (model.getPublicLevel() == DataSetPublicLevel.OnlyMyself) {
            // Notify union to remove the data set
            dontPublicDataSet(model.getId());
            return;
        }

        CommonThreadPool.run(() -> {
            try {
                request("data_set/put", params);
            } catch (StatusCodeWithException e) {
                super.log(e);
            }
        });

    }

    public void uploadImageDataSet(ImageDataSetMysqlModel model) throws StatusCodeWithException {
        // TODO: Zane 待补充
        JObject params = JObject
                .create()
                .put("id", model.getId())
                .put("name", model.getName())
                .put("member_id", CacheObjects.getMemberId())
                .put("public_level", model.getPublicLevel())
                .put("public_member_list", model.getPublicMemberList())
                .put("usage_count_in_job", model.getUsageCountInJob())
                .put("usage_count_in_flow", model.getUsageCountInFlow())
                .put("usage_count_in_project", model.getUsageCountInProject())
                .put("tags", model.getTags())
                .put("description", model.getDescription());

        uploadDataSet(model, params);
    }

    /**
     * Report data set information
     */
    public void uploadTableDataSet(DataSetMysqlModel model) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("id", model.getId())
                .put("name", model.getName())
                .put("member_id", CacheObjects.getMemberId())
                .put("contains_y", model.getContainsY())
                .put("row_count", model.getRowCount())
                .put("column_count", model.getColumnCount())
                .put("column_name_list", model.getColumnNameList())
                .put("feature_count", model.getFeatureCount())
                .put("feature_name_list", model.getFeatureNameList())
                .put("public_level", model.getPublicLevel())
                .put("public_member_list", model.getPublicMemberList())
                .put("usage_count_in_job", model.getUsageCountInJob())
                .put("usage_count_in_flow", model.getUsageCountInFlow())
                .put("usage_count_in_project", model.getUsageCountInProject())
                .put("tags", model.getTags())
                .put("description", model.getDescription());

        uploadDataSet(model, params);

    }

    /**
     * Hidden data set
     */
    public void dontPublicDataSet(String dataSetId) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", dataSetId);

        request("data_set/delete", params);
    }

    /**
     * Pagination query member
     */
    public synchronized JSONObject queryMembers(MemberListApi.Input input) throws StatusCodeWithException {

        String key = "queryMembers" + JSON.toJSONString(input);
        if (CACHE_MAP.containsKey(key)) {
            return (JSONObject) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create()
                .put("page_index", input.getPageIndex())
                .put("page_size", input.getPageSize())
                .put("name", input.getName())
                .put("id", input.getId());

        JSONObject response = request("member/query", params);
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
    public JSONObject queryTags(TagListApi.Input input) throws StatusCodeWithException {

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
        JObject params = JObject
                .create()
                .put("page_index", input.getPageIndex())
                .put("page_size", input.getPageSize())
                .put("id", input.getId())
                .put("tag", input.getTag())
                .put("name", input.getName())
                .put("contains_y", input.getContainsY())
                .put("member_id", input.getMemberId());

        return request("data_set/query", params);
    }

    /**
     * Get details of a single data set
     */
    public DataSetOutputModel queryDataSetDetail(String id) throws StatusCodeWithException {

        if (CACHE_MAP.containsKey(id)) {
            return (DataSetOutputModel) CACHE_MAP.get(id);
        }

        JObject params = JObject
                .create()
                .put("id", id);

        JSONObject result = request("data_set/detail", params);

        JSONObject data = result.getJSONObject("data");

        if (data == null || data.isEmpty()) {
            return null;
        }

        return data.toJavaObject(DataSetOutputModel.class);
    }

    private JSONObject request(String api, JSONObject params) throws StatusCodeWithException {
        return request(api, params, true);
    }

    private JSONObject request(String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
        /**
         * Prevent the map from being out of order, causing the verification to fail.
         */
        params = new JSONObject(new TreeMap(params));

        String data = params.toJSONString();

        // rsa signature
        if (needSign) {
            String sign = null;
            try {
                sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }


            JSONObject body = new JSONObject();
            body.put("member_id", CacheObjects.getMemberId());
            body.put("sign", sign);
            body.put("data", data);

            data = body.toJSONString();
        }

        HttpResponse response = HttpRequest
                .create(config.getUNION_BASE_URL() + "/" + api)
                .setBody(data)
                .postJson();

        if (!response.success()) {
            throw new StatusCodeWithException(response.getMessage(), StatusCode.RPC_ERROR);
        }

        JSONObject json;
        try {
            json = response.getBodyAsJson();
        } catch (JSONException e) {
            throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        if (json == null) {
            throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("union 响应失败(" + code + ")：" + json.getString("message"), StatusCode.RPC_ERROR);
        }
        return json;
    }

}
