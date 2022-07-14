package com.webank.cert.mgr.model.vo;

import java.io.Serializable;

/**
 * @author wesleywang
 */
public class CertRequestVO implements Serializable {

    private static final long serialVersionUID = -4440215451372304317L;

    private String pkId;

    private String userId;

    private String subjectKeyId;

    private String certRequestContent;

    private String pCertId;

    private String subjectOrg;

    private String subjectCN;

    private Boolean issue;

    private String pCertUserId;

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

    public String getCertRequestContent() {
        return certRequestContent;
    }

    public void setCertRequestContent(String certRequestContent) {
        this.certRequestContent = certRequestContent;
    }

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

    @Override
    public String toString() {
        return "CertRequestVO [pkId=" + pkId + ", userId=" + userId + ", subjectKeyId=" + subjectKeyId
                + ", certRequestContent=" + certRequestContent + ", pCertId=" + pCertId + ", subjectOrg=" + subjectOrg
                + ", subjectCN=" + subjectCN + ", issue=" + issue + ", pCertUserId=" + pCertUserId + "]";
    }

}
