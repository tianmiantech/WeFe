package com.welab.wefe.union.service.dto.dataresource;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.union.service.dto.base.BaseInput;

public class AbstractDataResourceInput extends BaseInput {
    @Check(require = true)
    protected String dataResourceId;
    @Check(require = true)
    protected String memberId;

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
