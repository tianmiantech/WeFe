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
package com.webank.cert.toolkit.encrypt;

import com.webank.cert.toolkit.enums.EccTypeEnums;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.ECDSAKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.SM2KeyPair;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;


/**
 * PemFormat
 *
 * @author graysonzhang
 * @author yuzhichu
 * @Description: PemFormat
 * @date 2019-12-23 15:01:13
 */
public class PemEncrypt {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }


	public static CryptoKeyPair getCryptKeyPair(byte[] privateKey, EccTypeEnums eccTypeEnums){
		if(CryptoKeyPair.ECDSA_CURVE_NAME.equals(eccTypeEnums.getEccName())){
			return new ECDSAKeyPair().createKeyPair(KeyPresenter.asBigInteger(privateKey));
		}
		else if(CryptoKeyPair.SM2_CURVE_NAME.equals(eccTypeEnums.getEccName())){
			return new SM2KeyPair().createKeyPair(KeyPresenter.asBigInteger(privateKey));
		}
		else{
			throw new IllegalArgumentException("unrecognised ecc type" + eccTypeEnums.getEccName());
		}
	}

    public static String encryptPrivateKey(byte[] privateKey, EccTypeEnums eccTypeEnums)
            throws Exception {
        CryptoKeyPair cryptoKeyPair = getCryptKeyPair(privateKey, eccTypeEnums);
        BigInteger key = new BigInteger(1, Numeric.hexStringToByteArray(cryptoKeyPair.getHexPrivateKey()));

        //1. Encapsulate curve meta info and private key bytes in PKCS#8 format
        ASN1ObjectIdentifier curveOid = ECUtil.getNamedCurveOid(eccTypeEnums.getEccName());
        X962Parameters params = new X962Parameters(curveOid);
        ECPrivateKey keyStructure = new ECPrivateKey(256, key,
                new DERBitString(Numeric.hexStringToByteArray(cryptoKeyPair.getHexPublicKey())),
                null);
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(
                new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params),
                keyStructure);

        //2. Serialize the private key data to output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PemWriter w = new PemWriter(new OutputStreamWriter(bos));
        try {
            ASN1Object o = (ASN1Object) privateKeyInfo.toASN1Primitive();
            w.writeObject(new PemObject("PRIVATE KEY", o.getEncoded("DER")));
            w.flush();
            //3. Gather result and return.
            return new String(bos.toByteArray());
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static byte[] decryptPrivateKey(String encryptPrivateKey) throws Exception {
        return decryptPrivateKey(new StringReader(encryptPrivateKey));
    }

    private static byte[] decryptPrivateKey(Reader reader) throws Exception {
        PemReader pemReader = new PemReader(reader);
        PemObject pemObject = pemReader.readPemObject();
        try {
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
            KeyFactory keyFacotry = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);

            PrivateKey privateKey = keyFacotry.generatePrivate(encodedKeySpec);
            BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) privateKey;
            return Numeric.toBytesPadded(bcecPrivateKey.getD(), 32);
        } finally {
            try {
                if (pemReader != null) {
                    pemReader.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static PrivateKey getPrivateKey(String encryptPrivateKey) throws Exception {
        return getPrivateKey(new StringReader(encryptPrivateKey));
    }


    public static PrivateKey getPrivateKey(Reader reader) throws Exception {
        PemReader pemReader = new PemReader(reader);
        PemObject pemObject = pemReader.readPemObject();
        try {
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
            KeyFactory keyFacotry = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);

            PrivateKey privateKey = keyFacotry.generatePrivate(encodedKeySpec);
            return privateKey;
        } finally {
            try {
                if (pemReader != null) {
                    pemReader.close();
                }
            } catch (Exception e) {
            }
        }
    }

}













