/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
