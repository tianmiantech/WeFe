/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.psi.sdk.ecdh;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Objects;

import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

/**
 * 椭圆曲线实现类以及点之间的操作实现 y^2 = x^3 + a*x + b (mod p)
 */
public class EllipticCurve {
    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger ZERO = BigInteger.valueOf(0);

    private BigInteger a;
    private BigInteger b;
    private BigInteger p;
    private BigInteger n;
    private ECPoint g;
    private ECCurve ecCurve;
    private String name;

    private ECParameterSpec ecParameterSpec;

    ECCurve getEcCurve() {
        return this.ecCurve;
    }

    String getName() {
        return this.name;
    }

    ECParameterSpec getEcParameterSpec() {
        return this.ecParameterSpec;
    }

    BigInteger getN() {
        return this.n;
    }

    EllipticCurve(ECParameterSpec params) {
        this.ecParameterSpec = params;
        this.ecCurve = params.getCurve();
        this.name = getNameCurve(this.ecCurve.getA().getFieldSize());
        this.a = this.ecCurve.getA().toBigInteger();
        this.b = this.ecCurve.getB().toBigInteger();
        this.g = params.getG();
        this.p = new BigInteger(getPFromNameCurve(this.name), 16);
        this.n = params.getN();
    }

    /**
     * Calculates square root of res mod p using q as the starting exponent.
     *
     * @param res the residue
     * @param q   the prime number
     * @param p   the prime number
     * @return square root of res mod p or null if none can be found
     */
    private static BigInteger complexSqrtP(BigInteger res, BigInteger q, BigInteger p) {
        BigInteger a = findNonResidue(p);
        if (a == null)
            return null;
        BigInteger t = (p.subtract(ONE)).divide(TWO);
        BigInteger negativePower = t; // a^negativePower mod p = -1 mod p this will be used to get the right power
        // res^q mod p = a^((p-1)/2) mod p

        while (q.mod(TWO).compareTo(ZERO) == 0) {
            q = q.divide(TWO);
            t = t.divide(TWO);
            // check to make sure that the right power was gone
            if (res.modPow(q, p).compareTo(a.modPow(t, p)) != 0) {
                // -(a^t mod p) = a^t*a^negativePower mod p = a^t+(negativePower) mod p
                t = t.add(negativePower);
            }
        }
        BigInteger inverseRes = res.modInverse(p);
        // inverseRes^((q-1)/2)
        q = (q.subtract(ONE)).divide(TWO);
        BigInteger partOne = inverseRes.modPow(q, p);
        // a^(t/2)
        t = t.divide(TWO);
        BigInteger partTwo = a.modPow(t, p);
        BigInteger root;
        root = partOne.multiply(partTwo);
        root = root.mod(p);
        return root;
    }

    /**
     * Checks if the input ECPoint belongs to the current elliptic curve.
     *
     * @param p an ECPoint
     * @return true if the input ECPoint belongs to the curve, false otherwise
     */
    private boolean belongs(ECPoint p) {
        return p.getYCoord().toBigInteger().pow(2).subtract(
                p.getXCoord().toBigInteger().pow(3).add(this.a.multiply(p.getXCoord().toBigInteger())).add(this.b))
                .mod(this.p).intValue() == 0;
    }

    @Override
    public String toString() {
        return "EllipticCurve{" + "A=" + this.a + ", B=" + this.b + ", P=" + this.p + ", N=" + this.n + ", G=" + this.g
                + '}';
    }

    static String getNameCurve(int keySize) {
        if (keySize == 160)
            return "secp160r2";
        else if (keySize == 224)
            return "secp224k1";
        else if (keySize == 256)
            return "prime256v1";
        else if (keySize == 384)
            return "secp384r1";
        else if (keySize == 512 || keySize == 521)
            return "secp521r1";
        else
            throw new RuntimeException("Input key size (" + keySize
                    + ") currently not supported for EC algorithms (ECDH and ECRSA). Supported values are 160, 224, 256, 384, 512 or 521.");
    }

    private static String getPFromNameCurve(String name) {
        if (Objects.equals(name, "secp160r2"))
            return "fffffffffffffffffffffffffffffffeffffac73";
        else if (Objects.equals(name, "secp224k1"))
            return "fffffffffffffffffffffffffffffffffffffffffffffffeffffe56d";
        else if (Objects.equals(name, "prime256v1"))
            return "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff";
        else if (Objects.equals(name, "secp384r1"))
            return "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000ffffffff";
        else if (Objects.equals(name, "secp521r1"))
            return "01ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        else
            throw new RuntimeException("Curve currently not supported.");
    }

    static ECPoint multiply(ECPoint p, BigInteger k) {
        return p.multiply(k);
    }

    private static BigInteger sqrtP(BigInteger res, BigInteger p) {
        BigInteger q = (p.subtract(ONE)).divide(TWO);

        while (q.mod(TWO).compareTo(ZERO) == 0) {
            q = q.divide(TWO);
            // if res^q mod p != 1 run the complicated root find
            if (res.modPow(q, p).compareTo(ONE) != 0) {
                return complexSqrtP(res, q, p);
            }

        }
        // Code gets here if res^q mod p were all 1's and now q is odd
        // then root = res^((q+1)/2) mod p
        q = (q.add(ONE)).divide(TWO);
        return res.modPow(q, p);
    }

    private static BigInteger findNonResidue(BigInteger p) {
        int a = 2;
        BigInteger q = (p.subtract(ONE)).divide(TWO);
        while (true) {
            if (BigInteger.valueOf(a).modPow(q, p).compareTo(ONE) != 0) {
                return BigInteger.valueOf(a);
            }
            // Once tested all the possible values of an integer, something is not working
            if (a == 0)
                return null;
            a++;
        }
    }

    /**
     * Maps the input value to an ECPoint of the current elliptic curve.
     *
     * @param m BigInteger input value
     * @return ECPoint mapping of the input value
     */
    public ECPoint mapMessage(BigInteger m) {
        if (this.p.compareTo(m) < 0)
            throw new RuntimeException("Unexpected: Hashing missing");
        BigInteger k = BigInteger.valueOf(200);
        BigInteger km1 = k.subtract(BigInteger.ONE);
        BigInteger start = m.multiply(k);
        BigInteger y;
        for (BigInteger I = BigInteger.ZERO; I.compareTo(km1) < 0; I = I.add(BigInteger.ONE)) {
            BigInteger x = start.mod(this.p).add(I).mod(this.p);
            y = x.modPow(THREE, this.p).add(this.a.multiply(x).mod(this.p)).mod(this.p).add(this.b).mod(this.p);
            if (y.modPow(this.p.subtract(BigInteger.ONE).multiply(TWO.modInverse(this.p)).mod(this.p), this.p)
                    .compareTo(BigInteger.ONE) == 0) {
                BigInteger r = sqrtP(y, this.p);
                ECPoint res = this.ecCurve.createPoint(x, r);
                if (!belongs(res))
                    throw new RuntimeException("Found mapping outside the curve");
                return res;
            }

        }
        throw new RuntimeException("Failed to map message");
    }

    public ECPoint hashToCurve(BigInteger m) {
        // 计算输入消息的哈希值，尝试构建坐标x和坐标y，如果失败，则继续哈希
        byte[] messageHashBytes = digestToBytes(m.toByteArray());
        while (true) {
            ECFieldElement x, y;
            // 哈希结果不需要模n，交给ECFieldElement判断结果是否合法
            BigInteger messageHash = byteArrayToNonNegBigInteger(messageHashBytes);
            try {
                x = this.ecCurve.fromBigInteger(messageHash);
            } catch (IllegalArgumentException e) {
                // 如果无法将哈希结果转换为坐标x，意味着哈希结果不是有效的椭圆曲线点，重新哈希
                messageHashBytes = digestToBytes(messageHashBytes);
                continue;
            }
            y = x.square().add(this.ecCurve.getA()).multiply(x).add(this.ecCurve.getB()).sqrt();
            if (y == null) {
                // 如果y无解，重新哈希
                messageHashBytes = digestToBytes(messageHashBytes);
                continue;
            }
            ECPoint ecPoint = this.ecCurve.createPoint(x.toBigInteger(), y.toBigInteger());
            // clearing the cofactor
            ecPoint = ecPoint.multiply(this.ecCurve.getCofactor());
            if (ecPoint == null || !ecPoint.isValid()) {
                messageHashBytes = digestToBytes(messageHashBytes);
                continue;
            }
            return ecPoint;
        }
    }

    /**
     * 将{@code byte[]}转换为非负数的{@code BigInteger}。注意：转换过程已经对数据进行了拷贝。
     *
     * @param byteArray 待转换的{@code byte[]}。
     * @return 转换结果。
     */
    public static BigInteger byteArrayToNonNegBigInteger(byte[] byteArray) {
        return new BigInteger(1, byteArray);
    }

    public static byte[] digestToBytes(byte[] bytes) {
        String s = new String(bytes);
        try {
            MessageDigest d = MessageDigest.getInstance("md5");
            return d.digest(s.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
