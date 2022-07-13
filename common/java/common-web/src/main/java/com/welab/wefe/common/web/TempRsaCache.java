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

package com.welab.wefe.common.web;

import com.welab.wefe.common.util.RSAUtil;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * 临时 RSA 缓存
 * <p>
 * 使用场景：
 * 1. 给前端分配公钥后传输敏感字段到后端解密
 *
 * @author Zane
 */
public class TempRsaCache {

    /**
     * user id : KeyPair
     */
    private static ExpiringMap<String, RSAUtil.RsaKeyPair> KEY_PAIR_MAP_BY_USER = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(60, TimeUnit.MINUTES)
            .build();

    public static void init() {
    }

    /**
     * 获取一个新的公钥
     */
    public static String getPublicKey() {
        RSAUtil.RsaKeyPair rsaKeyPair = RSAUtil.generateKeyPair();
        KEY_PAIR_MAP_BY_USER.put(CurrentAccount.id(), rsaKeyPair);
        return rsaKeyPair.publicKey;
    }

    /**
     * 解密
     */
    public static String decrypt(String ciphertext) throws Exception {
        RSAUtil.RsaKeyPair rsaKeyPair = KEY_PAIR_MAP_BY_USER.get(CurrentAccount.id());
        return RSAUtil.decryptByPrivateKey(ciphertext, rsaKeyPair.privateKey);
    }


    public static void main(String[] args) throws Exception {
        RSAUtil.RsaKeyPair rsaKeyPair = RSAUtil.generateKeyPair();

        System.out.println("publicKey:" + rsaKeyPair.publicKey);
        System.out.println("PrivateKey:" + rsaKeyPair.privateKey);

        String plaintext = "hello";
        System.out.println("plaintext:" + plaintext);
        String ciphertext = RSAUtil.encryptByPublicKey(plaintext, rsaKeyPair.publicKey);
        System.out.println("ciphertext:" + ciphertext);

        String decrypt = RSAUtil.decryptByPrivateKey(ciphertext, rsaKeyPair.privateKey);
        System.out.println("decrypt:" + decrypt);
    }
}
