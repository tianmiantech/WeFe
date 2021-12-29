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
import java.util.Random;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class DiffieHellmanUtil {

    public static DiffieHellmanKey generateKey(int keySize) {
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

    public static BigInteger encrypt(String value, BigInteger key, BigInteger p) {
        return encrypt(value, key, p, true);
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
    public static BigInteger encrypt(String value, BigInteger key, BigInteger p, boolean hash) {
        if (hash) {
            value = EncryptUtil.encrypt(value, AccountEncryptionType.md5.name());
        }
        BigInteger integer = new BigInteger(value, 16);
        return integer.modPow(key, p);
    }

    public static BigInteger generateRandomKey(int keySize) {
        return new BigInteger(keySize, new Random());
    }

}
