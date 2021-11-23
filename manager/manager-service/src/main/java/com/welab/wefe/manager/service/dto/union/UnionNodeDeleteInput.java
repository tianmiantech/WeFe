package com.welab.wefe.manager.service.dto.union;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class UnionNodeDeleteInput extends BaseInput {
    @Check(require = true)
    private String unionNodeId;

    public String getUnionNodeId() {
        return unionNodeId;
    }

    public void setUnionNodeId(String unionNodeId) {
        this.unionNodeId = unionNodeId;
    }
}
