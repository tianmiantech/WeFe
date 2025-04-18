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

package com.welab.wefe.serving.sdk.algorithm.lr.batch;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Bulk vertical federation(promoter)
 *
 * @author hunter.zhao
 */
public class LrVertPromoterBatchAlgorithm extends AbstractLrBatchAlgorithm<BaseLrModel, List<LrPredictResultModel>> {

    @Override
    protected List<LrPredictResultModel> handle(BatchPredictParams batchPredictParams, List<JObject> federatedResult) throws StatusCodeWithException {

        List<LrPredictResultModel> predictModelList = batchLocalCompute(batchPredictParams);

        return isScoreCard() ? mergeRemote(federatedResult, predictModelList) : normalize(mergeRemote(federatedResult, predictModelList));
    }

    private List<LrPredictResultModel> mergeRemote(List<JObject> federatedResult, List<LrPredictResultModel> predictModelList) {

        if (CollectionUtils.isEmpty(federatedResult)) {
            return predictModelList;
        }

        for (JSONObject remoteJson : federatedResult) {
            List<LrPredictResultModel> remoteScores = (List<LrPredictResultModel>) remoteJson.get("result");

            /**
             * Combine calculation results
             */
            for (LrPredictResultModel model : predictModelList) {
                for (LrPredictResultModel remote : remoteScores) {
                    if (model.getUserId().equals(remote.getUserId())) {

                        if (!remote.getError().isEmpty()) {
                            model.setError("协作方模型调用失败！错误：" + remote.getError());
                            continue;
                        }

                        Double score = model.getScore() + remote.getScore();
                        model.setScore(score);
                    }
                }
            }
        }


        return predictModelList;
    }
}
