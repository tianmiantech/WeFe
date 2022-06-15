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
import com.welab.wefe.serving.sdk.utils.AlgorithmThreadPool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author hunter.zhao
 */
public abstract class AbstractLrBatchAlgorithm<T extends BaseLrModel, R> extends AbstractBatchAlgorithm<T, R> {


    public List<LrPredictResultModel> compute(BatchPredictParams batchPredictParams) {
        CopyOnWriteArrayList<LrPredictResultModel> outputs = new CopyOnWriteArrayList<>();

        CountDownLatch latch = new CountDownLatch(batchPredictParams.getUserIds().size());

        batchPredictParams.getPredictParamsList().forEach(x ->
                AlgorithmThreadPool.run(() -> outputs.add(
                                LrAlgorithmHelper.compute(
                                        modelParam.getModelParam(),
                                        x.getUserId(),
                                        x.getFeatureDataModel().getFeatureDataMap())
                        )
                )
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Execution prediction error：{}", e.getMessage());
            e.printStackTrace();
        }

        return outputs;
    }

    public void intercept(List<LrPredictResultModel> predictModelList) {
        predictModelList.stream()
                .forEach(x -> x.setScore(x.getScore() + modelParam.getModelParam().getIntercept()));
    }
}
