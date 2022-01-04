package com.welab.wefe.common.data.mongodb.entity.base;

import java.util.UUID;

public class AbstractUnionNodeConfigMongoModel extends AbstractNormalMongoModel{
    protected String nodeId = UUID.randomUUID().toString().replaceAll("-", "");
    protected String configType;


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }
}
