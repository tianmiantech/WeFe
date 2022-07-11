package com.webank.cert.mgr.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wesleywang
 */
public enum KeyAlgorithmEnums {

    RSA("RSA"),
    ECDSA("ECDSA"),
    SM2("SM2");

    private KeyAlgorithmEnums(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    protected static final Logger LOG = LoggerFactory.getLogger(KeyAlgorithmEnums.class);

    private String keyAlgorithm;

    public static KeyAlgorithmEnums getByKeyAlg(String keyAlgorithm){
        for(KeyAlgorithmEnums type : KeyAlgorithmEnums.values()){
            if(type.getKeyAlgorithm().equals(keyAlgorithm)){
                return type;
            }
        }
        LOG.error("keyAlgorithm type {} can't be converted.", keyAlgorithm);
        return null;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }
    
}
