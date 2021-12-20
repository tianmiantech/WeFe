/**
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

package com.welab.wefe.serving.service.manager;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class ModelManager {

    private static final ModelService modelService;

    static {
        modelService = Launcher.CONTEXT.getBean(ModelService.class);
    }


    /**
     * Model cache
     */
    private static Map<String, BaseModel> MODEL = new HashMap<>();

    /**
     * Model state cache
     */
    private static Map<String, Boolean> MODEL_ENABLE = new HashMap<>();

    public static Boolean getModelEnable(String modelId) throws StatusCodeWithException {

        if (MODEL_ENABLE.get(modelId) != null) {
            return MODEL_ENABLE.get(modelId);
        }

        synchronized (modelService) {
            ModelMySqlModel mysqlModel = modelService.findOne(modelId);
            if (mysqlModel == null) {
                throw new StatusCodeWithException("modelId error: " + modelId, StatusCode.PARAMETER_VALUE_INVALID);
            }


            MODEL_ENABLE.put(modelId, mysqlModel.isEnable());
        }

        return MODEL_ENABLE.get(modelId);
    }

    /**
     * refresh Model state
     */
    public static synchronized void refreshModelEnable(String modelId, boolean enable) {
        MODEL_ENABLE.put(modelId, enable);
    }

    public static BaseModel getModelParam(String modelId) throws StatusCodeWithException {

        if (MODEL.get(modelId) != null) {
            return MODEL.get(modelId);
        }

        synchronized (modelService) {
            ModelMySqlModel mysqlModel = modelService.findOne(modelId);
            if (mysqlModel == null) {
                throw new StatusCodeWithException("modelId error: " + modelId, StatusCode.PARAMETER_VALUE_INVALID);
            }

            List<ModelMemberMySqlModel> member = modelService.findByModelIdAndMemberId(modelId, CacheObjects.getMemberId());
            if (CollectionUtils.isEmpty(member)) {
                throw new StatusCodeWithException("modelId error:" + modelId + " or memberId error :" + CacheObjects.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
            }

            BaseModel model = new BaseModel();
            model.setAlgorithm(mysqlModel.getAlgorithm());
            model.setFlType(mysqlModel.getFlType());
            model.setParams(mysqlModel.getModelParam());
            model.setModelId(mysqlModel.getModelId());
            model.setMyRole(member.get(0).getRole());
            MODEL.put(modelId, model);
        }

        return MODEL.get(modelId);
    }

    public static BaseModel getModelParam(String modelId, JobMemberRole myRole) throws StatusCodeWithException {

        if (MODEL.get(modelId) != null) {
            return MODEL.get(modelId);
        }

        synchronized (modelService) {
            ModelMySqlModel mysqlModel = modelService.findOne(modelId);
            if (mysqlModel == null) {
                throw new StatusCodeWithException("modelId error: " + modelId, StatusCode.PARAMETER_VALUE_INVALID);
            }

            ModelMemberMySqlModel member = modelService.findByModelIdAndMemberIdAndRole(modelId, CacheObjects.getMemberId(), myRole);
            if (member == null) {
                throw new StatusCodeWithException("modelId error:" + modelId + " or memberId error :" + CacheObjects.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
            }

            BaseModel model = new BaseModel();
            model.setAlgorithm(mysqlModel.getAlgorithm());
            model.setFlType(mysqlModel.getFlType());
            model.setParams(mysqlModel.getModelParam());
            model.setModelId(mysqlModel.getModelId());
            model.setMyRole(member.getRole());
            MODEL.put(modelId, model);
        }

        return MODEL.get(modelId);
    }
}
