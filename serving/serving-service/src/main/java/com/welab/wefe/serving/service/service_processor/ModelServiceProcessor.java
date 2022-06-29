/*
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
package com.welab.wefe.serving.service.service_processor;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.service.api.predict.PredictApi;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.predicter.Predictor;
import com.welab.wefe.serving.service.service.CacheObjects;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author hunter.zhao
 */
public class ModelServiceProcessor extends AbstractServiceProcessor<TableModelMySqlModel, PredictResult> {


    @Override
    public PredictResult process(JObject data, TableModelMySqlModel model) throws StatusCodeWithException {

        PredictApi.Input input = data.toJavaObject(PredictApi.Input.class);
        input.checkAndStandardize();

        if (!ModelManager.getModelEnable(input.getModelId())) {
            throw StatusCode.INVALID_PARAMETER
                    .throwException("模型成员 " + CacheObjects.getMemberName() + " 未上线该模型");
        }


        /**
         * batch prediction
         */
        if (CollectionUtils.isNotEmpty(input.getUserIds())) {
            PredictResult result = Predictor.batch(
                    input.getRequestId(),
                    input.getModelId(),
                    input.getUserIds(),
                    input.getFeatureDataMap()
            );
            return result;
        }


        /**
         * Single prediction
         */
        PredictResult result = Predictor.predict(
                input.getRequestId(),
                input.getModelId(),
                input.getUserId(),
                input.getFeatureData()
        );

        return result;
    }
}
