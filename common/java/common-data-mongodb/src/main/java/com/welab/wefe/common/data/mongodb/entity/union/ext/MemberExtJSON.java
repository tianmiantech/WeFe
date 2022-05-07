/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.data.mongodb.entity.union.ext;

import com.welab.wefe.common.constant.SecretKeyType;

import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class MemberExtJSON {
    private String servingBaseUrl;
    private int realNameAuthStatus;
    private String reporter;
    private String principalName;
    private String authType;
    private String auditComment;
    private long realNameAuthTime;
    private String description;
    private List<RealnameAuthFileInfo> realnameAuthFileInfoList;
    private SecretKeyType secretKeyType = SecretKeyType.rsa;


    public String getServingBaseUrl() {
        return servingBaseUrl;
    }

    public void setServingBaseUrl(String servingBaseUrl) {
        this.servingBaseUrl = servingBaseUrl;
    }

    public int getRealNameAuthStatus() {
        return realNameAuthStatus;
    }

    public void setRealNameAuthStatus(int realNameAuthStatus) {
        this.realNameAuthStatus = realNameAuthStatus;
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


    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }


    public List<RealnameAuthFileInfo> getRealnameAuthFileInfoList() {
        return realnameAuthFileInfoList;
    }

    public void setRealnameAuthFileInfoList(List<RealnameAuthFileInfo> realnameAuthFileInfoList) {
        this.realnameAuthFileInfoList = realnameAuthFileInfoList;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public long getRealNameAuthTime() {
        return realNameAuthTime;
    }

    public void setRealNameAuthTime(long realNameAuthTime) {
        this.realNameAuthTime = realNameAuthTime;
    }

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }
}
