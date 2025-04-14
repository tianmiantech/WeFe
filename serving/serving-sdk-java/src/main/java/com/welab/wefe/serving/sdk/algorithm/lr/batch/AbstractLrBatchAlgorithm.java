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

import com.welab.wefe.serving.sdk.algorithm.AbstractBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.LrAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;

import java.util.List;

/**
 * @author hunter.zhao
 */
public abstract class AbstractLrBatchAlgorithm<T extends BaseLrModel, R> extends AbstractBatchAlgorithm<T, R> {
    protected List<LrPredictResultModel> batchLocalCompute(BatchPredictParams batchPredictParams) {
        List<LrPredictResultModel> predictResult = LrAlgorithmHelper.batchCompute(
                modelParam.getModelParam(), batchPredictParams, modelParam.getScoreCardInfo());

        predictResult.stream().forEach(
                x -> x.setFeatureResult(PredictModel.extractFeatureResult(
                                batchPredictParams
                                        .getPredictParamsByUserId(x.getUserId())
                                        .getFeatureDataModel()
                        )
                )
        );

        return predictResult;
    }

    protected void intercept(List<LrPredictResultModel> predictResultList) {
        predictResultList.forEach(x -> x.setScore(
                        LrAlgorithmHelper.intercept(
                                x.getScore(),
                                modelParam.getModelParam().getIntercept())
                )
        );
    }

    /**
     * batch sigmod function
     */
    protected void sigmod(List<LrPredictResultModel> predictResultList) {
        predictResultList.forEach(model ->
                model.setScore(LrAlgorithmHelper.sigmod(model.getScore()))
        );
    }

    protected List<LrPredictResultModel> normalize(List<LrPredictResultModel> predictResultList) {
        intercept(predictResultList);
        sigmod(predictResultList);

        return predictResultList;
    }

    protected boolean isScoreCard() {
        return modelParam.getScoreCardInfo() != null;
    }

}
