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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.ecdh.EcdhPsiClient;
import com.welab.wefe.mpc.psi.sdk.service.PrivateSetIntersectionService;
import com.welab.wefe.mpc.psi.sdk.util.EcdhUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Author: winter
 **/
public class EcdhPsi extends Psi {

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

    /**
     * 查询本文id集与服务器id集的集合操作
     *
     * @param config   服务器的连接信息
     * @param ids      本方id集
     * @param keySize  密钥安全长度
     * @param operator 自定义列表运算结果,默认求两个列表交集
     * @return
     * @throws Exception
     */
    @Override
    public List<String> query(CommunicationConfig config, List<String> ids, int currentBatch, int batchSize)
            throws Exception {
        if (isContinue()) {
            int arr[] = readLastCurrentBatchAndSize(config.getRequestId());
            currentBatch = arr[0] + 1;
            batchSize = arr[1];
        }
        if (CollectionUtil.isEmpty(ids)) {
            throw new IllegalArgumentException("local id is empty");
        }
        if (config == null || StrUtil.isEmpty(config.getServerUrl())) {
            throw new IllegalArgumentException("server config missing");
        }
        long start = System.currentTimeMillis();
        EcdhPsiClient client = new EcdhPsiClient();
        // 加密我自己的数据
        Map<Long, String> clientEncryptedDatasetMap = client.encryptClientOriginalDataset(ids);
        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        // 发给服务端
        request.setClientIds(EcdhUtil.convert2List(clientEncryptedDatasetMap));
        request.setRequestId(config.getRequestId());
        request.setCurrentBatch(currentBatch);
        request.setBatchSize(batchSize);
        request.setType(Psi.ECDH_PSI);
        PrivateSetIntersectionService privateSetIntersectionService = new PrivateSetIntersectionService();
        logger.info("ecdh psi request = " + request);
        QueryPrivateSetIntersectionResponse response = privateSetIntersectionService.handle(config, request);
        if (response.getCode() != 0) {
            logger.error("ecdh psi request error, response = " + JSONObject.toJSONString(response));
            // TODO 是否需要重试？
            throw new Exception(response.getMessage());
        }
        boolean hasNext = response.isHasNext();
        // 获取服务端id, 加密服务端ID
        client.encryptServerDataset(response.getServerEncryptIds());
        logger.info("ecdh psi response serverIds size = " + CollectionUtils.size(response.getServerEncryptIds())
                + ", clientIds size = " + CollectionUtils.size(response.getClientIdByServerKeys()));
        // 客户端进行转换成椭圆曲线上的点
        client.convertDoubleEncryptedClientDataset2ECPoint(EcdhUtil.convert2Map(response.getClientIdByServerKeys()));
        Set<String> batchResult = client.psi();
        List<String> result = new ArrayList<>();
        if (batchResult != null && !batchResult.isEmpty()) {
            if (clientDatasetMap != null && !clientDatasetMap.isEmpty()) {
                batchResult = batchResult.stream().map(s -> clientDatasetMap.get(s)).collect(Collectors.toSet());
            }
            result.addAll(batchResult);
        }
        saveLastCurrentBatchAndSize(request.getRequestId(), request.getCurrentBatch(), request.getBatchSize());
        savePsiResult(batchResult, request.getRequestId());
        logger.info("ecdh psi result, currentBatch = " + request.getCurrentBatch() + ", all psi result size = "
                + result.size() + ", hasNext = " + hasNext + ",duration = " + (System.currentTimeMillis() - start));
        while (hasNext) {
            start = System.currentTimeMillis();
            // 发给服务端
            request.setClientIds(null);// 只需要第一次传给服务端
            request.setCurrentBatch(request.getCurrentBatch() + 1);
            privateSetIntersectionService = new PrivateSetIntersectionService();
            logger.info("ecdh psi request = " + request);
            response = privateSetIntersectionService.handle(config, request);
            if (response.getCode() != 0) {
                logger.error("ecdh psi request error, response = " + JSONObject.toJSONString(response));
                throw new Exception(response.getMessage());
            }
            logger.info("ecdh psi response serverIds size = " + CollectionUtils.size(response.getServerEncryptIds())
                    + ", clientIds size = " + CollectionUtils.size(response.getClientIdByServerKeys()));
            hasNext = response.isHasNext();
            // 获取服务端id, 加密服务端ID
            client.encryptServerDataset(response.getServerEncryptIds());
            // 客户端进行转换成椭圆曲线上的点
//            client.convertDoubleEncryptedClientDataset2ECPoint(
//                    EcdhUtil.convert2Map(response.getClientIdByServerKeys()));
            batchResult = client.psi();
            if (batchResult != null && !batchResult.isEmpty()) {
                if (clientDatasetMap != null && !clientDatasetMap.isEmpty()) {
                    batchResult = batchResult.stream().map(s -> clientDatasetMap.get(s)).collect(Collectors.toSet());
                }
                result.addAll(batchResult);
            }
            saveLastCurrentBatchAndSize(request.getRequestId(), request.getCurrentBatch(), request.getBatchSize());
            savePsiResult(batchResult, request.getRequestId());
            logger.info("ecdh psi result, currentBatch = " + request.getCurrentBatch() + ", all psi result size = "
                    + result.size() + ", hasNext = " + hasNext + ",duration = " + (System.currentTimeMillis() - start));
        }
        return result;
    }
}
