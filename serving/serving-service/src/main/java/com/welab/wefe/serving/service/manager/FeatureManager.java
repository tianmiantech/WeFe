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
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandler;
import com.welab.wefe.serving.service.feature.SqlFeatureDataHandler;
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
            TableModelMySqlModel mysqlModel = modelService.findOne(modelId);
            if (mysqlModel == null) {
                throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "modelId error: " + modelId);
            }

            FEATURE_SOURCE.put(modelId, mysqlModel.getFeatureSource());
        }

        return FEATURE_SOURCE.get(modelId);
    }

    public static FeatureDataModel getFeatureData(String modelId, String userId) throws StatusCodeWithException {

        PredictFeatureDataSource featureSource = getFeatureSource(modelId);
        /**
         * Judge the source of feature acquisition
         * -Interface input parameter
         * -Class configuration
         * -Through SQL query
         */
        switch (featureSource) {
            case code:
                return new CodeFeatureDataHandler().handle(modelId, userId);
            case sql:
                return new SqlFeatureDataHandler().handle(modelId, userId);
            default:
                throw new StatusCodeWithException(UNEXPECTED_ENUM_CASE);
        }
    }

    /**
     * Get configuration via SQL
     */
    public static FeatureDataModel getFeatureData(String userId, JSONObject sqlConfig) throws StatusCodeWithException {
        String dataSourceId = sqlConfig.get("data_source_id").toString();
        String sqlScript = sqlConfig.get("sql_script").toString();
        String sqlConditionField = sqlConfig.get("sql_condition_field").toString();

        //Fill in corresponding feature information
        return SqlFeatureDataHandler.debug(dataSourceId, sqlScript, sqlConditionField, userId);
    }

    public static synchronized void refresh(String modelId, PredictFeatureDataSource featureDataSource) {
        FEATURE_SOURCE.put(modelId, featureDataSource);
    }

    /**
     * Process name
     */
    public static String getProcessor(String modelId) {
        return CodeFeatureDataHandler.getSimpleName(modelId);
    }
}
