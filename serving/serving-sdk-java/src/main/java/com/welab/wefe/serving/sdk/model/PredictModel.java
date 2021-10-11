/**
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

package com.welab.wefe.serving.sdk.model;

import com.welab.wefe.serving.sdk.enums.StateCode;

/**
 * @author hunter.zhao
 */
public class PredictModel {
    private String userId;
    private int stateCode = 0;
    private String message;
    private Double score;
    private Object scores;
    private Object data;

    public static PredictModel of(String userId, Double score) {
        PredictModel model = new PredictModel();
        model.userId = userId;
        model.score = score;
        return model;
    }

    public static PredictModel ofObject(String userId, Object data) {
        PredictModel model = new PredictModel();
        model.userId = userId;
        model.data = data;
        return model;
    }

    public static PredictModel ofScores(String userId, Object scores) {
        PredictModel model = new PredictModel();
        model.userId = userId;
        model.scores = scores;
        return model;
    }

    public static PredictModel fail(String userId, StateCode stateCode, String message) {
        PredictModel model = new PredictModel();
        model.userId = userId;
        model.stateCode = stateCode.getCode();
        model.message = message;
        return model;
    }

    public static PredictModel fail(String userId, StateCode stateCode) {
        PredictModel model = new PredictModel();
        model.userId = userId;
        model.stateCode = stateCode.getCode();
        model.message = stateCode.getMessage();
        return model;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Object getScores() {
        return scores;
    }

    public void setScores(Object scores) {
        this.scores = scores;
    }
}
