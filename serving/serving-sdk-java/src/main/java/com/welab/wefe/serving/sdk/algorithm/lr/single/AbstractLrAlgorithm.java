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

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.LrAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;

/**
 * @author hunter.zhao
 */
public abstract class AbstractLrAlgorithm<T extends BaseLrModel, R> extends AbstractAlgorithm<T, R> {
    public LrPredictResultModel localCompute(PredictParams predictParams) {
        LrPredictResultModel predictResult = LrAlgorithmHelper.compute(
                modelParam.getModelParam(),
                predictParams.getUserId(),
                predictParams.getFeatureDataModel().getFeatureDataMap(),
                modelParam.getScoreCardInfo());

        predictResult.setFeatureResult(
                PredictModel.extractFeatureResult(predictParams.getFeatureDataModel())
        );
        return predictResult;
    }

    public LrPredictResultModel normalize(LrPredictResultModel predictResult) {
        if (isScoreCard()) {
            predictResult.setScore(baseScore() + predictResult.getScore());
            return predictResult;
        }

        intercept(predictResult);
        sigmod(predictResult);

        return predictResult;
    }


    /**
     * single sigmod function
     */
    private void sigmod(LrPredictResultModel predictResult) {
        predictResult.setScore(LrAlgorithmHelper.sigmod(predictResult.getScore()));
    }

    private void intercept(LrPredictResultModel predictResult) {
        Double score = LrAlgorithmHelper.intercept(predictResult.getScore(), modelParam.getModelParam().getIntercept());
        predictResult.setScore(score);
    }

    private boolean isScoreCard() {
        return modelParam.getScoreCardInfo() != null;
    }

    private double baseScore() {
        JObject scoreCard = JObject.create(modelParam.getScoreCardInfo().getScoreCard());
        return scoreCard.getDouble("a_score") + scoreCard.getDouble("b_score") * modelParam.getModelParam().getIntercept();
    }
}
