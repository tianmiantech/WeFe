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

package com.welab.wefe.serving.sdk.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hunter.zhao
 */
public class BatchPredictParams {

    private List<PredictParams> predictParamsList;

    private List<String> userIds;

    private BatchPredictParams() {

    }

    public static BatchPredictParams ofUserIds(List<String> userIds) {
        BatchPredictParams batchPredictParams = new BatchPredictParams();
        batchPredictParams.userIds = userIds;
        return batchPredictParams;
    }

    public static BatchPredictParams of(List<PredictParams> predictParamsList) {
        BatchPredictParams batchPredictParams = new BatchPredictParams();
        batchPredictParams.predictParamsList = predictParamsList;

        List<String> userIds = new ArrayList<>();
        predictParamsList.forEach(x -> userIds.add(x.getUserId()));
        batchPredictParams.userIds = userIds;

        return batchPredictParams;
    }

    public List<PredictParams> getPredictParamsList() {
        return predictParamsList;
    }

    public void setPredictParamsList(List<PredictParams> predictParamsList) {
        this.predictParamsList = predictParamsList;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
