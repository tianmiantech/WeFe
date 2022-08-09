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

package com.welab.wefe.common.data.mongodb.entity.manager;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;

/**
 * @author wesleywang
 */
@Document(collection = MongodbTable.CERT_INFO)
public class CertInfo extends AbstractNormalMongoModel {

    private static final long serialVersionUID = 2536000530329139954L;

    // 主键ID
    private String pkId = UUID.randomUUID().toString().replaceAll("-", "");

    // 用户ID
    private String userId;

    // 签发机构私钥ID
    private String issuerKeyId;

    // 签发机构组织名称
    private String issuerOrg;

    // 签发机构常用名
    private String issuerCN;

    // 申请人私钥ID
    private String subjectKeyId;

    // 申请人公钥内容
    private String subjectPubKey;

    // 申请人组织名称
    private String subjectOrg;

    // 申请人常用名
    private String subjectCN;

    // 证书序列号
    private String serialNumber;

    // 证书内容
    private String certContent;

    // 父证书ID 签发机构证书ID
    private String pCertId;

    // 是否是CA证书
    private Boolean isCACert;

    // 是否是根证书
    private Boolean isRootCert;

    // 证书请求ID
    private String csrId;

    // 证书状态
    private int status;

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

}
