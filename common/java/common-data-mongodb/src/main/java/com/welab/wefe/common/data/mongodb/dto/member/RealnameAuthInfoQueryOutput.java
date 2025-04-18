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

package com.welab.wefe.common.data.mongodb.dto.member;

import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class RealnameAuthInfoQueryOutput {

    private int realNameAuthStatus;
    private String principalName;
    private String auditComment;
    private String authType;
    private String description;
    private String realNameAuthUsefulLife;
    
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
    
    private List<FileInfo> fileInfoList;

    public static class FileInfo {
        private String fileId;
        private String filename;

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
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

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public String getRealNameAuthUsefulLife() {
        return realNameAuthUsefulLife;
    }

    public void setRealNameAuthUsefulLife(String realNameAuthUsefulLife) {
        this.realNameAuthUsefulLife = realNameAuthUsefulLife;
    }

    public String getCertRequestContent() {
        return certRequestContent;
    }

    public void setCertRequestContent(String certRequestContent) {
        this.certRequestContent = certRequestContent;
    }

    public String getCertRequestId() {
        return certRequestId;
    }

    public void setCertRequestId(String certRequestId) {
        this.certRequestId = certRequestId;
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

    public String getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    public Boolean getMemberGatewayTlsEnable() {
        return memberGatewayTlsEnable;
    }

    public void setMemberGatewayTlsEnable(Boolean memberGatewayTlsEnable) {
        this.memberGatewayTlsEnable = memberGatewayTlsEnable;
    }
}
