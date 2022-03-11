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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;


/**
 * @author zane.luo
 */
public class RSAUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtil.class);

    public static final String KEY_ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHM = "SHA1withRSA";
    private static final int ENCRYPT_BLOCK = 245;
    private static final int DECRYPT_BLOCK = 256;

    public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] encryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public static String encryptByPrivateKey(String data, String privateKeyStr) throws Exception {
        RSAPrivateKey privateKey = getPrivateKey(privateKeyStr);
        return encryptByPrivateKey(data, privateKey);
    }

    public static String decryptByPublicKey(String data, String publicKeyStr) throws Exception {
        RSAPublicKey publicKey = getPublicKey(publicKeyStr);
        return decryptByPublicKey(data, publicKey);
    }

    public static String encryptByPublicKey(String data, String publicKeyStr) throws Exception {
        RSAPublicKey publicKey = getPublicKey(publicKeyStr);
        byte[] enData = encryptByPublicKey(data.getBytes("UTF-8"), publicKey);
        return Base64.getEncoder().encodeToString(enData);
    }

    /**
     * The private key encryption
     *
     * @param privateKey The private key
     * @param data       Data to be encrypted
     * @return Encrypted string
     */
    public static String encryptByPrivateKey(String data, RSAPrivateKey privateKey) throws Exception {
        byte[] datas = data.getBytes();
        byte[] encrypt = null;
        int dataLen = datas.length;
        int nBlock = (dataLen / ENCRYPT_BLOCK);
        if ((dataLen % ENCRYPT_BLOCK) != 0) {
            nBlock += 1;
        }

        ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * DECRYPT_BLOCK);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        for (int offset = 0; offset < dataLen; offset += ENCRYPT_BLOCK) {

            int inputLen = (dataLen - offset);
            if (inputLen > ENCRYPT_BLOCK) {
                inputLen = ENCRYPT_BLOCK;
            }

            byte[] encryptedBlock = cipher.doFinal(datas, offset, inputLen);
            outbuf.write(encryptedBlock);
            outbuf.flush();
            encrypt = outbuf.toByteArray();
            outbuf.close();
        }
        byte[] enData = Base64.getDecoder().decode(encrypt);
        return new String(enData);
    }

    /**
     * A public key to decrypt
     *
     * @param publicKey The public key
     * @param data      Data that needs to be decrypted
     * @return Decrypted string
     * @throws Exception
     */
    public static String decryptByPublicKey(String data, RSAPublicKey publicKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(data.getBytes("UTF-8"));
        byte[] decrypt = null;
        int dataLen = bytes.length;

        int nBlock = (dataLen / ENCRYPT_BLOCK);

        if ((dataLen % ENCRYPT_BLOCK) != 0) {
            nBlock += 1;
        }
        //Output buffer, nBlock per encryptBlock size
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * ENCRYPT_BLOCK);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        // Segmented decryption
        for (int offset = 0; offset < bytes.length; offset += DECRYPT_BLOCK) {
            // Block size: DECRYPT_BLOCK or number of remaining bytes
            int inputLen = (bytes.length - offset);
            if (inputLen > DECRYPT_BLOCK) {
                inputLen = DECRYPT_BLOCK;
            }
            // The segmented decryption results are obtained
            byte[] decryptedBlock = cipher.doFinal(bytes, offset, inputLen);
            // Appends the result to the output buffer
            outbuf.write(decryptedBlock);
        }
        outbuf.flush();
        decrypt = outbuf.toByteArray();
        outbuf.close();

        String decryptData = new String(decrypt, "UTF-8");
        return decryptData;
    }


    public static byte[] decryptByPublicKey(byte[] data, RSAPublicKey publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] decryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }


    /**
     * The private key signature
     */
    public static String sign(String data, String privateKeyStr) throws Exception {
        Signature sigEng = Signature.getInstance(SIGN_ALGORITHM);
        byte[] priByte = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
        KeyFactory fac = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
        sigEng.initSign(privateKey);
        sigEng.update(data.getBytes());
        return Base64.getEncoder().encodeToString(sigEng.sign());

    }


    /**
     * The private key signature
     */
    public static String sign(String data, String privateKeyStr, String charset) throws Exception {
        Signature sigEng = Signature.getInstance(SIGN_ALGORITHM);
        byte[] priByte = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
        KeyFactory fac = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
        sigEng.initSign(privateKey);
        sigEng.update(data.getBytes(charset));
        return Base64.getEncoder().encodeToString(sigEng.sign());

    }


    public static String sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public static boolean verify(byte[] data, PublicKey publicKey, String sign) throws Exception {
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
    }


    /**
     * Private key format conversion: Converts the string type to RSAPrivateKey
     */
    public static RSAPrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr.getBytes());
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }


    /**
     * Public key format conversion: Convert from string type to RSAPublicKey type
     */
    public static RSAPublicKey getPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
    }

    public static RSAPrivateKey getPrivateKeyFromPem(String keyPath) throws Exception {
        byte[] key = readKeyFile(keyPath);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static RSAPublicKey getPublicKeyFromPem(String keyPath) throws Exception {
        byte[] key = readKeyFile(keyPath);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }


    public static byte[] readKeyFile(String keyPath) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new FileReader(keyPath));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) != '-') {
                    sb.append(readLine).append('\r');
                }
            }
            br.close();
            return Base64.getDecoder().decode(sb.toString().getBytes());
        } catch (FileNotFoundException e) {
            throw new Exception("key file not exist, path=" + keyPath);
        } catch (IOException e) {
            throw new Exception("read key file occur error, path=" + keyPath);
        }
    }


    public static byte[] encryptLongMsgByPublicKey(String data, RSAPublicKey publicKey, Integer stepLen) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int keyLen = stepLen != null ? stepLen : publicKey.getModulus().bitLength() / 8;
        // Length of encrypted data <= mode length -11
        String[] datas = splitString(data, keyLen - 11);
        byte[] msg = new byte[0];
        //Group encryption is used if the plaintext length is greater than the module length -11
        for (String s : datas) {
            byte[] info = cipher.doFinal(s.getBytes());
            msg = concat(msg, info);
        }
        return msg;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }


    public static String decryptLongMsgByPrivateKey(byte[] bytes, RSAPrivateKey privateKey, Integer stepLen) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //length
        int keyLen = stepLen != null ? stepLen : privateKey.getModulus().bitLength() / 8;

        //If the ciphertext length is greater than the module length, group decryption is performed
        StringBuilder msg = new StringBuilder();
        byte[][] arrays = splitArray(bytes, keyLen);
        for (byte[] arr : arrays) {
            msg.append(new String(cipher.doFinal(arr)));
        }
        return msg.toString();
    }

    public static String[] splitString(String string, int len) {
        int x = string.length() / len;
        int y = string.length() % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        String[] strings = new String[x + z];
        String str;
        for (int i = 0; i < x + z; i++) {
            if (i == x + z - 1 && y != 0) {
                str = string.substring(i * len, i * len + y);
            } else {
                str = string.substring(i * len, i * len + len);
            }
            strings[i] = str;
        }
        return strings;
    }

    public static byte[][] splitArray(byte[] data, int len) {
        int x = data.length / len;
        int y = data.length % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        byte[][] arrays = new byte[x + z][];
        byte[] arr;
        for (int i = 0; i < x + z; i++) {
            arr = new byte[len];
            if (i == x + z - 1 && y != 0) {
                System.arraycopy(data, i * len, arr, 0, y);
            } else {
                System.arraycopy(data, i * len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }

    public static byte[] rsaByPrivateKey(byte[] srcData, String transformation, String keyFile, String keyType, String keyPass, int mode, int blockSize) {
        try {
            KeyStore ks = KeyStore.getInstance(keyType);
            char[] charPriKeyPass = keyPass.toCharArray();
            ks.load(new FileInputStream(keyFile), charPriKeyPass);
            Enumeration<String> aliasEnum = ks.aliases();
            String keyAlias = null;
            if (aliasEnum.hasMoreElements()) {
                keyAlias = aliasEnum.nextElement();
            }
            PrivateKey key = (PrivateKey) ks.getKey(keyAlias, charPriKeyPass);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(mode, key);
            int len = srcData.length;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int offset = 0; offset < len; offset += blockSize) {
                int remain = len - offset;
                if (remain < blockSize) {
                    blockSize = remain;
                }
                byte[] doFinal = cipher.doFinal(srcData, offset, blockSize);
                baos.write(doFinal);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static String byte2Hex(byte[] srcBytes) {
        StringBuilder hexRetSb = new StringBuilder();
        for (byte b : srcBytes) {
            String hexString = Integer.toHexString(0x00ff & b);
            hexRetSb.append(hexString.length() == 1 ? 0 : "").append(hexString);
        }
        return hexRetSb.toString();
    }

    public static RsaKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        return new RsaKeyPair(
                Base64.getEncoder().encodeToString(publicKey.getEncoded()),
                Base64.getEncoder().encodeToString(privateKey.getEncoded())
        );

    }

    /**
     * RSA secret key to
     */
    public static class RsaKeyPair {
        public String publicKey;
        public String privateKey;

        public RsaKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }

}
