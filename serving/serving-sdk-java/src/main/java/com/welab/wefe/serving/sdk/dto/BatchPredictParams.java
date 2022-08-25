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

package com.welab.wefe.serving.sdk.dto;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public class BatchPredictParams {

    private List<PredictParams> predictParamsList = Lists.newArrayList();

    private List<String> userIds = Lists.newArrayList();

    private BatchPredictParams() {

    }


    public static BatchPredictParams create(List<String> userIds, Map<String, Map<String, Object>> featureDataMap) {
        BatchPredictParams batchPredictParams = new BatchPredictParams();
        batchPredictParams.userIds = userIds;
        batchPredictParams.predictParamsList = MapUtils.isEmpty(featureDataMap) ? Lists.newArrayList() :
                featureDataMap
                        .entrySet()
                        .stream()
                        .map(x -> PredictParams.create(x.getKey(), x.getValue()))
                        .collect(Collectors.toList());
        return batchPredictParams;
    }

    public List<PredictParams> getPredictParamsList() {
        return Collections.unmodifiableList(predictParamsList);
    }

    public void initializePredictParamsList(List<PredictParams> predictParamsList) {
        this.predictParamsList.addAll(predictParamsList);
    }

    public void replacePredictParamsList(List<PredictParams> predictParamsList) {
        for (int i = 0; i < predictParamsList.size(); i++) {
            predictParamsList.remove(i);
        }
        this.predictParamsList.addAll(predictParamsList);
    }


    public List<String> getUserIds() {
        return Collections.unmodifiableList(userIds);
    }

    public PredictParams getPredictParamsByUserId(String userId) {
        return predictParamsList.isEmpty() ? null : predictParamsList
                .stream()
                .filter(x -> x.getUserId().equals(userId))
                .findFirst()
                .get();
    }
}
