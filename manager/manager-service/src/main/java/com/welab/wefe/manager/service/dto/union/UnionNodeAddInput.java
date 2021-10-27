package com.welab.wefe.manager.service.dto.union;

import com.welab.wefe.common.data.mongodb.entity.union.ext.UnionNodeExtJSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

import java.util.UUID;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class UnionNodeAddInput extends BaseInput {
    private String sign;
    @Check(require = true)
    private String unionBaseUrl;
    @Check(require = true)
    private String organizationName;
    private boolean enable = true;
    private UnionNodeExtJSON extJson;



    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public UnionNodeExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(UnionNodeExtJSON extJson) {
        this.extJson = extJson;
    }
}
