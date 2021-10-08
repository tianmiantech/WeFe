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

package com.welab.wefe.serving.sdk.algorithm.xgboost.batch;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.xgboost.XgboostAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;
import com.welab.wefe.serving.sdk.utils.AlgorithmThreadPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Vertical federation initiator(xgboost)
 *
 * @author hunter.zhao
 */
public class XgboostVertPromoterBatchAlgorithm extends AbstractXgBoostBatchAlgorithm<BaseXgboostModel, List<PredictModel>> {

    /**
     * Federated forecast returns data
     */
    private Map<String, Map<String, Map<String, Boolean>>> remoteResult = new HashMap<>();

    private CopyOnWriteArrayList<PredictModel> predictModelList = new CopyOnWriteArrayList<>();

    /**
     * Call provider to get the federated decision tree
     * Return to the structure
     * <p>
     * {
     * "0":{
     * "1":true,
     * "2":false,
     * "3":false,
     * "4":false
     * },
     * "1":{
     * "0":false,
     * "2":false,
     * "8":false
     * }
     * }
     * </>
     */
    private void getFederatedPredict(FederatedParams federatedParams, PredictParams predictParams) throws StatusCodeWithException {

        //Calling provider
        List<JObject> federatedResult = federatedPredict(
                federatedParams.getProviders(),
                setFederatedBatchPredictBody(federatedParams, predictParams.getUserIds())
        );

        if (CollectionUtils.isEmpty(federatedResult)) {
            throw new StatusCodeWithException("No result is returned from the provider", StatusCode.REMOTE_SERVICE_ERROR);
        }

        /**
         * The resolution partner returns the result
         */
        for (JObject result : federatedResult) {
            List<JObject> predictModelList = result.getJSONList("data");

            for (JObject jobj : predictModelList) {

                PredictModel model = jobj.toJavaObject(PredictModel.class);
                Map<String, Map<String, Boolean>> tree = (Map) model.getData();

                Map<String, Map<String, Boolean>> remote = remoteResult.get(model.getUserId());
                if (MapUtils.isEmpty(remote)) {
                    remote = new HashMap<>(16);
                }

                for (String key : tree.keySet()) {
                    if (remote.containsKey(key)) {
                        remote.get(key).putAll(tree.get(key));
                    } else {
                        remote.put(key, tree.get(key));
                    }
                }
                remoteResult.put(model.getUserId(), remote);
            }
        }

        if (MapUtils.isEmpty(remoteResult)) {
            throw new StatusCodeWithException("remoteResult is null", StatusCode.REMOTE_SERVICE_ERROR);
        }
    }

    @Override
    protected List<PredictModel> handlePredict(FederatedParams federatedParams, PredictParams predictParams, JSONObject params) throws StatusCodeWithException {

        getFederatedPredict(federatedParams, predictParams);

        CountDownLatch latch = new CountDownLatch(fidValueMapping.size());

        //Multithreaded compute node
        fidValueMapping.forEach((k, v) ->
                AlgorithmThreadPool.run(() -> {
                    try {
                        predictModelList.add(
                                XgboostAlgorithmHelper.promoterPredictByVert(
                                        modelParam.getModelParam(),
                                        k,
                                        v,
                                        remoteResult.get(k)
                                )
                        );
                    } finally {
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
