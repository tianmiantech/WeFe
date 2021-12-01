package com.welab.wefe.manager.service.dto.union;

import com.welab.wefe.common.data.mongodb.entity.union.ext.UnionNodeExtJSON;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class UnionNodeQueryOutput {
    private String unionNodeId;
    private String unionBaseUrl;
    private String organizationName;
    private int enable;
    private int status;
    private UnionNodeExtJSON extJson;


    public String getUnionNodeId() {
        return unionNodeId;
    }

    public void setUnionNodeId(String unionNodeId) {
        this.unionNodeId = unionNodeId;
    }

    public String getUnionBaseUrl() {
        return unionBaseUrl;
    }

    public void setUnionBaseUrl(String unionBaseUrl) {
        this.unionBaseUrl = unionBaseUrl;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public UnionNodeExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(UnionNodeExtJSON extJson) {
        this.extJson = extJson;
    }
}
