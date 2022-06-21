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

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * @author hunter.zhao
 * @date 2022/5/17
 */
public class FeatureDataModel extends BaseFeatureResultModel {

    private Map<String, Object> featureDataMap;

    public Map<String, Object> getFeatureDataMap() {
        return featureDataMap;
    }

    public void setFeatureDataMap(Map<String, Object> featureDataMap) {
        this.featureDataMap = featureDataMap;
    }

    public static FeatureDataModel of(Map<String, Object> featureData) {
        FeatureDataModel featureDataModel = new FeatureDataModel();
        featureDataModel.featureDataMap = featureData;
        featureDataModel.found = MapUtils.isNotEmpty(featureData);
        featureDataModel.error = MapUtils.isNotEmpty(featureData) ? "" : "未查询到样本！";
        return featureDataModel;
    }

    public static FeatureDataModel fail(String errorMessage) {
        FeatureDataModel featureDataModel = new FeatureDataModel();
        featureDataModel.error = errorMessage;
        return featureDataModel;
    }

    public static void main(String[] args) {
        FeatureDataModel featureDataModel =  FeatureDataModel.of(null);
        System.out.println(JSON.toJSONString(featureDataModel));
    }
}
