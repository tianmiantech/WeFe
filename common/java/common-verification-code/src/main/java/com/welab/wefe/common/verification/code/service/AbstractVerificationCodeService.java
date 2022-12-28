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

package com.welab.wefe.common.verification.code.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;
import com.welab.wefe.common.verification.code.ClientFactory;
import com.welab.wefe.common.verification.code.common.CaptchaSendChannel;
import com.welab.wefe.common.verification.code.common.VerificationCodeBusinessType;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract verification code service
 */
public abstract class AbstractVerificationCodeService {
    protected final Logger LOG = LoggerFactory.getLogger(AbstractVerificationCodeService.class);
    /**
     * Verification code valid duration, unit:minutes
     */
    private final static int VALID_DURATION_MINUTES = 2;
    /**
     * verification code cache
     */
    private static ExpiringMap<String, String> VERIFICATION_CODE_CACHE = ExpiringMap.builder()
            .expiration(VALID_DURATION_MINUTES, TimeUnit.MINUTES)
            .build();

    public abstract void check(String mobile) throws StatusCodeWithException;

    public abstract Map<String, Object> buildExtendParams(String mobile, String verificationCode, VerificationCodeBusinessType businessType) throws StatusCodeWithException;

    public abstract void saveSendRecord(String mobile, String verificationCode, VerificationCodeBusinessType businessType, AbstractResponse response);


    /**
     * Send verification code
     */
    public void send(String mobile, VerificationCodeBusinessType businessType) throws StatusCodeWithException {
        // check mobile
        check(mobile);
        String cacheKey = buildCacheKey(mobile, businessType);
        if (VERIFICATION_CODE_CACHE.containsKey(cacheKey)) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, VALID_DURATION_MINUTES + "分钟内禁止多次获取验证码");
        }
        String verificationCode = generateVerificationCode();
        try {
            // Get Client
            AbstractClient client = ClientFactory.getClient(getVerificationCodeSendChannel(), buildExtendParams(mobile, verificationCode, businessType));
            // send
            AbstractResponse response = client.send(mobile, verificationCode);
            // Persistence send record
            saveSendRecord(mobile, verificationCode, businessType, response);
            if (!response.success()) {
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "发送验证码异常:" + response.getMessage());
            }
            VERIFICATION_CODE_CACHE.put(cacheKey, verificationCode);
        } catch (StatusCodeWithException e) {
            LOG.error("Send verification code exception: ", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Send verification code exception: ", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "发送验证码失败");
        }
    }

    public CaptchaSendChannel getVerificationCodeSendChannel() {
        return CaptchaSendChannel.email;
    }


    /**
     * Check verification code is valid
     */
    public void checkVerificationCode(String mobile, String verificationCode, VerificationCodeBusinessType businessType) throws StatusCodeWithException {
        if (StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "手机号不能为空。");
        }
        if (StringUtil.isEmpty(verificationCode)) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "验证码不能为空。");
        }
        String cacheKey = buildCacheKey(mobile, businessType);
        String cacheVerificationCode = VERIFICATION_CODE_CACHE.get(cacheKey);
        if (StringUtil.isEmpty(cacheVerificationCode)) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "验证码无效,请重新获取验证码。");
        }
        if (!cacheVerificationCode.equals(verificationCode)) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "验证码不正确。");
        }
    }

    /**
     * Generate 6-digit verification code
     */
    public String generateVerificationCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    /**
     * Build cache key
     */
    private String buildCacheKey(String mobile, VerificationCodeBusinessType businessType) {
        return mobile + "_" + businessType.name();
    }
}
