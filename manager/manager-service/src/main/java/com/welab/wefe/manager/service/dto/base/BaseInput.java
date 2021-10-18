package com.welab.wefe.manager.service.dto.base;

import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @author hunter.zhao
 * @date 2020/6/17
 */
public class BaseInput extends AbstractApiInput {
    // @Check(require = true)
    public String curMemberId;

    public String getCurMemberId() {
        return curMemberId;
    }

    public void setCurMemberId(String curMemberId) {
        this.curMemberId = curMemberId;
    }
}
