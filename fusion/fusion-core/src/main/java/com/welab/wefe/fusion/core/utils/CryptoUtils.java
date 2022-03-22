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

package com.welab.wefe.fusion.core.utils;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

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
            BigInteger n = sk.getModulus();
            BigInteger d = sk.getExponent();
            byte[][] bs = new byte[query.length][];
            for (int i = 0; i < query.length; i++) {
                BigInteger x = PSIUtils.bytesToBigInteger(query[i], 0, query[i].length);
                BigInteger y = x.modPow(d, n);
                bs[i] = PSIUtils.bigIntegerToBytes(y, false);
            }

            return bs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1][1]);
    }


    public static byte[][] sign(BigInteger n, BigInteger d, List<byte[]> query) {
        try {
            byte[][] bs = new byte[query.size()][];
            for (int i = 0; i < query.size(); i++) {
                bs[i] = query.get(i);
            }

            return sign(n, d, bs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1][1]);
    }

    public static byte[][] sign(BigInteger n, BigInteger d, byte[][] query) {
        try {
            byte[][] bs = new byte[query.length][];
            for (int i = 0; i < query.length; i++) {
                BigInteger x = PSIUtils.bytesToBigInteger(query[i], 0, query[i].length);
                BigInteger y = x.modPow(d, n);
                bs[i] = PSIUtils.bigIntegerToBytes(y, false);
            }

            return bs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1][1]);
    }

    public static byte[][] sign(BigInteger n,
                                BigInteger d,
                                BigInteger p,
                                BigInteger q,
                                BigInteger cp,
                                BigInteger cq,
                                byte[][] query) {
        try {
            byte[][] bs = new byte[query.length][];
            for (int i = 0; i < query.length; i++) {
                BigInteger x = PSIUtils.bytesToBigInteger(query[i], 0, query[i].length);

                //crt优化后
                BigInteger rp = x.modPow(d.remainder(p.subtract(BigInteger.valueOf(1))), p);
                BigInteger rq = x.modPow(d.remainder(q.subtract(BigInteger.valueOf(1))), q);

                BigInteger y = (rp.multiply(cp).add(rq.multiply(cq))).remainder(n);

                bs[i] = PSIUtils.bigIntegerToBytes(y, false);
            }

            return bs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new byte[1][1]);
    }

    public static byte[] sign(BigInteger n, BigInteger d, byte[] query) {
        try {
            BigInteger x = PSIUtils.bytesToBigInteger(query, 0, query.length);
            BigInteger y = x.modPow(d, n);
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
        BigInteger n = pk.getModulus();
        ret[0] = PSIUtils.bigIntegerToBytes(e, false);
        ret[1] = PSIUtils.bigIntegerToBytes(n, false);

        return ret;
    }
}
