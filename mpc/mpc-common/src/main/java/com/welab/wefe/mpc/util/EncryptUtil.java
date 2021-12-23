/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.mpc.util;


import com.welab.wefe.mpc.commom.AccountEncryptionType;
import com.welab.wefe.mpc.key.DiffieHellmanKey;

import javax.crypto.interfaces.DHPublicKey;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * @author eval
 */
public class EncryptUtil {

    public static String encrypt(String id, String method) {
        AccountEncryptionType encryptionType = AccountEncryptionType.valueOf(method);
        switch (encryptionType) {
            case md5:
                return SHAUtil.SHAMD5(id);
            case sha256:
                return SHAUtil.SHA256(id);
            case sha512:
                return SHAUtil.SHA512(id);
            default:
                return id;
        }
    }


    public static DiffieHellmanKey generateDHKey(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("DH");
            generator.initialize(keySize);
            KeyPair pair = generator.generateKeyPair();
            DHPublicKey publicKey = (DHPublicKey) pair.getPublic();
            DiffieHellmanKey dhKey = new DiffieHellmanKey();
            dhKey.setG(publicKey.getParams().getG());
            dhKey.setP(publicKey.getParams().getP());
            return dhKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 模密运算
     *
     * @param value 待加密的数据, 16进制字符串 或 明文(hash字段为true)
     * @param key   密钥
     * @param p     模
     * @param hash  是否需要哈希
     * @return
     */
    public static BigInteger DhEncrypt(String value, BigInteger key, BigInteger p, boolean hash) {
        if (hash) {
            value = encrypt(value, AccountEncryptionType.md5.name());
        }
        BigInteger integer = new BigInteger(value, 16);
        return integer.modPow(key, p);
    }

}
