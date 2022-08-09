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

package com.welab.wefe.serving.sdk.model.lr;

import com.welab.wefe.serving.sdk.model.PredictModel;

/**
 * @author hunter.zhao
 */
public class LrPredictResultModel extends PredictModel {
    private Double score;
    private Object scoreCard;

    public static LrPredictResultModel of(String userId, Double score) {
        LrPredictResultModel model = new LrPredictResultModel();
        model.userId = userId;
        model.score = score;
        return model;
    }

    public static LrPredictResultModel of(String userId, Object scoreCard) {
        LrPredictResultModel model = new LrPredictResultModel();
        model.userId = userId;
        model.scoreCard = scoreCard;
        return model;
    }

    public static LrPredictResultModel fail(String userId, String error) {
        LrPredictResultModel model = new LrPredictResultModel();
        model.userId = userId;
        model.error = error;
        return model;
    }


    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Object getScoreCard() {
        return scoreCard;
    }

    public void setScoreCard(Object scoreCard) {
        this.scoreCard = scoreCard;
    }
}
