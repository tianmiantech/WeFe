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

package com.welab.wefe.serving.sdk.algorithm.xgboost.batch;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.xgboost.XgboostAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.enums.XgboostWorkMode;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgbProviderPredictResultModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostPredictResultModel;
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
public class XgboostVertPromoterBatchAlgorithm extends AbstractXgBoostBatchAlgorithm<BaseXgboostModel, List<XgboostPredictResultModel>> {

    /**
     * Federated forecast returns data
     */
    private Map<String, Map<String, Object>> remoteResult = new HashMap<>();

    private CopyOnWriteArrayList<XgboostPredictResultModel> predictModelList = new CopyOnWriteArrayList<>();

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
    private void getFederatedPredict(List<JObject> federatedResult) throws StatusCodeWithException {

        if (CollectionUtils.isEmpty(federatedResult)) {
            throw new StatusCodeWithException(StatusCode.REMOTE_SERVICE_ERROR, "协作方结果有误！");
        }

        /**
         * The resolution partner returns the result
         */
        for (JObject result : federatedResult) {
            List<JObject> predictModelList = result.getJSONList("result");

            for (JObject jobj : predictModelList) {

                XgbProviderPredictResultModel model = jobj.toJavaObject(XgbProviderPredictResultModel.class);
                Map<String, Object> tree = (Map) model.getXgboostTree();

                Map<String, Object> remote = remoteResult.get(model.getUserId());
                if (MapUtils.isEmpty(remote)) {
                    remote = new HashMap<>(16);
                }

                for (String key : tree.keySet()) {
                    if (remote.containsKey(key)
                            && XgboostWorkMode.skip.name().equals(modelParam.getModelMeta().getWorkMode())) {
                        Map<String, Boolean> map = (Map) remote.get(key);
                        map.putAll((Map) tree.get(key));
                        remote.put(key, map);

                    } else {
                        remote.put(key, tree.get(key));
                    }
                }
                remoteResult.put(model.getUserId(), remote);
            }
        }

        if (MapUtils.isEmpty(remoteResult)) {
            throw new StatusCodeWithException(StatusCode.REMOTE_SERVICE_ERROR, "remoteResult is null");
        }
    }

    @Override
    protected List<XgboostPredictResultModel> handlePredict(BatchPredictParams batchPredictParams, List<JObject> federatedResult) throws StatusCodeWithException {

        getFederatedPredict(federatedResult);

        CountDownLatch latch = new CountDownLatch(fidValueMapping.size());

        //Multithreaded compute node
        fidValueMapping.forEach((k, v) ->
                AlgorithmThreadPool.run(() -> {
                    try {
                        predictModelList.add(
                                XgboostAlgorithmHelper.promoterPredictByVert(
                                        modelParam.getModelMeta().getWorkMode(),
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
            LOG.error("执行预测失败：{}", e.getMessage());
            e.printStackTrace();
        }

        return predictModelList;
    }
}
