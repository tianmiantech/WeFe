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
/*
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.cert.toolkit.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuzhichu
 * @author wesleywang
 */
public class KeyUtils {

	public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
	public final static BigInteger SM2_ECC_GX = new BigInteger(
			"32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
	public final static BigInteger SM2_ECC_GY = new BigInteger(
			"BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
	public final static BigInteger SM2_ECC_N = CURVE.getOrder();
	public final static BigInteger SM2_ECC_H = CURVE.getCofactor();
	public static final org.bouncycastle.math.ec.ECPoint G_POINT = CURVE.createPoint(SM2_ECC_GX, SM2_ECC_GY);
	public static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT,
			SM2_ECC_N, SM2_ECC_H);
	private static final String PEM_STRING_ECPRIVATEKEY = "EC PRIVATE KEY";

	private static final SecureRandom random = new SecureRandom();

	protected static final Logger LOG = LoggerFactory.getLogger(KeyUtils.class);
	
	public static KeyPair generateKeyPair(){
		KeyPairGenerator keyPairGen = null;
		KeyPair keyPair = null;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(2048);
			keyPair = keyPairGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
		    LOG.error("generateKeyPair failed", e);
		}
		return keyPair;
	}

	/**
	 * Generate ECDSA key pairs
	 */
	public static KeyPair generateECDSAKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecGenParameterSpec, random);
        return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Generate SM2 key pairs
	 */
	public static KeyPair generateSM2KeyPair() throws NoSuchProviderException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		ECGenParameterSpec genParameterSpec = new ECGenParameterSpec("sm2p256v1");
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", provider);
		keyPairGenerator.initialize(genParameterSpec, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	public static AsymmetricCipherKeyPair generateSM2KeyPairParameter() {
		return generateKeyPairParameter(DOMAIN_PARAMS, random);
	}

	public static KeyPair generateECKeyPair(ECDomainParameters domainParameters, SecureRandom random)
			throws NoSuchProviderException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
		org.bouncycastle.jce.spec.ECParameterSpec parameterSpec = new org.bouncycastle.jce.spec.ECParameterSpec(
				domainParameters.getCurve(), domainParameters.getG(),
				domainParameters.getN(), domainParameters.getH());
		kpg.initialize(parameterSpec, random);
		return kpg.generateKeyPair();
	}

	/**
	 * Generate ECC key pairs
	 */
	public static AsymmetricCipherKeyPair generateKeyPairParameter(
			ECDomainParameters domainParameters, SecureRandom random) {
		ECKeyGenerationParameters keyGenerationParams = new ECKeyGenerationParameters(domainParameters, random);
		ECKeyPairGenerator keyGen = new ECKeyPairGenerator();
		keyGen.init(keyGenerationParams);
		return keyGen.generateKeyPair();
	}

	/**
	 * Converts the PKCS8 standard private key byte stream to a private key pair
	 */
	public static BCECPrivateKey convertPKCS8ToECPrivateKey(byte[] pkcs8Key)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		PKCS8EncodedKeySpec peks = new PKCS8EncodedKeySpec(pkcs8Key);
		KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
		return (BCECPrivateKey) kf.generatePrivate(peks);
	}

	/**
	 * Converts the PKCS8 standard private key byte stream to PEM
	 */
	public static String convertECPrivateKeyPKCS8ToPEM(byte[] encodedKey) throws IOException {
		return convertEncodedDataToPEM(PEM_STRING_ECPRIVATEKEY, encodedKey);
	}

	private static String convertEncodedDataToPEM(String type, byte[] encodedData) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		PemWriter pWrt = new PemWriter(new OutputStreamWriter(bOut));
		try {
			PemObject pemObj = new PemObject(type, encodedData);
			pWrt.writeObject(pemObj);
		} finally {
			pWrt.close();
		}
		return new String(bOut.toByteArray());
	}

	public static PublicKey getRSAPublicKey(String key) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(key);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}

	public static PrivateKey getRSAPrivateKey(String key) throws Exception {
		key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		key = key.replace("-----END RSA PRIVATE KEY-----", "");
		byte[] keyBytes = Base64.decodeBase64(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	public static PublicKey getRSAPublicKey(PrivateKey privateKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKeySpec spec = keyFactory.getKeySpec(privateKey,RSAPrivateKeySpec.class);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(spec.getModulus(),
				BigInteger.valueOf(65537));
		return keyFactory.generatePublic(publicKeySpec);
	}

	public static KeyPair getECKeyPair(String privateStr) throws Exception {
		PEMKeyPair pemObject = (PEMKeyPair) CertUtils.convertPemStrToObject(privateStr);
		PrivateKey privateKey = KeyFactory.getInstance("EC").generatePrivate(
				new PKCS8EncodedKeySpec(pemObject.getPrivateKeyInfo().getEncoded()));
		PublicKey publicKey = getPublicKey((ECPrivateKey) privateKey);
		return new KeyPair(publicKey,privateKey);
	}

	public static KeyPair getRSAKeyPair(String privateStr) throws Exception {
		PEMKeyPair pemObject = (PEMKeyPair) CertUtils.convertPemStrToObject(privateStr);
		if (pemObject == null) {
			throw new RuntimeException("missing pemPrivateKey string coding");
		}
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
				new PKCS8EncodedKeySpec(pemObject.getPrivateKeyInfo().getEncoded()));
		PublicKey publicKey = getRSAPublicKey(privateKey);
		return new KeyPair(publicKey,privateKey);
	}


	public static PublicKey getPublicKey(ECPrivateKey privateKey)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
		ECParameterSpec params = privateKey.getParams();
		org.bouncycastle.jce.spec.ECParameterSpec bcSpec =
				org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params, false);
		org.bouncycastle.math.ec.ECPoint q = bcSpec.getG().multiply(privateKey.getS());
		org.bouncycastle.math.ec.ECPoint bcW = bcSpec.getCurve().decodePoint(q.getEncoded(false));
		ECPoint w =
				new ECPoint(
						bcW.getAffineXCoord().toBigInteger(), bcW.getAffineYCoord().toBigInteger());
		ECPublicKeySpec keySpec = new ECPublicKeySpec(w, tryFindNamedCurveSpec(params));
		return KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
				.generatePublic(keySpec);
	}

	@SuppressWarnings("unchecked")
	private static ECParameterSpec tryFindNamedCurveSpec(ECParameterSpec params) {
		org.bouncycastle.jce.spec.ECParameterSpec bcSpec =
				org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params, false);
		for (Object name : Collections.list(org.bouncycastle.jce.ECNamedCurveTable.getNames())) {
			org.bouncycastle.jce.spec.ECNamedCurveParameterSpec bcNamedSpec =
					org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec((String) name);
			if (bcNamedSpec.getN().equals(bcSpec.getN())
					&& bcNamedSpec.getH().equals(bcSpec.getH())
					&& bcNamedSpec.getCurve().equals(bcSpec.getCurve())
					&& bcNamedSpec.getG().equals(bcSpec.getG())) {
				return new org.bouncycastle.jce.spec.ECNamedCurveSpec(
						bcNamedSpec.getName(),
						bcNamedSpec.getCurve(),
						bcNamedSpec.getG(),
						bcNamedSpec.getN(),
						bcNamedSpec.getH(),
						bcNamedSpec.getSeed());
			}
		}
		return params;
	}
}
















