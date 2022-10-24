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
package com.webank.cert.mgr.model.vo;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author wesleywang
 */
public class CertVO implements Serializable {

    private static final long serialVersionUID = 5882479979815938267L;

    private String pkId; // 主键ID

    private String userId; // 用户ID

    private String subjectPubKey; // 公钥

    private String serialNumber; // 证书序列号

    private String certContent; // 证书内容

    private String pCertId; // 父证书ID

    private String issuerOrg; // 签发机构组织名称

    @JSONField(name = "issuer_cn")
    private String issuerCN; // 签发机构常用名称

    private String subjectOrg; // 申请人组织名称

    @JSONField(name = "subject_cn")
    private String subjectCN; // 申请人常用名

    @JSONField(name = "is_ca_cert")
    private Boolean isCACert; // 是否是机构证书

    private String issuerKeyId; // 签发机构私钥ID

    private String subjectKeyId;// 申请人私钥ID

    @JSONField(name = "is_root_cert")
    private Boolean isRootCert; // 是否是根证书

    private String csrId; // 证书请求ID

    private int status; // 证书状态

    private long createTime; // 创建时间

    // 是否在信任库中
    private Boolean canTrust;

    @JSONField(name = "update_status_reason")
    private String updateStatusReason;

    public String getUpdateStatusReason() {
        return updateStatusReason;
    }

    public void setUpdateStatusReason(String updateStatusReason) {
        this.updateStatusReason = updateStatusReason;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubjectPubKey() {
        return subjectPubKey;
    }

    public void setSubjectPubKey(String subjectPubKey) {
        this.subjectPubKey = subjectPubKey;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCertContent() {
        return certContent;
    }

    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    public String getpCertId() {
        return pCertId;
    }

    public void setpCertId(String pCertId) {
        this.pCertId = pCertId;
    }

    public String getIssuerOrg() {
        return issuerOrg;
    }

    public void setIssuerOrg(String issuerOrg) {
        this.issuerOrg = issuerOrg;
    }

    public String getIssuerCN() {
        return issuerCN;
    }

    public void setIssuerCN(String issuerCN) {
        this.issuerCN = issuerCN;
    }

    public String getSubjectOrg() {
        return subjectOrg;
    }

    public void setSubjectOrg(String subjectOrg) {
        this.subjectOrg = subjectOrg;
    }

    public String getSubjectCN() {
        return subjectCN;
    }

    public void setSubjectCN(String subjectCN) {
        this.subjectCN = subjectCN;
    }

    public Boolean getIsCACert() {
        return isCACert;
    }

    public void setIsCACert(Boolean isCACert) {
        this.isCACert = isCACert;
    }

    public String getIssuerKeyId() {
        return issuerKeyId;
    }

    public void setIssuerKeyId(String issuerKeyId) {
        this.issuerKeyId = issuerKeyId;
    }

    public String getSubjectKeyId() {
        return subjectKeyId;
    }

    public void setSubjectKeyId(String subjectKeyId) {
        this.subjectKeyId = subjectKeyId;
    }

    public Boolean getIsRootCert() {
        return isRootCert;
    }

    public void setIsRootCert(Boolean isRootCert) {
        this.isRootCert = isRootCert;
    }

    public String getCsrId() {
        return csrId;
    }

    public void setCsrId(String csrId) {
        this.csrId = csrId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Boolean getCanTrust() {
        return canTrust;
    }

    public void setCanTrust(Boolean canTrust) {
        this.canTrust = canTrust;
    }

}
