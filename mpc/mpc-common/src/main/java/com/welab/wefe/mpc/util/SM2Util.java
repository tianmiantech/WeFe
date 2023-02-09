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

package com.welab.wefe.mpc.util;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;

/**
 * @author yuxin.zhang
 */
public class SM2Util {
    private static final Logger LOG = LoggerFactory.getLogger(SM2Util.class);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static Sm2KeyPair generateKeyPair() {

        KeyPair keyPair = null;
        try {
            ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            kpg.initialize(sm2Spec, new SecureRandom());
            keyPair = kpg.generateKeyPair();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
            return new Sm2KeyPair(
                    new String(Hex.encode(publicKey.getQ().getEncoded(false))),
                    privateKey.getD().toString(16));

        } catch (InvalidAlgorithmParameterException e) {
            LOG.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    public static class Sm2KeyPair {
        public String publicKey;
        public String privateKey;

        public Sm2KeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }

    /**
     * Public key format conversion: Convert from string type to PublicKey type
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
        ECParameterSpec ecParameterSpec = new ECParameterSpec(parameters.getCurve(),
                parameters.getG(), parameters.getN(), parameters.getH());
        // 将公钥HEX字符串转换为椭圆曲线对应的点
        ECPoint ecPoint = parameters.getCurve().decodePoint(Hex.decode(publicKeyStr));
        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        BCECPublicKey key = (BCECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));
        return key;
    }

    /**
     * PrivateKey key format conversion: Convert from string type to PrivateKey type
     */
    public static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        // 将私钥HEX字符串转换为X值
        BigInteger bigInteger = new BigInteger(privateKeyStr, 16);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());

        X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
        ECParameterSpec ecParameterSpec = new ECParameterSpec(parameters.getCurve(),
                parameters.getG(), parameters.getN(), parameters.getH());

        BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(bigInteger,
                ecParameterSpec));

        return privateKey;
    }

    /**
     * The private key signature
     */
    public static String sign(String data, String privateKeyStr) throws Exception {
        Signature signature = Signature.getInstance(
                GMObjectIdentifiers.sm2sign_with_sm3.toString(), new BouncyCastleProvider());

        signature.initSign(getPrivateKey(privateKeyStr));
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = signature.sign();
        // System.out.println(org.apache.commons.codec.binary.Hex.encodeHex(signBytes));
        return Base64.getEncoder().encodeToString(signBytes);

    }

    /**
     * Encrypt data by public key
     */
    public static String encryptByPublicKey(String plaintext, String publicKeyStr) throws Exception {
        BCECPublicKey publicKey = (BCECPublicKey) getPublicKey(publicKeyStr);
        ECParameterSpec ecParameterSpec = publicKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
        return new String(Base64.getEncoder().encodeToString(sm2Engine.processBlock(data, 0, data.length)));
    }

    /**
     * Decrypt by private key
     */
    public static String decryptByPrivateKey(String ciphertext, String privateKeyStr) throws Exception {
        BCECPrivateKey privateKey = (BCECPrivateKey) getPrivateKey(privateKeyStr);
        ECParameterSpec ecParameterSpec = privateKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(privateKey.getD(),
                ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        sm2Engine.init(false, ecPrivateKeyParameters);
        byte[] data = Base64.getDecoder().decode(ciphertext.getBytes(StandardCharsets.UTF_8));
        return new String(sm2Engine.processBlock(data, 0, data.length), StandardCharsets.UTF_8);
    }
}
