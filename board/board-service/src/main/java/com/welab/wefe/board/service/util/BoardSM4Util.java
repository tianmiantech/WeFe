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

package com.welab.wefe.board.service.util;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.SM4Util;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardSM4Util {
    private static final Logger LOG = LoggerFactory.getLogger(BoardSM4Util.class);

    public static String encryptPhoneNumber(String phoneNumber) throws StatusCodeWithException {
        if (StringUtil.isEmpty(phoneNumber)) {
            return phoneNumber;
        }
        try {
            // Compatible with old version data
            if (StringUtil.checkPhoneNumber(phoneNumber)) {
                return encrypt(phoneNumber);
            }
            return phoneNumber;
        } catch (Exception e) {
            LOG.error("加密手机号:" + phoneNumber + " 失败, 原因：", e);
            throw new StatusCodeWithException("加密手机号:" + phoneNumber + " 失败, 原因：" + e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    public static String decryptPhoneNumber(String phoneNumber) throws StatusCodeWithException {
        if (StringUtil.isEmpty(phoneNumber)) {
            return phoneNumber;
        }
        try {
            // Compatible with old version data
            if (!StringUtil.checkPhoneNumber(phoneNumber)) {
                return decrypt(phoneNumber);
            }
            return phoneNumber;
        } catch (Exception e) {
            LOG.error("解密手机号:" + phoneNumber + " 失败, 原因：", e);
            throw new StatusCodeWithException("解密手机号:" + phoneNumber + " 失败, 原因：" + e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    public static String encryptCommonText(String text) throws StatusCodeWithException {
        if(StringUtil.isEmpty(text)) {
            return text;
        }
        try {
            return encrypt(text);
        } catch (Exception e) {
            LOG.error("加密字符串:" + text + " 失败, 原因：", e);
            throw new StatusCodeWithException("加密字符串:" + text + " 失败, 原因：" + e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        }

    }

    public static String decryptCommonText(String text) throws StatusCodeWithException {
        if(StringUtil.isEmpty(text)) {
            return text;
        }
        try {
            return decrypt(text);
        } catch (Exception e) {
            LOG.error("解密字符串:" + text + " 失败, 原因：", e);
            throw new StatusCodeWithException("解密字符串:" + text + " 失败, 原因：" + e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    public static boolean isEncryptText(String text) {
        if (StringUtil.isEmpty(text)) {
            return false;
        }
        try {
            decrypt(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String encrypt(String plaintext) throws Exception {
        Config config = Launcher.CONTEXT.getBean(Config.class);
        return SM4Util.encrypt(config.getSm4SecretKey(), plaintext);
    }

    private static String decrypt(String plaintext) throws Exception {
        Config config = Launcher.CONTEXT.getBean(Config.class);
        return SM4Util.decrypt(config.getSm4SecretKey(), plaintext);
    }
}
