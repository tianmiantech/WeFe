package com.welab.wefe.manager.service.dto.authtype;

import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberAuthTypeExtJSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 */
public class MemberAuthTypeAddInput extends BaseInput {
    @Check(require = true)
    private String typeName;
    private MemberAuthTypeExtJSON extJson;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public MemberAuthTypeExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(MemberAuthTypeExtJSON extJson) {
        this.extJson = extJson;
    }
}

