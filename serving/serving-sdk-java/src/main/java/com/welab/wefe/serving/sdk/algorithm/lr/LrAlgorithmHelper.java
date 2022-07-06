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

package com.welab.wefe.serving.sdk.algorithm.lr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.enums.StateCode;
import com.welab.wefe.serving.sdk.model.lr.LrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;
import com.welab.wefe.serving.sdk.utils.AlgorithmThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static java.lang.Math.exp;

/**
 * @author hunter.zhao
 */
public class LrAlgorithmHelper {
    private static final Logger LOG = LoggerFactory.getLogger(LrAlgorithmHelper.class);


    /**
     * single sigmod function
     */
    public static Double sigmod(Double score) {
        return 1. / (1. + exp(-score));
    }

    /**
     * Calculate points based on features
     */
    public static LrPredictResultModel compute(LrModel model, String userId, Map<String, Object> featureData) {
        if (featureData == null) {
            return LrPredictResultModel.of(userId, 0.0);
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
            return LrPredictResultModel.fail(userId, StateCode.FEATURE_ERROR.getMessage());
        }

        return LrPredictResultModel.of(userId, score);
    }


    public static List<LrPredictResultModel> batchCompute(LrModel lrModel, BatchPredictParams batchPredictParams) {
        CopyOnWriteArrayList<LrPredictResultModel> outputs = new CopyOnWriteArrayList<>();

        CountDownLatch latch = new CountDownLatch(batchPredictParams.getUserIds().size());

        batchPredictParams.getPredictParamsList().forEach(x ->
                AlgorithmThreadPool.run(() -> outputs.add(
                                LrAlgorithmHelper.compute(
                                        lrModel,
                                        x.getUserId(),
                                        x.getFeatureDataModel().getFeatureDataMap())
                        )
                )
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Execution prediction error：{}", e.getMessage());
            e.printStackTrace();
        }

        return outputs;
    }

    public static Double intercept(double score, double intercept) {
        return score + intercept;
    }
}
