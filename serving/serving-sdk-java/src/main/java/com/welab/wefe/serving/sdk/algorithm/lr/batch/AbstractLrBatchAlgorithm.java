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

package com.welab.wefe.serving.sdk.algorithm.lr.batch;

import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.LrAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import com.welab.wefe.serving.sdk.utils.AlgorithmThreadPool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author hunter.zhao
 */
public abstract class AbstractLrBatchAlgorithm<T extends BaseLrModel, R> extends AbstractAlgorithm<T, R> {


    public List<PredictModel> compute(PredictParams predictParams) {
        CopyOnWriteArrayList<PredictModel> outputs = new CopyOnWriteArrayList<>();

        CountDownLatch latch = new CountDownLatch(predictParams.getUserIds().size());

        predictParams.getFeatureDataMap().forEach((k, v) ->
                AlgorithmThreadPool.run(() -> outputs.add(LrAlgorithmHelper.compute(modelParam.getModelParam(), k, v)))
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Execution prediction error：{}", e.getMessage());
            e.printStackTrace();
        }

        return outputs;
    }
}
