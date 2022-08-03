package com.welab.wefe.board.service.database.entity.cert;

import javax.persistence.Entity;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

@Entity(name = "cert_info")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class CertInfoMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = 3983194628565221216L;

    // 用户ID
    private String userId;

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

    // 证书请求ID
    private String csrId;

    // 证书状态
    private String status;

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

    public String getSubjectPubKey() {
        return subjectPubKey;
    }

    public void setSubjectPubKey(String subjectPubKey) {
        this.subjectPubKey = subjectPubKey;
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

    public String getCsrId() {
        return csrId;
    }

    public void setCsrId(String csrId) {
        this.csrId = csrId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
