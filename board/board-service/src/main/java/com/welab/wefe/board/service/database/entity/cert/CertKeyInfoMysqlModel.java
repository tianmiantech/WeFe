package com.welab.wefe.board.service.database.entity.cert;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.exception.StatusCodeWithException;

@Entity(name = "cert_key_info")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class CertKeyInfoMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -7493726478506825680L;

    @Column(name = "key_pem")
    private String keyPem;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "key_alg")
    private String keyAlg;

    public String getKeyPem() throws StatusCodeWithException {
        return keyPem;
    }

    public void setKeyPem(String keyPem) throws StatusCodeWithException {
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
