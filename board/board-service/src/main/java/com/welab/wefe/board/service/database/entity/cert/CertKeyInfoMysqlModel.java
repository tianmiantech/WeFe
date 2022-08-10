package com.welab.wefe.board.service.database.entity.cert;

import javax.persistence.Entity;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

@Entity(name = "cert_key_info")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class CertKeyInfoMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -7493726478506825680L;

    private String keyPem;

    private String memberId;

    private String keyAlg;

    public String getKeyPem() {
        return keyPem;
    }

    public void setKeyPem(String keyPem) {
        this.keyPem = keyPem;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getKeyAlg() {
        return keyAlg;
    }

    public void setKeyAlg(String keyAlg) {
        this.keyAlg = keyAlg;
    }

}
