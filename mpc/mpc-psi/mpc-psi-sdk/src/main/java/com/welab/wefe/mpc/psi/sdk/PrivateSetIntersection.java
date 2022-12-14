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
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PrivateSetIntersection implements Psi {

    private static final Logger logger = LoggerFactory.getLogger(PrivateSetIntersection.class);

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
     * @param config    服务器的连接信息
     * @param clientIds 本方id集
     * @param keySize   密钥安全长度
     * @param operator  自定义列表运算结果,默认求两个列表交集
     * @return
     * @throws Exception
     */
    public List<String> query(CommunicationConfig config, List<String> clientIds) throws Exception {
        return query(config, clientIds, DEFAULT_CURRENT_BATH);
    }

    @Override
    public List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch) throws Exception {
        return query(config, clientIds, DEFAULT_CURRENT_BATH, DEFAULT_BATCH_SIZE);
    }

    @Override
    public List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch, int batchSize)
            throws Exception {
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
        request.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        request.setCurrentBatch(currentBatch);
        request.setType(Psi.DH_PSI);
        request.setBatchSize(batchSize);
        PrivateSetIntersectionService privateSetIntersectionService = new PrivateSetIntersectionService();
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
        Set<String> allResult = client.psi();
        logger.info("dh psi result, currentBatch = " + request.getCurrentBatch() + ", all psi result size = "
                + allResult.size() + ", hasNext = " + hasNext + ", duration = " + (System.currentTimeMillis() - start));
        while (hasNext) {
            start = System.currentTimeMillis();
            // 发给服务端
            request.setClientIds(null);// 只需要第一次传给服务端
            request.setCurrentBatch(request.getCurrentBatch() + 1);
            privateSetIntersectionService = new PrivateSetIntersectionService();
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
            Set<String> batchResult = client.psi();
            if (batchResult != null && !batchResult.isEmpty()) {
                allResult.addAll(batchResult);
            }
            logger.info("dh psi result, currentBatch = " + request.getCurrentBatch() + ", all psi result size = "
                    + allResult.size() + ", hasNext = " + hasNext + ",duration = "
                    + (System.currentTimeMillis() - start));
        }
        return new ArrayList<>(allResult);
    }
}
