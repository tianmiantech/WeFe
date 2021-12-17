/*
 * *
 *  * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.welab.wefe.mpc.pir.protocol.se.aes;

import com.welab.wefe.mpc.pir.protocol.se.SymmetricKey;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * @Author eval
 * @Date 2020-11-19
 **/
public class AESDecryptKey extends AESKey implements SymmetricKey {

    public AESDecryptKey(byte[] key) {
        super(key, null);
    }

    public AESDecryptKey(byte[] key, byte[] iv) {
        super(key, iv);
    }

    @Override
    public void initCipher() {
        try {
            Key key1 = new SecretKeySpec(key, "AES");
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key1, new IvParameterSpec(iv));
        } catch (Exception e) {

        }
    }

    @Override
    public byte[] encrypt(byte[] ciphertext) {
        try {
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] getIv() {
        return this.iv;
    }
}
