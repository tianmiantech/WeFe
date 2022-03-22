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

package com.welab.wefe.mpc.pir.protocol.ro.mac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Author eval
 * @Date 2020-11-19
 **/
public class Sha256MAC extends HashBasedMessageAuthenticationCode {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sha256MAC.class);

    Mac mac;
    byte[] key;

    public Sha256MAC(byte[] key) {
        this.key = key;
        String mode = "HmacSHA256";
        SecretKey secretKey = new SecretKeySpec(key, mode);
        try {
            mac = Mac.getInstance(mode);
            mac.init(secretKey);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public byte[] digest(byte[] message) {
        return mac.doFinal(message);
    }
}
