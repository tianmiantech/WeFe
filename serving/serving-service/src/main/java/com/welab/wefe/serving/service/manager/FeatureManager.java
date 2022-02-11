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

package com.welab.wefe.serving.service.manager;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.feature.ApiFeatureDataHandle;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandle;
import com.welab.wefe.serving.service.feature.SqlFeatureDataHandle;
import com.welab.wefe.serving.service.service.ModelService;

import java.util.HashMap;
import java.util.Map;

import static com.welab.wefe.common.StatusCode.UNEXPECTED_ENUM_CASE;

/**
 * Get eigenvalue
 *
 * @author hunter.zhao
 */
public class FeatureManager {
    /**
     * Model cache
     */
    private static Map<String, PredictFeatureDataSource> FEATURE_SOURCE = new HashMap<>();

    private static final ModelService modelService;

    static {
        modelService = Launcher.CONTEXT.getBean(ModelService.class);
    }


    /**
     * Get feature source
     */
    private static PredictFeatureDataSource getFeatureSource(String modelId) throws StatusCodeWithException {

        if (FEATURE_SOURCE.get(modelId) != null) {
            return FEATURE_SOURCE.get(modelId);
        }

        synchronized (modelService) {
            ModelMySqlModel mysqlModel = modelService.findOne(modelId);
            if (mysqlModel == null) {
                throw new StatusCodeWithException("modelId error: " + modelId, StatusCode.PARAMETER_VALUE_INVALID);
            }

            FEATURE_SOURCE.put(modelId, mysqlModel.getFeatureSource());
        }

        return FEATURE_SOURCE.get(modelId);
    }

    public static Map<String, Object> getFeatureData(String modelId, PredictParams predictParams) throws StatusCodeWithException {

        PredictFeatureDataSource featureSource = getFeatureSource(modelId);
        /**
         * Judge the source of feature acquisition
         * -Interface input parameter
         * -Class configuration
         * -Through SQL query
         */
        switch (featureSource) {
            case api:
                return new ApiFeatureDataHandle().handle(modelId, predictParams);
            case code:
                return new CodeFeatureDataHandle().handle(modelId, predictParams);
            case sql:
                return new SqlFeatureDataHandle().handle(modelId, predictParams);
            default:
                throw new StatusCodeWithException(UNEXPECTED_ENUM_CASE);
        }
    }

    public static Map<String, Map<String, Object>> getFeatureDataByBatch(String modelId, PredictParams predictParams) throws StatusCodeWithException {

        PredictFeatureDataSource featureSource = getFeatureSource(modelId);
        /**
         * Judge the source of feature acquisition
         * -Interface input parameter
         * -Class configuration
         * -Through SQL query
         */
        switch (featureSource) {
            case api:
                return new ApiFeatureDataHandle().batch(modelId, predictParams);
            case code:
                return new CodeFeatureDataHandle().batch(modelId, predictParams);
            case sql:
                return new SqlFeatureDataHandle().batch(modelId, predictParams);
            default:
                throw new StatusCodeWithException(UNEXPECTED_ENUM_CASE);
        }
    }

    /**
     * Get configuration via SQL
     */
    public static Map<String, Object> getFeatureData(JSONObject sqlConfig) throws StatusCodeWithException {
        DatabaseType type = DatabaseType.valueOf(sqlConfig.get("type").toString());
        String url = sqlConfig.get("url").toString();
        String username = sqlConfig.get("username").toString();
        String password = sqlConfig.get("password").toString();
        String sqlContext = sqlConfig.get("sql_context").toString();
        String userId = sqlConfig.get("user_id").toString();

        //Fill in corresponding feature information
        return SqlFeatureDataHandle.get(type, url, username, password, sqlContext, userId);
    }

    /**
     * Process name
     */
    public static String getProcessor(String modelId) {
        return CodeFeatureDataHandle.getSimpleName(modelId);
    }
}
