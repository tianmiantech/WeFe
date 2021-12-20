/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.union.service.cache;

import com.welab.wefe.common.data.mongodb.entity.base.AbstractUnionNodeConfigMongoModel;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNodeSm2Config;
import com.welab.wefe.union.service.constant.UnionNodeConfigType;

import java.util.concurrent.ConcurrentHashMap;

public class UnionNodeConfigCache {
    private static ConcurrentHashMap<String, AbstractUnionNodeConfigMongoModel> unionNodeConfigMap = new ConcurrentHashMap<>();

    public static String currentBlockchainNodeId;

    public static String getSm2PrivateKey() {
        UnionNodeSm2Config unionNodeSm2Config = (UnionNodeSm2Config) unionNodeConfigMap.get(UnionNodeConfigType.SM2.name());
        return unionNodeSm2Config.getPrivateKey();
    }

    public static String getSm2PublicKey() {
        UnionNodeSm2Config unionNodeSm2Config = (UnionNodeSm2Config) unionNodeConfigMap.get(UnionNodeConfigType.SM2.name());
        return unionNodeSm2Config.getPublicKey();
    }


}
