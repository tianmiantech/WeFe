package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class RealNameAuthInput extends BaseInput {
    @Check(require = true)
    private String id;
    @Check(require = true)
    private int realNameAuthStatus;
    private String auditComment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRealNameAuthStatus() {
        return realNameAuthStatus;
    }

    public void setRealNameAuthStatus(int realNameAuthStatus) {
        this.realNameAuthStatus = realNameAuthStatus;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }
}
