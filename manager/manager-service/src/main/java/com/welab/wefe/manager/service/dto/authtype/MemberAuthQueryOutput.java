package com.welab.wefe.manager.service.dto.authtype;

import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberAuthTypeExtJSON;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class MemberAuthQueryOutput {
    private String typeId;
    private String typeName;
    private int status;
    private MemberAuthTypeExtJSON extJson;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MemberAuthTypeExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(MemberAuthTypeExtJSON extJson) {
        this.extJson = extJson;
    }
}
