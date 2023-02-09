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
    // -1认证失败 /0未认证 /1认证中 /2已认证
    private Integer realNameAuthStatus;
    private String servingBaseUrl;
    private String reporter;
    private String principalName;
    private String authType;
    private String auditComment;
    private Long realNameAuthTime;
    private Long updatedTime;
    private String description;
    private List<RealnameAuthFileInfo> realnameAuthFileInfoList;
    private SecretKeyType secretKeyType;

    // 证书相关字段
    // 证书请求内容
    private String certRequestContent;
    // 证书请求ID
    private String certRequestId;
    // 证书pem内容
    private String certPemContent;
    // 证书序列号
    private String certSerialNumber;
    // 证书状态
    private String certStatus;
    // 是否开启TLS通信
    private Boolean memberGatewayTlsEnable;

    public Integer getRealNameAuthStatus() {
        return realNameAuthStatus;
    }

    public String getServingBaseUrl() {
        return servingBaseUrl;
    }

    public void setServingBaseUrl(String servingBaseUrl) {
        this.servingBaseUrl = servingBaseUrl;
    }

    public void setRealNameAuthStatus(Integer realNameAuthStatus) {
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

    public Long getRealNameAuthTime() {
        return realNameAuthTime;
    }

    public void setRealNameAuthTime(Long realNameAuthTime) {
        this.realNameAuthTime = realNameAuthTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }

    public String getCertRequestContent() {
        return certRequestContent;
    }

    public void setCertRequestContent(String certRequestContent) {
        this.certRequestContent = certRequestContent;
    }

    public String getCertPemContent() {
        return certPemContent;
    }

    public void setCertPemContent(String certPemContent) {
        this.certPemContent = certPemContent;
    }

    public String getCertSerialNumber() {
        return certSerialNumber;
    }

    public void setCertSerialNumber(String certSerialNumber) {
        this.certSerialNumber = certSerialNumber;
    }

    public String getCertRequestId() {
        return certRequestId;
    }

    public void setCertRequestId(String certRequestId) {
        this.certRequestId = certRequestId;
    }

    public Boolean getMemberGatewayTlsEnable() {
        return memberGatewayTlsEnable;
    }

    public void setMemberGatewayTlsEnable(Boolean memberGatewayTlsEnable) {
        this.memberGatewayTlsEnable = memberGatewayTlsEnable;
    }

    public String getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

}
