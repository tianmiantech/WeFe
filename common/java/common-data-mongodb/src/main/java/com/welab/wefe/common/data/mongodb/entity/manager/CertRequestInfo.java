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
@Document(collection = MongodbTable.CERT_REQUEST_INFO)
public class CertRequestInfo extends AbstractNormalMongoModel {

    private static final long serialVersionUID = 7150886210876056683L;

    // 主键ID
    private String pkId = UUID.randomUUID().toString().replaceAll("-", "");

    // 用户ID
    private String memberId;

    // 申请人私钥ID
    private String subjectKeyId;

    // 申请人组织名称
    private String subjectOrg;

    // 申请人常用名
    private String subjectCN;

    // 证书申请内容
    private String certRequestContent;

    // 签发机构的证书ID
    private String issuerCertId;

    // 签发机构的证书的用户ID
    private String issuerCertUserId;

    // 是否签发
    private Boolean issue;
    
    // 创建人
    private String createdBy;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getSubjectKeyId() {
        return subjectKeyId;
    }

    public void setSubjectKeyId(String subjectKeyId) {
        this.subjectKeyId = subjectKeyId;
    }

    public String getCertRequestContent() {
        return certRequestContent;
    }

    public void setCertRequestContent(String certRequestContent) {
        this.certRequestContent = certRequestContent;
    }

    public String getIssuerCertId() {
        return issuerCertId;
    }

    public void setIssuerCertId(String issuerCertId) {
        this.issuerCertId = issuerCertId;
    }

    public String getIssuerCertUserId() {
        return issuerCertUserId;
    }

    public void setIssuerCertUserId(String issuerCertUserId) {
        this.issuerCertUserId = issuerCertUserId;
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

    public Boolean getIssue() {
        return issue;
    }

    public void setIssue(Boolean issue) {
        this.issue = issue;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
