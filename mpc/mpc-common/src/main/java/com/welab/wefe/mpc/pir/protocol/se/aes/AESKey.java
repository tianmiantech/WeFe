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

import javax.crypto.Cipher;
import java.security.SecureRandom;

/**
 * @Author eval
 * @Date 2020-11-19
 **/
public abstract class AESKey {
    public byte[] key;
    /**
     * iv 16字节
     */
    public byte[] iv;

    public Cipher cipher;

    public AESKey(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = new byte[16];
        if (iv == null || iv.length < 16) {
            new SecureRandom().nextBytes(this.iv);
        } else {
            System.arraycopy(iv, 0, this.iv, 0, 16);
        }
        initCipher();
    }

    public abstract void initCipher();
}
