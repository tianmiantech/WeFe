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

import com.welab.wefe.mpc.key.DiffieHellmanKey;
import junit.framework.TestCase;
import org.junit.Assert;

import java.math.BigInteger;
import java.util.Random;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class DiffieHellmanUtilTest extends TestCase {

    public void testGenerateKey() {
        DiffieHellmanKey key = DiffieHellmanUtil.generateKey(1024);
        System.out.println("g=" + key.getG().toString(16) + "\np=" + key.getP().toString(16));
    }

    public void testEncrypt() {
        DiffieHellmanKey diffieHellmanKey = DiffieHellmanUtil.generateKey(1024);
        BigInteger key = new BigInteger(1024, new Random(2048));
        String value = "123456";
        BigInteger encryptValue = DiffieHellmanUtil.encrypt(value, key, diffieHellmanKey.getP(), true);
        Assert.assertEquals("6c086d25ab029377f4f47c3f317660303923afedbb7915e0c36c51b0466d371cf85bc57f5985501d2ffc97cb8d0a2b6117f0f3f9d2736b64633904a3646a6e9db3f9bd31a0f771fea59b23ca9799fb0a30950a90cc1820c4a950180acdefa397710e6a309c8d2c94cb5f3f84782249133fc5d718b16f99abd4ddade69e6e0e03", encryptValue.toString(16));

        String newValue = "6c086d25ab029377f4f47c3f317660303923afedbb7915e0c36c51b0466d371cf85bc57f5985501d2ffc97cb8d0a2b6117f0f3f9d2736b64633904a3646a6e9db3f9bd31a0f771fea59b23ca9799fb0a30950a90cc1820c4a950180acdefa397710e6a309c8d2c94cb5f3f84782249133fc5d718b16f99abd4ddade69e6e0e03";
        BigInteger key2 = new BigInteger(1024, new Random(4096));
        BigInteger encryptValue2 = DiffieHellmanUtil.encrypt(newValue, key2, diffieHellmanKey.getP(), false);
        Assert.assertEquals("5cee091e23b0d284ea60d2e51051951e0bb9bb8812f04fb4f80f79e99749dd96866692de6ca0ea86eb276d569eb3ee1dd18a6b798213a99763f1702cb6a8fac35557ad1daeb287bd6d7ccf9ad11a3f5eb4d8f4f032baf82c1e97852d5df3666840f285b2c2f32eaece5c1feb2b3b3283ad392e02028cb162d3fb43e7c59f5b43", encryptValue2.toString(16));
    }

    public void testDHEncrypt() {
        int keySize = 1024;
        String phone = "13012341234";
        DiffieHellmanKey diffieHellmanKey = DiffieHellmanUtil.generateKey(keySize);
        BigInteger x = DiffieHellmanUtil.generateRandomKey(keySize);
        BigInteger y = DiffieHellmanUtil.generateRandomKey(keySize);

        BigInteger encryptByX = DiffieHellmanUtil.encrypt(phone, x, diffieHellmanKey.getP());
        BigInteger encryptByXY = DiffieHellmanUtil.encrypt(encryptByX.toString(16), y, diffieHellmanKey.getP(), false);

        BigInteger encryptByY = DiffieHellmanUtil.encrypt(phone, y, diffieHellmanKey.getP());
        BigInteger encryptByYX = DiffieHellmanUtil.encrypt(encryptByY.toString(16), x, diffieHellmanKey.getP(), false);
        Assert.assertEquals(encryptByXY, encryptByYX);
    }

    public void testGenerateRandomKey() {
        BigInteger value = DiffieHellmanUtil.generateRandomKey(1024);
        System.out.println(value.bitLength());
    }
}