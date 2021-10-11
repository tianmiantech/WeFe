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

package com.welab.wefe.serving.sdk.algorithm.xgboost.batch;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.serving.sdk.algorithm.xgboost.XgboostAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;
import com.welab.wefe.serving.sdk.utils.AlgorithmThreadPool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Horizontal federal projection(xgboost)
 *
 * @author hunter.zhao
 */
public class XgboostHorzPromoterBatchAlgorithm extends AbstractXgBoostBatchAlgorithm<BaseXgboostModel, List<PredictModel>> {

    private CopyOnWriteArrayList<PredictModel> predictModelList = new CopyOnWriteArrayList<>();

    @Override
    protected List<PredictModel> handlePredict(FederatedParams federatedParams, PredictParams predictParams, JSONObject params) {

        CountDownLatch latch = new CountDownLatch(fidValueMapping.size());

        //Multithreaded compute node
        fidValueMapping.forEach((k, v) ->
                AlgorithmThreadPool.run(() -> {
                    try {

                        LOG.info("predict start:" + k);
                        predictModelList.add(XgboostAlgorithmHelper.promoterPredictByHorz(modelParam.getModelParam(), k, v));

                    } finally {
                        LOG.info("Predict the end:" + k);
                        latch.countDown();
                    }

                })
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Execution prediction error：{}", e.getMessage());
            e.printStackTrace();
        }

        return predictModelList;
    }
}
