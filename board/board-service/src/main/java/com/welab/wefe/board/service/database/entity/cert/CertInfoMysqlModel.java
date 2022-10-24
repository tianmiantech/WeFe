package com.welab.wefe.board.service.database.entity.cert;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

@Entity(name = "cert_info")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class CertInfoMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = 3983194628565221216L;

    // 用户ID
    @Column(name = "member_id")
    private String memberId;

    // 申请人公钥内容
    @Column(name = "subject_pub_key")
    private String subjectPubKey;

    // 申请人组织名称
    @Column(name = "subject_org")
    private String subjectOrg;

    // 申请人常用名
    @Column(name = "subject_cn")
    private String subjectCN;

    // 证书序列号
    @Column(name = "serial_number")
    private String serialNumber;

    // 证书内容
    @Column(name = "cert_content")
    private String certContent;

    // 证书请求ID
    @Column(name = "csr_id")
    private String csrId;

    // 证书状态
    @Column(name = "status")
    private String status;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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
