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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.TrustCertsExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.UnionNodeExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.TRUST_CERTS)
public class TrustCerts extends AbstractBlockChainBusinessModel {
    private String certId;
    private String serialNumber;
    private String certContent;
    private String pCertId;
    private String issuerOrg;
    private String issuerCn;
    private String subjectOrg;
    private String subjectCn;
    private String isCaCert;
    private String isRootCert;
    private TrustCertsExtJSON extJson = new TrustCertsExtJSON();


    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
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

    public String getIssuerCn() {
        return issuerCn;
    }

    public void setIssuerCn(String issuerCn) {
        this.issuerCn = issuerCn;
    }

    public String getSubjectOrg() {
        return subjectOrg;
    }

    public void setSubjectOrg(String subjectOrg) {
        this.subjectOrg = subjectOrg;
    }

    public String getSubjectCn() {
        return subjectCn;
    }

    public void setSubjectCn(String subjectCn) {
        this.subjectCn = subjectCn;
    }

    public String getIsCaCert() {
        return isCaCert;
    }

    public void setIsCaCert(String isCaCert) {
        this.isCaCert = isCaCert;
    }

    public String getIsRootCert() {
        return isRootCert;
    }

    public void setIsRootCert(String isRootCert) {
        this.isRootCert = isRootCert;
    }

    public TrustCertsExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(TrustCertsExtJSON extJson) {
        this.extJson = extJson;
    }
}
