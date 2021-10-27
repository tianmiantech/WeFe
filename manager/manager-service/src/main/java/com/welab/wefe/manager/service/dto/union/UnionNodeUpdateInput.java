package com.welab.wefe.manager.service.dto.union;

import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetDefaultTagExtJSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class UnionNodeUpdateInput extends BaseInput {
    @Check(require = true)
    private String unionNodeId;
    @Check(require = true)
    private String unionBaseUrl;
    private String organizationName;
    private DataSetDefaultTagExtJSON extJson;

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

    public DataSetDefaultTagExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataSetDefaultTagExtJSON extJson) {
        this.extJson = extJson;
    }
}
