/**
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

package com.welab.wefe.fusion.core.utils;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author hunter.zhao
 */
public class CryptoUtils {

    public static AsymmetricCipherKeyPair generateKeys(int keySize) {
        RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
        gen.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(),
                keySize, 80));

        return gen.generateKeyPair();
    }

    public static byte[][] sign(AsymmetricCipherKeyPair keyPair, byte[][] query) {
        try {
            RSAPrivateCrtKeyParameters sk = (RSAPrivateCrtKeyParameters) keyPair.getPrivate();
            BigInteger N = sk.getModulus();
            BigInteger d = sk.getExponent();
            byte[][] bs = new byte[query.length][];
            for (int i = 0; i < query.length; i++) {
                BigInteger x = PSIUtils.bytesToBigInteger(query[i], 0, query[i].length);
                BigInteger y = x.modPow(d, N);
                bs[i] = PSIUtils.bigIntegerToBytes(y, false);
            }

            return bs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1][1]);
    }

    public static byte[][] sign(BigInteger N, BigInteger d, byte[][] query) {
        try {
            byte[][] bs = new byte[query.length][];
            for (int i = 0; i < query.length; i++) {
                BigInteger x = PSIUtils.bytesToBigInteger(query[i], 0, query[i].length);
                BigInteger y = x.modPow(d, N);
                bs[i] = PSIUtils.bigIntegerToBytes(y, false);
            }

            return bs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1][1]);
    }

    public static byte[] sign(BigInteger N, BigInteger d, byte[] query) {
        try {
            BigInteger x = PSIUtils.bytesToBigInteger(query, 0, query.length);
            BigInteger y = x.modPow(d, N);
            return PSIUtils.bigIntegerToBytes(y, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1]);
    }


    public static byte[][] getPK(AsymmetricCipherKeyPair keyPair) {
        byte[][] ret = new byte[2][];
        RSAKeyParameters pk = (RSAKeyParameters) keyPair.getPublic();
        BigInteger e = pk.getExponent();
        BigInteger N = pk.getModulus();
        ret[0] = PSIUtils.bigIntegerToBytes(e, false);
        ret[1] = PSIUtils.bigIntegerToBytes(N, false);

        return ret;
    }
}
