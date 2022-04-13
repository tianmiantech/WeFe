/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.serving.service.service.verificationcode;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;
import com.welab.wefe.common.wefe.enums.VerificationCodeBusinessType;
import com.welab.wefe.common.wefe.enums.VerificationCodeSendChannel;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.VerificationCodeMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.AccountRepository;
import com.welab.wefe.serving.service.database.serving.repository.VerificationCodeRepository;
import com.welab.wefe.serving.service.utils.ServingSM4Util;

import net.jodah.expiringmap.ExpiringMap;

/**
 * Verification code service class
 *
 * @author aaron.li
 * @date 2022/1/19 11:29
 **/
@Service
public class VerificationCodeService {
    protected final Logger LOG = LoggerFactory.getLogger(VerificationCodeService.class);
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

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private Config config;

    /**
     * Send verification code
     *
     * @param mobile       target mobile
     * @param businessType business type
     * @throws StatusCodeWithException
     */
    public void send(String mobile, VerificationCodeBusinessType businessType) throws StatusCodeWithException {
        if (StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException("手机号不能为空", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }

        if (!StringUtil.checkPhoneNumber(mobile)) {
            throw new StatusCodeWithException("非法的手机号", StatusCode.PARAMETER_VALUE_INVALID);
        }

        String cacheKey = buildCacheKey(mobile, businessType);
        if (VERIFICATION_CODE_CACHE.containsKey(cacheKey)) {
            throw new StatusCodeWithException(VALID_DURATION_MINUTES + "分钟内禁止多次获取验证码", StatusCode.ILLEGAL_REQUEST);
        }

        AccountMySqlModel model = accountRepository.findOne("phoneNumber", ServingSM4Util.encryptPhoneNumber(mobile), AccountMySqlModel.class);
        // phone number error
        if (model == null) {
            throw new StatusCodeWithException("手机号错误，该用户不存在", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员", StatusCode.PERMISSION_DENIED);
        }
        try {
            // Generate verification code
            String verificationCode = generateVerificationCode();
            VerificationCodeSendChannel sendChannel = VerificationCodeSendChannel.valueOf(config.getVerificationCodeSendChannel());
            // Get Client
            AbstractClient client = ClientFactory.getClient(sendChannel, businessType);
            // send
            AbstractResponse response = client.send(mobile, verificationCode);
            // save model
            save(buildModel(mobile, verificationCode, sendChannel, businessType, response));
            if (!response.success()) {
                throw new StatusCodeWithException("发送验证码异常:" + response.getMessage(), StatusCode.SYSTEM_ERROR);
            }
            VERIFICATION_CODE_CACHE.put(cacheKey, verificationCode);
        } catch (StatusCodeWithException e) {
            LOG.error("Send verification code exception: ", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Send verification code exception: ", e);
            throw new StatusCodeWithException("发送验证码异常:" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * Check verification code is valid
     */
    public void checkVerificationCode(String mobile, String verificationCode, VerificationCodeBusinessType businessType) throws StatusCodeWithException {
        if(StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException("手机号不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if(StringUtil.isEmpty(verificationCode)) {
            throw new StatusCodeWithException("验证码不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        String cacheKey = buildCacheKey(mobile, businessType);
        String cacheVerificationCode = VERIFICATION_CODE_CACHE.get(cacheKey);
        if(StringUtil.isEmpty(cacheVerificationCode)) {
            throw new StatusCodeWithException("验证码无效,请重新获取验证码。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if(!cacheVerificationCode.equals(verificationCode)) {
            throw new StatusCodeWithException("验证码不正确。", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    /**
     * Get send channel
     */
    public String getSendChannel() {
        return config.getVerificationCodeSendChannel();
    }

    /**
     * save model
     */
    private void save(VerificationCodeMysqlModel model) {
        try {
            verificationCodeRepository.save(model);
        } catch (Exception e) {
            LOG.error("Save verificationCode exception: ", e);
        }
    }

    /**
     * build entity model
     */
    private VerificationCodeMysqlModel buildModel(String mobile, String verificationCode, VerificationCodeSendChannel sendChannel,
                                                  VerificationCodeBusinessType businessType, AbstractResponse response) {
        VerificationCodeMysqlModel model = new VerificationCodeMysqlModel();
        model.setBizId(UUID.randomUUID().toString().replace("-", ""));
        model.setMobile(mobile);
        model.setCode(verificationCode);
        model.setSuccess(String.valueOf(response.success()));
        model.setSendChannel(sendChannel);
        model.setBusinessType(businessType);
        model.setRespContent(response.getRespBody());
        return model;
    }


    /**
     * Build cache key
     */
    private String buildCacheKey(String mobile, VerificationCodeBusinessType businessType) {
        return mobile + "_" + businessType.name();
    }


    /**
     * Generate 6-digit verification code
     */
    private String generateVerificationCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

}
