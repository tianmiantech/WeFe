package com.webank.cert.mgr.model.vo;

import java.io.Serializable;

/**
 * @author wesleywang
 */
public class CertVO implements Serializable {

    private static final long serialVersionUID = 5882479979815938267L;

    private Long pkId;

    private String userId;

    private String subjectPubKey;

    private String serialNumber;

    private String certContent;

    private Long pCertId;

    private String issuerOrg;

    private String issuerCN;

    private String subjectOrg;

    private String subjectCN;

    private Boolean isCACert;

    private Long issuerKeyId;

    private Long subjectKeyId;

    public Long getPkId() {
        return pkId;
    }

    public void setPkId(Long pkId) {
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

    public Long getpCertId() {
        return pCertId;
    }

    public void setpCertId(Long pCertId) {
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

    public Long getIssuerKeyId() {
        return issuerKeyId;
    }

    public void setIssuerKeyId(Long issuerKeyId) {
        this.issuerKeyId = issuerKeyId;
    }

    public Long getSubjectKeyId() {
        return subjectKeyId;
    }

    public void setSubjectKeyId(Long subjectKeyId) {
        this.subjectKeyId = subjectKeyId;
    }

}
