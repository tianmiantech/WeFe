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

package com.welab.wefe.common.util;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * GM sm4
 */
public class SM4Util {
    private static final String ENCODING = "UTF-8";
    private static final String ALGORITHM_NAME = "SM4";
    private static final String ALGORITHM_NAME_CBC_PADDING = "SM4/CBC/PKCS5Padding";
    /**
     * 128-32 bit hex
     */
    private static final int DEFAULT_KEY_SIZE = 128;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Automatically generate key string
     */
    public static String generateKeyString() throws Exception {
        return new String(Hex.encodeHex(generateKey(DEFAULT_KEY_SIZE), false));
    }

    /**
     * sm4 encrypt
     *
     * @param hexKey    Hexadecimal key（ignore case）
     * @param plaintext Plaintext
     * @return Hexadecimal encrypted string
     * @throws Exception
     * @explain Encryption mode：CBC
     */
    public static String encrypt(String hexKey, String plaintext) throws Exception {
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        byte[] srcData = plaintext.getBytes(ENCODING);
        byte[] cipherArray = encryptCbcPadding(keyData, srcData);
        return ByteUtils.toHexString(cipherArray);
    }

    /**
     * sm4 decrypt
     *
     * @param hexKey Hexadecimal key（ignore case）
     * @param ciphertext   ciphertext
     * @return plaintext
     * @throws Exception
     * @explain Encryption mode：CBC
     */
    public static String decrypt(String hexKey, String ciphertext) throws Exception {
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        byte[] resultData = ByteUtils.fromHexString(ciphertext);
        byte[] srcData = decryptCbcPadding(keyData, resultData);
        return new String(srcData, ENCODING);
    }

    /**
     * CBC model
     */
    private static byte[] decryptCbcPadding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    /**
     * generate iv
     */
    private static AlgorithmParameters generateIV() throws Exception {
        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) 0x00);
        AlgorithmParameters params = AlgorithmParameters.getInstance(ALGORITHM_NAME);
        params.init(new IvParameterSpec(iv));
        return params;
    }

    private static Cipher generateCbcCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key, generateIV());
        return cipher;
    }

    /**
     * CBC in encryption mode
     */
    private static byte[] encryptCbcPadding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static byte[] generateKey(int keySize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

}
