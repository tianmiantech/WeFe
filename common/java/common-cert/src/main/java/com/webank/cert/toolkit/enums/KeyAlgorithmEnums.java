package com.webank.cert.toolkit.enums;

/**
 * @author wesleywang
 */
public enum KeyAlgorithmEnums {

    RSA("RSA"), ECDSA("ECDSA"), SM2("SM2");

    private String keyAlgorithm;

    public static KeyAlgorithmEnums getByKeyAlg(String keyAlgorithm) {
        for (KeyAlgorithmEnums type : KeyAlgorithmEnums.values()) {
            if (type.getKeyAlgorithm().equals(keyAlgorithm)) {
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

    private KeyAlgorithmEnums(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

}
