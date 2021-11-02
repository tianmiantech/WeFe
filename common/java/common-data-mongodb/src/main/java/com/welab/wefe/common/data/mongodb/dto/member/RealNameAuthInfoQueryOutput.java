package com.welab.wefe.common.data.mongodb.dto.member;

import com.welab.wefe.common.data.mongodb.entity.union.ext.RealNameAuthFileInfo;

import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class RealNameAuthInfoQueryOutput {

    private boolean realNameAuth;
    private String principalName;
    private String reviewComments;
    private String authType;
    private String description;
    private List<String> fileIdList;


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

    public List<String> getFileIdList() {
        return fileIdList;
    }

    public void setFileIdList(List<String> fileIdList) {
        this.fileIdList = fileIdList;
    }

    public String getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }
}
