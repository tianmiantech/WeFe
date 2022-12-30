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

package com.welab.wefe.mpc.psi.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.dh.DhPsiClient;
import com.welab.wefe.mpc.psi.sdk.service.PrivateSetIntersectionService;
import com.welab.wefe.mpc.psi.sdk.util.EcdhUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class PrivateSetIntersection extends Psi {

    /**
     * 多方求交集
     * 
     * @param configs 服务器的通信配置信息列表
     * @param ids     本方id集
     * @return
     * @throws Exception
     */
    public List<String> query(List<CommunicationConfig> configs, List<String> ids) throws Exception {
        List<String> result = new ArrayList<>(ids);
        for (CommunicationConfig config : configs) {
            List<String> psi = query(config, ids);
            if (result.isEmpty()) {
                break;
            }
            result = result.stream().filter(item -> psi.contains(item)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch, int batchSize)
            throws Exception {
        if (isContinue()) {
            int arr[] = readLastCurrentBatchAndSize(config.getRequestId());
            currentBatch = arr[0] + 1;
            batchSize = arr[1];
        }
        if (CollectionUtil.isEmpty(clientIds)) {
            throw new IllegalArgumentException("local id is empty");
        }
        if (config == null || StrUtil.isEmpty(config.getServerUrl())) {
            throw new IllegalArgumentException("server config missing");
        }
        long start = System.currentTimeMillis();
        DhPsiClient client = new DhPsiClient();
        // 加密我自己的数据
        Map<Long, String> clientEncryptedDatasetMap = client.encryptClientOriginalDataset(clientIds);
        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        request.setP(client.getP().toString(16));
        // 发给服务端
        request.setClientIds(EcdhUtil.convert2List(clientEncryptedDatasetMap));
        request.setRequestId(config.getRequestId());
        request.setCurrentBatch(currentBatch);
        request.setType(Psi.DH_PSI);
        request.setBatchSize(batchSize);
        PrivateSetIntersectionService privateSetIntersectionService = new PrivateSetIntersectionService();
        logger.info("dh psi request = " + request);
        QueryPrivateSetIntersectionResponse response = privateSetIntersectionService.handle(config, request);
        if (response.getCode() != 0) {
            throw new Exception(response.getMessage());
        }
        boolean hasNext = response.isHasNext();
        logger.info("dh psi response serverIds size = " + CollectionUtils.size(response.getServerEncryptIds())
                + ", clientIds size = " + CollectionUtils.size(response.getClientIdByServerKeys()));
        // 获取服务端id, 加密服务端ID
        client.encryptServerDataset(response.getServerEncryptIds());
        // 获取被服务端加密了的客户端ID
        client.setClientIdByServerKeys(EcdhUtil.convert2Map(response.getClientIdByServerKeys()));
        List<String> result = new ArrayList<>();
        Set<String> batchResult = client.psi();
        if (batchResult != null && !batchResult.isEmpty()) {
            if (clientDatasetMap != null && !clientDatasetMap.isEmpty()) {
                batchResult = batchResult.stream().map(s -> clientDatasetMap.get(s)).collect(Collectors.toSet());
            }
            result.addAll(batchResult);
        }
        saveLastCurrentBatchAndSize(request.getRequestId(), request.getCurrentBatch(), request.getBatchSize());
        savePsiResult(batchResult, request.getRequestId());
        logger.info("dh psi result, currentBatch = " + request.getCurrentBatch() + ", all psi result size = "
                + result.size() + ", hasNext = " + hasNext + ", duration = " + (System.currentTimeMillis() - start));
        while (hasNext) {
            start = System.currentTimeMillis();
            // 发给服务端
            request.setClientIds(null);// 只需要第一次传给服务端
            request.setCurrentBatch(request.getCurrentBatch() + 1);
            privateSetIntersectionService = new PrivateSetIntersectionService();
            logger.info("dh psi request = " + request);
            response = privateSetIntersectionService.handle(config, request);
            if (response.getCode() != 0) {
                throw new Exception(response.getMessage());
            }
            hasNext = response.isHasNext();
            logger.info("dh psi response serverIds size = " + CollectionUtils.size(response.getServerEncryptIds())
                    + ", clientIds size = " + CollectionUtils.size(response.getClientIdByServerKeys()));
            // 获取服务端id, 加密服务端ID
            client.encryptServerDataset(response.getServerEncryptIds());
            // 获取被服务端加密了的客户端ID
//            client.setClientIdByServerKeys(response.getClientIdByServerKeys());
            batchResult = client.psi();
            if (batchResult != null && !batchResult.isEmpty()) {
                if (clientDatasetMap != null && !clientDatasetMap.isEmpty()) {
                    batchResult = batchResult.stream().map(s -> clientDatasetMap.get(s)).collect(Collectors.toSet());
                }
                result.addAll(batchResult);
            }
            saveLastCurrentBatchAndSize(request.getRequestId(), request.getCurrentBatch(), request.getBatchSize());
            savePsiResult(batchResult, request.getRequestId());
            logger.info("dh psi result, currentBatch = " + request.getCurrentBatch() + ", all psi result size = "
                    + result.size() + ", hasNext = " + hasNext + ",duration = " + (System.currentTimeMillis() - start));
        }
        return result;
    }
}
