package com.welab.wefe.board.service.database.entity.cert;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

@Entity(name = "cert_request_info")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class CertRequestInfoMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -6973794829218983299L;

    // 用户ID
    @Column(name = "member_id")
    private String memberId;

    // 申请人私钥ID
    @Column(name = "subject_key_id")
    private String subjectKeyId;

    // 申请人组织名称
    @Column(name = "subject_org")
    private String subjectOrg;

    // 申请人常用名
    @Column(name = "subject_cn")
    private String subjectCN;

    // 证书申请内容
    @Column(name = "cert_request_content")
    private String certRequestContent;

    // 是否签发
    @Column(name = "issue")
    private Boolean issue;

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

}
