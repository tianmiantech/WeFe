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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.operation.ListOperator;
import com.welab.wefe.mpc.psi.sdk.operation.impl.IntersectionOperator;
import com.welab.wefe.mpc.psi.sdk.service.PrivateSetIntersectionService;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class PrivateSetIntersection {

    /**
     * 多方求交集
     * @param configs 服务器的通信配置信息列表
     * @param ids 本方id集
     * @return
     */
    public List<String> query(List<CommunicationConfig> configs, List<String> ids) {
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

    public List<String> query(CommunicationConfig config, List<String> ids) {
        return query(config, ids, 1024);
    }

    public List<String> query(CommunicationConfig config, List<String> ids, int keySize) {
        return query(config, ids, keySize, new IntersectionOperator());
    }

    /**
     * 查询本文id集与服务器id集的集合操作
     *
     * @param config   服务器的连接信息
     * @param ids      本方id集
     * @param keySize  密钥安全长度
     * @param operator 自定义列表运算结果,默认求两个列表交集
     * @return
     */
    public List<String> query(CommunicationConfig config, List<String> ids, int keySize, ListOperator operator) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new IllegalArgumentException("local id is empty");
        }
        if (config == null || StrUtil.isEmpty(config.getServerUrl())) {
            throw new IllegalArgumentException("server config missing");
        }
        if (keySize < 1) {
            keySize = 1024;
        }
        if (operator == null) {
            operator = new IntersectionOperator();
        }

        DiffieHellmanKey diffieHellmanKey = DiffieHellmanUtil.generateKey(keySize);
        BigInteger key = DiffieHellmanUtil.generateRandomKey(keySize);
        List<String> encryptIds = new ArrayList<>(ids.size());
        for (String id : ids) {
            encryptIds.add(DiffieHellmanUtil.encrypt(id, key, diffieHellmanKey.getP()).toString(16));
        }
        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        request.setP(diffieHellmanKey.getP().toString(16));
        request.setClientIds(encryptIds);
        PrivateSetIntersectionService privateSetIntersectionService = new PrivateSetIntersectionService();
        QueryPrivateSetIntersectionResponse response = privateSetIntersectionService.handle(config, request);
        List<String> encryptServerIds = response.getServerEncryptIds();
        List<String> encryptIdWithServerKeys = response.getClientIdByServerKeys();
        List<String> serverIdWithClientKeys = new ArrayList<>(encryptServerIds.size());
        for (String serverId : encryptServerIds) {
            String encryptValue = DiffieHellmanUtil.encrypt(serverId, key, diffieHellmanKey.getP(), false).toString(16);
            serverIdWithClientKeys.add(encryptValue);
        }

        return operator.operator(ids, encryptIdWithServerKeys, serverIdWithClientKeys);
    }
}
