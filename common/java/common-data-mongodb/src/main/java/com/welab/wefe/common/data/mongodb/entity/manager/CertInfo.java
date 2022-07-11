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

import org.springframework.data.mongodb.core.mapping.Document;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;

/**
 * @author wesleywang
 */
@Document(collection = MongodbTable.CERT_INFO)
public class CertInfo extends AbstractNormalMongoModel {

    private static final long serialVersionUID = 2536000530329139954L;

    private String pkId;

    private String userId;

    private String issuerKeyId;

    private String subjectKeyId;

    private String subjectPubKey;

    private String serialNumber;

    private String certContent;

    private String pCertId;

    private String issuerOrg;

    private String issuerCN;

    private String subjectOrg;

    private String subjectCN;

    private Boolean isCACert;

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
}
