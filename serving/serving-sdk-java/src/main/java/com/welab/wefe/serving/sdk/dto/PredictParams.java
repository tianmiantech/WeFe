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

package com.welab.wefe.serving.sdk.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class PredictParams {
    private String userId;

    private Map<String, Object> featureData;

    private List<String> userIds;

    private Map<String, Map<String, Object>> featureDataMap;

    private PredictParams() {

    }

    public PredictParams(String userId) {
        this.userId = userId;
    }

    public static PredictParams of(String userId, Map<String, Object> featureData) {
        PredictParams predictParams = new PredictParams();
        predictParams.userId = userId;
        predictParams.featureData = featureData;
        return predictParams;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getFeatureData() {
        return featureData;
    }

    public void setFeatureData(Map<String, Object> featureData) {
        this.featureData = featureData;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Map<String, Map<String, Object>> getFeatureDataMap() {
        return featureDataMap;
    }

    public void setFeatureDataMap(Map<String, Map<String, Object>> featureDataMap) {
        this.featureDataMap = featureDataMap;
    }

    public static PredictParams ofUserIds(List<String> userIds) {
        PredictParams predictParams = new PredictParams();
        predictParams.userIds = userIds;
        return predictParams;
    }

    public static PredictParams of(Map<String, Map<String, Object>> featureDataMap) {
        PredictParams predictParams = new PredictParams();
        predictParams.featureDataMap = featureDataMap;

        List<String> userIds = new ArrayList<>();
        featureDataMap.forEach((k, v) -> userIds.add(k));
        predictParams.userIds = userIds;

        return predictParams;
    }
}
