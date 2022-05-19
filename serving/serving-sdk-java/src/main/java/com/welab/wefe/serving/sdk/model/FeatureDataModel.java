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
package com.welab.wefe.serving.sdk.model;

import java.util.Map;

/**
 * @author hunter.zhao
 * @date 2022/5/17
 */
public class FeatureDataModel {

    public boolean isFound = true;

    public Map<String, Object> featureDataMap;

    public String retCode = "0";

    public String error;

    public String message;

    public boolean isFound() {
        return isFound;
    }

    public void setFound(boolean found) {
        isFound = found;
    }

    public Map<String, Object> getFeatureDataMap() {
        return featureDataMap;
    }

    public void setFeatureDataMap(Map<String, Object> featureDataMap) {
        this.featureDataMap = featureDataMap;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static FeatureDataModel of(Map<String, Object> featureData) {
        FeatureDataModel featureDataModel = new FeatureDataModel();
        featureDataModel.featureDataMap = featureData;
        return featureDataModel;
    }

    public static FeatureDataModel fail(String errorMessage, String retCode) {
        FeatureDataModel featureDataModel = new FeatureDataModel();
        featureDataModel.isFound = false;
        featureDataModel.error = errorMessage;
        featureDataModel.retCode = retCode;
        return featureDataModel;
    }

    /**
     * 不带特征返回实体
     *
     * @param retCode
     * @param message
     * @return
     */
    public static FeatureDataModel of(Boolean isFound, String retCode, String message) {
        FeatureDataModel featureDataModel = new FeatureDataModel();
        featureDataModel.retCode = retCode;
        featureDataModel.message = message;
        featureDataModel.isFound = isFound;
        return featureDataModel;
    }


}
