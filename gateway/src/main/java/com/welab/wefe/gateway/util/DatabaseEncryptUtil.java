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

package com.welab.wefe.gateway.util;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.SM4Util;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.config.CommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseEncryptUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseEncryptUtil.class);

    /**
     * Encrypt
     */
    public static String encrypt(String plaintext) throws StatusCodeWithException {
        if (StringUtil.isEmpty(plaintext)) {
            return plaintext;
        }
        try {
            CommonConfig config = GatewayServer.CONTEXT.getBean(CommonConfig.class);
            if (!config.isDatabaseEncryptEnable() || isCiphertext(plaintext)) {
                return plaintext;
            }
            return baseEncrypt(plaintext);
        } catch (Exception e) {
            LOG.error("加密字符串 " + plaintext + " 失败, 原因：", e);
            throw new StatusCodeWithException("加密字符串:" + plaintext + " 失败, 原因：" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * Decrypt
     */
    public static String decrypt(String ciphertext) throws StatusCodeWithException {
        if (StringUtil.isEmpty(ciphertext)) {
            return ciphertext;
        }
        try {
            CommonConfig config = GatewayServer.CONTEXT.getBean(CommonConfig.class);
            if (!config.isDatabaseEncryptEnable() || !isCiphertext(ciphertext)) {
                return ciphertext;
            }
            return baseDecrypt(ciphertext);
        } catch (Exception e) {
            LOG.error("解密字符串 " + ciphertext + " 失败, 原因：", e);
            throw new StatusCodeWithException("解密字符串:" + ciphertext + " 失败, 原因：" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * Whether the string is a ciphertext
     */
    private static boolean isCiphertext(String text) {
        try {
            baseDecrypt(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String baseEncrypt(String plaintext) throws Exception {
        CommonConfig config = GatewayServer.CONTEXT.getBean(CommonConfig.class);
        return SM4Util.encrypt(config.getDatabaseEncryptSecretKey(), plaintext);
    }

    private static String baseDecrypt(String ciphertext) throws Exception {
        CommonConfig config = GatewayServer.CONTEXT.getBean(CommonConfig.class);
        return SM4Util.decrypt(config.getDatabaseEncryptSecretKey(), ciphertext);
    }
}
