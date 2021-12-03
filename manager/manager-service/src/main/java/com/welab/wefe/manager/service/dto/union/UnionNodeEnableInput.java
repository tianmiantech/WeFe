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
    private String nodeId;
    @Check(require = true)
    private Boolean enable;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
