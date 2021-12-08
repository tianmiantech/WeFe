package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = MongodbTable.Union.UNION_NODE_CONFIG)
public class UnionNodeConfig extends AbstractNormalMongoModel {
    private String nodeId = UUID.randomUUID().toString().replaceAll("-", "");
    private String privateKey;
    private String publicKey;
    private String configType;

    public UnionNodeConfig(String configType){
        this.configType = configType;
    }
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
