package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractUnionNodeConfigMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = MongodbTable.Union.UNION_NODE_CONFIG)
public class UnionNodeSm2Config extends AbstractUnionNodeConfigMongoModel {

    private String privateKey;
    private String publicKey;


    public UnionNodeSm2Config(String configType) {
        this.configType = configType;
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
