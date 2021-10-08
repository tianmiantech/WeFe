/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.serving.sdk.algorithm.lr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.serving.sdk.enums.StateCode;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.LrModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author hunter.zhao
 */
public class LrAlgorithmHelper {
    private static final Logger LOG = LoggerFactory.getLogger(LrAlgorithmHelper.class);

    /**
     * Calculate points based on features
     */
    public static PredictModel compute(LrModel model, String userId, Map<String, Object> featureData) {
        if (featureData == null) {
            return PredictModel.of(userId, 0.0);
        }

        double score = 0;
        int featureNum = 0;

        for (String key : featureData.keySet()) {
            if (model.getWeight().containsKey(key)) {
                Double x = TypeUtils.castToDouble(featureData.get(key));
                Double w = TypeUtils.castToDouble(model.getWeight().get(key));
                score += w * x;
                featureNum++;
            }
        }

        //Features do not match at all
        if (featureNum <= 0) {
            LOG.error("featureData error, userId : {}, featureData: {} ,weight: {}", userId, JSON.toJSONString(featureData), JSON.toJSONString(model.getWeight()));
            PredictModel.fail(userId, StateCode.FEATURE_ERROR);
        }

        return PredictModel.of(userId, score);
    }
}
