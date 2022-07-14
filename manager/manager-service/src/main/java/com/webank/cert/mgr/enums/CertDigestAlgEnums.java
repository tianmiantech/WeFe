package com.webank.cert.mgr.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wesleywang
 * 
 */
public enum CertDigestAlgEnums {

    SHA256WITHRSA("RSA", "SHA256WITHRSA"), SHA256WITHECDSA("ECDSA", "SHA256WITHECDSA"), SM3WITHSM2("SM2", "SM3WITHSM2");

    protected static final Logger LOG = LoggerFactory.getLogger(CertDigestAlgEnums.class);

    // 私钥生成算法
    private String keyAlgorithm;
    // 数字签名算法
    private String algorithmName;

    private CertDigestAlgEnums(String keyAlgorithm, String algorithmName) {
        this.keyAlgorithm = keyAlgorithm;
        this.algorithmName = algorithmName;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public static CertDigestAlgEnums getByKeyAlg(String keyAlgorithm) {
        for (CertDigestAlgEnums type : CertDigestAlgEnums.values()) {
            if (type.getKeyAlgorithm().equals(keyAlgorithm)) {
                return type;
            }
        }
        LOG.error("keyAlgorithm type {} can't be converted.", keyAlgorithm);
        return null;
    }

    public static CertDigestAlgEnums getByAlgName(String algorithmName) {
        for (CertDigestAlgEnums type : CertDigestAlgEnums.values()) {
            if (type.getAlgorithmName().equals(algorithmName)) {
                return type;
            }
        }
        LOG.error("algorithmName type {} error.", algorithmName);
        return null;
    }
}
