/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.util;

import com.welab.wefe.common.constant.SecretKeyType;

public class AsymmetricCryptoUtil {

    /**
     * Encrypt data by public key
     *
     * @param plaintext     Plain text
     * @param publicKeyStr  Public key str format
     * @param secretKeyType Secret key type
     */
    public static String encryptByPublicKey(String plaintext, String publicKeyStr, SecretKeyType secretKeyType) throws Exception {
        secretKeyType = (null == secretKeyType ? SecretKeyType.rsa : secretKeyType);
        switch (secretKeyType) {
            case sm2:
                return SM2Util.encryptByPublicKey(plaintext, publicKeyStr);
            default:
                return RSAUtil.encryptByPublicKey(plaintext, publicKeyStr);
        }
    }

    /**
     * Decrypt data by private key
     *
     * @param ciphertext    Cipher text
     * @param privateKeyStr Private key str format
     * @param secretKeyType Secret key type
     */
    public static String decryptByPrivateKey(String ciphertext, String privateKeyStr, SecretKeyType secretKeyType) throws Exception {
        secretKeyType = (null == secretKeyType ? SecretKeyType.rsa : secretKeyType);
        switch (secretKeyType) {
            case sm2:
                return SM2Util.decryptByPrivateKey(ciphertext, privateKeyStr);
            default:
                return RSAUtil.decryptByPrivateKey(ciphertext, privateKeyStr);
        }
    }
}
