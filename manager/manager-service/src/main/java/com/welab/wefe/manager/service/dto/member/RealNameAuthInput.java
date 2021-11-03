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
    private boolean realNameAuth;
    private String auditComment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRealNameAuth() {
        return realNameAuth;
    }

    public void setRealNameAuth(boolean realNameAuth) {
        this.realNameAuth = realNameAuth;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }
}
