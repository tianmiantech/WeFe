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

package com.welab.wefe.serving.sdk.algorithm.lr.single;

import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Vertical federation initiator
 *
 * @author hunter.zhao
 */
public class LrVertPromoterAlgorithm extends AbstractLrAlgorithm<BaseLrModel, LrPredictResultModel> {

    @Override
    protected LrPredictResultModel handle(PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException {

        //Calculation results
        LrPredictResultModel result = execute(predictParams);

        if (StringUtil.isNotEmpty(result.getError())) {
            return result;
        }

        if (CollectionUtils.isEmpty(federatedResult)) {
            normalize(result);
            return result;
        }

        /**
         * Consolidated results
         */
        for (JObject remote : federatedResult) {
            LrPredictResultModel predictModel = remote.getJObject("result").toJavaObject(LrPredictResultModel.class);
            if (!predictModel.getError().isEmpty()) {
                StatusCode.REMOTE_SERVICE_ERROR.throwException("协作方模型调用失败！错误：" + predictModel.getError());
            }

            Double score = TypeUtils.castToDouble(result.getScore()) + TypeUtils.castToDouble(predictModel.getScore());
            result.setScore(score);
        }

        normalize(result);
        return result;
    }
}
