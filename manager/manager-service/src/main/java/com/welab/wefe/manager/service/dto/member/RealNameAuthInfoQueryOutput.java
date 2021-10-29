package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.data.mongodb.entity.union.ext.RealNameAuthFileInfo;
import com.welab.wefe.common.web.dto.AbstractApiOutput;

import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class RealNameAuthInfoQueryOutput extends AbstractApiOutput {
    private String memberId;
    private boolean realNameAuth;
    private String principalName;
    private String authType;
    private String description;
    private List<RealNameAuthFileInfo> realNameAuthFileInfoList;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isRealNameAuth() {
        return realNameAuth;
    }

    public void setRealNameAuth(boolean realNameAuth) {
        this.realNameAuth = realNameAuth;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RealNameAuthFileInfo> getRealNameAuthFileInfoList() {
        return realNameAuthFileInfoList;
    }

    public void setRealNameAuthFileInfoList(List<RealNameAuthFileInfo> realNameAuthFileInfoList) {
        this.realNameAuthFileInfoList = realNameAuthFileInfoList;
    }
}
