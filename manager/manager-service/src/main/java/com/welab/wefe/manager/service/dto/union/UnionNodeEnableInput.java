package com.welab.wefe.manager.service.dto.union;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class UnionNodeEnableInput extends BaseInput {
    @Check(require = true)
    private String unionNodeId;
    @Check(require = true)
    private Boolean enable;

    public String getUnionNodeId() {
        return unionNodeId;
    }

    public void setUnionNodeId(String unionNodeId) {
        this.unionNodeId = unionNodeId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
