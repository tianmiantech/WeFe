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
