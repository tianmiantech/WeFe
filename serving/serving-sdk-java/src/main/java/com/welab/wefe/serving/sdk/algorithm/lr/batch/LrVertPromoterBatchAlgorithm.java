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

package com.welab.wefe.serving.sdk.algorithm.lr.batch;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Bulk vertical federation(promoter)
 *
 * @author hunter.zhao
 */
public class LrVertPromoterBatchAlgorithm extends AbstractLrBatchAlgorithm<BaseLrModel, List<PredictModel>> {

    @Override
    protected List<PredictModel> handle(FederatedParams federatedParams, PredictParams predictParams, JSONObject params) throws StatusCodeWithException {

        List<PredictModel> scores = compute(predictParams);

        List<JObject> federatedResult = federatedPredict(
                federatedParams.getProviders(),
                setFederatedBatchPredictBody(federatedParams, predictParams.getUserIds())
        );

        if (CollectionUtils.isEmpty(federatedResult)) {
            return sigmod(scores);
        }

        for (JSONObject remoteJson : federatedResult) {
            List<PredictModel> remoteScores = (List<PredictModel>) remoteJson.get("data");

            /**
             * Combine calculation results
             */
            for (PredictModel model : scores) {
                for (PredictModel remote : remoteScores) {
                    if (model.getUserId().equals(remote.getUserId()) && remote.getStateCode() == 0) {
                        Double score = model.getScore() + remote.getScore();
                        model.setScore(score);
                    }
                }
            }
        }

        return sigmod(scores);
    }
}
