package com.welab.wefe.common.data.mongodb.entity.union.ext;

import javax.persistence.Column;
import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class MemberExtJSON {
    @Column(name = "real_name_auth")
    private boolean realNameAuth;
    @Column(name = "principalName")
    private String principalName;
    @Column(name = "auth_type")
    private String authType;
    private String reviewComments;
    private String description;
    private List<RealNameAuthFileInfo> realNameAuthFileInfoList;


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

    public String getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }
}
