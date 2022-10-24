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
public class CertRequestVO implements Serializable {

    private static final long serialVersionUID = -4440215451372304317L;

    private String pkId;

    private String userId;

    private String subjectKeyId;

//    private String certRequestContent;

    private String pCertId;

    private String subjectOrg;

    @JSONField(name = "subject_cn")
    private String subjectCN;

    private Boolean issue;

    private String pCertUserId;

    private long createTime;

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

    public String getSubjectKeyId() {
        return subjectKeyId;
    }

    public void setSubjectKeyId(String subjectKeyId) {
        this.subjectKeyId = subjectKeyId;
    }
//
//    public String getCertRequestContent() {
//        return certRequestContent;
//    }
//
//    public void setCertRequestContent(String certRequestContent) {
//        this.certRequestContent = certRequestContent;
//    }

    public String getpCertId() {
        return pCertId;
    }

    public void setpCertId(String pCertId) {
        this.pCertId = pCertId;
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

    public String getpCertUserId() {
        return pCertUserId;
    }

    public void setpCertUserId(String pCertUserId) {
        this.pCertUserId = pCertUserId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
