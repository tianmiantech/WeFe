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
