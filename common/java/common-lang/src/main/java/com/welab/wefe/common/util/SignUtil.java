/**
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

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * Sign util class
 *
 * <p>
 *     At present, only Ras and SM2 are supported
 * </p>
 *
 * @author aaron.li
 * @date 2022/1/14 14:55
 **/
public class SignUtil {

    /**
     * Generate key pair
     */
    public static KeyPair generateKeyPair(SecretKeyType secretKeyType) throws NoSuchAlgorithmException {
        secretKeyType = (null == secretKeyType ? SecretKeyType.rsa : secretKeyType);
        switch (secretKeyType) {
            case sm2:
                SM2Util.Sm2KeyPair sm2KeyPair = SM2Util.generateKeyPair();
                return new KeyPair(sm2KeyPair.publicKey, sm2KeyPair.privateKey);
            default:
                RSAUtil.RsaKeyPair rsaKeyPair = RSAUtil.generateKeyPair();
                return new KeyPair(rsaKeyPair.publicKey, rsaKeyPair.privateKey);
        }
    }


    /**
     * Generate sign
     *
     * @param data          Data to be signed
     * @param privateKeyStr Secret key
     * @param secretKeyType Secret key type
     * @throws Exception
     */
    public static String sign(String data, String privateKeyStr, SecretKeyType secretKeyType) throws Exception {
        secretKeyType = (null == secretKeyType ? SecretKeyType.rsa : secretKeyType);
        switch (secretKeyType) {
            case sm2:
                return SM2Util.sign(data, privateKeyStr);
            default:
                return RSAUtil.sign(data, privateKeyStr, StandardCharsets.UTF_8.toString());
        }
    }

    /**
     * Sign verify
     *
     * @param data          Data to be verify
     * @param publicKey     public key
     * @param sign          sign
     * @param secretKeyType Secret key type
     */
    public static boolean verify(byte[] data, String publicKey, String sign, SecretKeyType secretKeyType) throws Exception {
        secretKeyType = (null == secretKeyType ? SecretKeyType.rsa : secretKeyType);
        switch (secretKeyType) {
            case sm2:
                return SM2Util.verify(data, SM2Util.getPublicKey(publicKey), sign);
            default:
                return RSAUtil.verify(data, RSAUtil.getPublicKey(publicKey), sign);
        }
    }

    public static class KeyPair {
        public String publicKey;
        public String privateKey;

        public KeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }
}
