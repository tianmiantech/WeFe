package com.webank.cert.toolkit.enums;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wesleywang
 */
@Getter
@ToString
@Slf4j
public enum CertDigestAlgEnums {

    SHA256WITHRSA("RSA", "SHA256WITHRSA"), SHA256WITHECDSA("ECDSA", "SHA256WITHECDSA"), SM3WITHSM2("SM2", "SM3WITHSM2");

    private CertDigestAlgEnums(String keyAlgorithm, String algorithmName) {
        this.keyAlgorithm = keyAlgorithm;
        this.algorithmName = algorithmName;
    }

    private String keyAlgorithm;
    private String algorithmName;

    public static CertDigestAlgEnums getByKeyAlg(String keyAlgorithm) {
        for (CertDigestAlgEnums type : CertDigestAlgEnums.values()) {
            if (type.getKeyAlgorithm().equals(keyAlgorithm)) {
                return type;
            }
        }
        return null;
    }

    public static CertDigestAlgEnums getByAlgName(String algorithmName) {
        for (CertDigestAlgEnums type : CertDigestAlgEnums.values()) {
            if (type.getAlgorithmName().equals(algorithmName)) {
                return type;
            }
        }
        return null;
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

}
