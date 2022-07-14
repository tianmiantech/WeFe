package com.webank.cert.mgr.model.vo;

import java.io.Serializable;

/**
 * @author wesleywang
 */
public class CertKeyVO implements Serializable {

    private static final long serialVersionUID = 3902274893320956610L;

    private String pkId;

    private String userId;

    private String keyAlg;

    private String keyPem;

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

    public String getKeyAlg() {
        return keyAlg;
    }

    public void setKeyAlg(String keyAlg) {
        this.keyAlg = keyAlg;
    }

    public String getKeyPem() {
        return keyPem;
    }

    public void setKeyPem(String keyPem) {
        this.keyPem = keyPem;
    }

    @Override
    public String toString() {
        return "CertKeyVO [pkId=" + pkId + ", userId=" + userId + ", keyAlg=" + keyAlg + ", keyPem=" + keyPem + "]";
    }

}
