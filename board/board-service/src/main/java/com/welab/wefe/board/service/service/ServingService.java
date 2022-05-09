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

package com.welab.wefe.board.service.service;


import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.data_output_info.SyncModelToServingApi;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.dto.entity.job.JobMemberOutputModel;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.dto.globalconfig.ServingConfigModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author Zane
 */
@Service
public class ServingService extends AbstractService {

    private static final String SEPARATOR = "_";

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobMemberService jobMemberService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    TaskService taskService;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * Update serving global configuration
     */
    public void asynRefreshMemberInfo(MemberInfoModel model) throws StatusCodeWithException {

        CommonThreadPool.run(() -> {
            try {
                refreshMemberInfo(model);
            } catch (StatusCodeWithException e) {
                LOG.error("serving 响应失败：" + e.getMessage(), StatusCode.REMOTE_SERVICE_ERROR);
            }
        });
    }

    /**
     * Update serving global configuration
     */
    public void refreshMemberInfo(MemberInfoModel model) throws StatusCodeWithException {

        TreeMap<String, Object> params = new TreeMap<>();
        params.put("member_id", model.getMemberId());
        params.put("member_name", model.getMemberName());
        params.put("rsa_private_key", model.getRsaPrivateKey());
        params.put("rsa_public_key", model.getRsaPublicKey());

        request("global_setting/refresh", params);
    }

    private JSONObject request(String api, TreeMap<String, Object> params) throws StatusCodeWithException {
        return request(api, params, true);
    }

    /**
     * call serving service
     *
     * @param params TreeMap prevents the map from being out of order, causing the verification to fail.
     */
    private JSONObject request(String api,
                               TreeMap<String, Object> params,
                               Boolean needSign) throws StatusCodeWithException {

        String data = new JSONObject(params).toJSONString();

        // sign
        if (needSign) {

            String sign = null;
            try {
                sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey());
            } catch (Exception e) {
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }

            JSONObject body = new JSONObject();
            body.put("memberId", CacheObjects.getMemberId());
            body.put("sign", sign);
            body.put("data", data);

            data = body.toJSONString();
        }

        ServingConfigModel servingConfig = globalConfigService.getServingConfig();
        if (servingConfig == null || StringUtil.isEmpty(servingConfig.intranetBaseUri)) {
            StatusCode.RPC_ERROR.throwException("请在[全局设置][系统设置]中指定 Serving 服务地址后重试");
        }

        if (!api.startsWith("/")) {
            api = "/" + api;
        }

        // send request
        HttpResponse response = HttpRequest
                .create(servingConfig.intranetBaseUri + api)
                .setBody(data)
                .postJson();

        if (!response.success()) {
            throw new StatusCodeWithException(response.getMessage(), StatusCode.RPC_ERROR);
        }

        JSONObject json = response.getBodyAsJson();
        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("serving 响应失败(" + code + ")：" + response.getMessage(), StatusCode.RPC_ERROR);
        }
        return json;
    }


    /**
     * Modeling synchronization to serving
     */
    public void syncModelToServing(SyncModelToServingApi.Input input) throws StatusCodeWithException {
        TreeMap<String, Object> jobj = setBody(input.getTaskId(), input.getRole());

        request("model_save", jobj, true);
    }


    public TreeMap<String, Object> setBody(String taskId, JobMemberRole role) throws StatusCodeWithException {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndTypeAndRole(taskId, TaskResultType.model_train.name(), role);

        if (taskResult == null) {
            LOG.error("查询task任务异常");
            throw new StatusCodeWithException("task 不存在！", StatusCode.PARAMETER_VALUE_INVALID);
        }


        List<JobMemberOutputModel> memberList = jobMemberService.list(taskResult.getJobId(), false);

        if (CollectionUtils.isEmpty(memberList)) {
            LOG.error("查询job_member异常");
            throw new StatusCodeWithException("查询job_member异常！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        JobMySqlModel job = jobRepository.findByJobId(taskResult.getJobId(), role.name());

        if (job == null) {
            LOG.error("查询job异常");
            throw new StatusCodeWithException("查询job异常！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Feature engineering
        List<TaskResultMySqlModel> featureEngineerResults = taskResultService.findByTaskIdAndRoleNotEqualType(taskId, TaskResultType.model_train.name(), role);
        Map<Integer, Object> featureEngineerMap = new TreeMap<>();
        for (TaskResultMySqlModel fe : featureEngineerResults) {
            TaskMySqlModel taskMySqlModel = taskService.findOne(fe.getTaskId());
            if (taskMySqlModel == null) {
                LOG.error("查询task任务异常");
                throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID);
            }
            featureEngineerMap.put(taskMySqlModel.getPosition(), getModelParam(fe.getResult()));
        }

        List<JSONObject> members = new ArrayList<>();

        memberList.forEach(mem -> {
            JSONObject member = new JSONObject();
            member.put("memberId", mem.getMemberId());
            member.put("role", mem.getJobRole());

            // Find the public key
            try {
                JSONObject json = unionService.queryMemberById(mem.getMemberId());
                member.put("name", json.getJSONObject("data").
                        getJSONArray("list").
                        getJSONObject(0).
                        getString("name"));
                member.put("publicKey", json.getJSONObject("data").
                        getJSONArray("list").
                        getJSONObject(0).
                        getString("public_key"));
            } catch (StatusCodeWithException e) {
                super.log(e);
            }

            members.add(member);
        });


        // body
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("modelId", taskResult.getModelId());
        params.put("name", job.getName());
        // The v2 version job does not have Algorithm and flType parameters
        params.put("algorithm", getAlgorithm(taskResult.getComponentType()));
        params.put("flType", job.getFederatedLearningType().name());
        params.put("modelParam", taskResult.getResult());
        params.put("memberParams", members);
        params.put("featureEngineerMap", featureEngineerMap);

        return params;
    }

    private Algorithm getAlgorithm(ComponentType componentType) {
        switch (componentType) {
            case HorzLR:
            case VertLR:
            case MixLR:
                return Algorithm.LogisticRegression;
            case HorzSecureBoost:
            case VertSecureBoost:
            case MixSecureBoost:
                return Algorithm.XGBoost;
            default:
                throw new RuntimeException("预算之外的组件类型");
        }
    }

    private String getModelParam(String taskResult) {
        return JObject.create(taskResult).getString("model_param");
    }
}
