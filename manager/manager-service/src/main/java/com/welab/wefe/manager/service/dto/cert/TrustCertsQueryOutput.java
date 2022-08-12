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

package com.welab.wefe.manager.service.dto.cert;

import com.welab.wefe.common.web.dto.AbstractTimedApiOutput;

/**
 * @author yuxin.zhang
 * @date 2022/08/11
 */
public class TrustCertsQueryOutput extends AbstractTimedApiOutput {
    private String certId;
    private String memberId;
    private String serialNumber;
    private String certContent;
    private String pCertId;
    private String issuerOrg;
    private String issuerCn;
    private String subjectOrg;
    private String subjectCn;
    private boolean isCaCert;
    private boolean isRootCert;
    private int status;



    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public boolean isCaCert() {
        return isCaCert;
    }

    public void setCaCert(boolean caCert) {
        isCaCert = caCert;
    }

    public boolean isRootCert() {
        return isRootCert;
    }

    public void setRootCert(boolean rootCert) {
        isRootCert = rootCert;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
