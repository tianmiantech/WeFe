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

package com.welab.wefe.mpc.pir.protocol.se.aes;

import com.welab.wefe.mpc.pir.protocol.se.SymmetricKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * @Author eval
 * @Date 2020-11-23
 **/
public class AESEncryptKey extends AESKey implements SymmetricKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESEncryptKey.class);

    public AESEncryptKey(byte[] key) {
        super(key, null);
    }

    public AESEncryptKey(byte[] key, byte[] iv) {
        super(key, iv);
    }

    @Override
    public void initCipher() {
        try {
            Key key1 = new SecretKeySpec(key, "AES");
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key1, new IvParameterSpec(iv));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public byte[] encrypt(byte[] plainText) {
        try {
            return cipher.doFinal(plainText);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    @Override
    public byte[] getIv() {
        return iv;
    }
}
