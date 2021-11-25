/**
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

package com.welab.wefe.union.service.service.sms;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.constant.SmsBusinessType;
import com.welab.wefe.common.data.mongodb.constant.SmsSupplierEnum;
import com.welab.wefe.common.data.mongodb.entity.sms.SmsDetailInfo;
import com.welab.wefe.common.data.mongodb.entity.sms.SmsVerificationCode;
import com.welab.wefe.common.data.mongodb.repo.SmsDetailInfoReop;
import com.welab.wefe.common.data.mongodb.repo.SmsVerificationCodeReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aaron.li
 * @Date 2021/10/19
 **/
@Service
public class SmsService {
    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    /**
     * Verification code valid duration, unit: minute
     */
    private final static long CODE_VALID_DURATION_MINUTE = 2;

    private final static long CODE_VALID_DURATION_MILLISECONDS = CODE_VALID_DURATION_MINUTE * 60 * 1000L;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private SmsVerificationCodeReop smsVerificationCodeReop;
    @Autowired
    private SmsDetailInfoReop smsDetailInfoReop;

    /**
     * Send verification code
     *
     * @param mobile target mobile number
     * @throws Exception
     */
    public void sendVerificationCode(String mobile, SmsBusinessType smsBusinessType) throws StatusCodeWithException {
        if (StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException("手机号不能为空", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }

        if (!StringUtil.checkPhoneNumber(mobile)) {
            throw new StatusCodeWithException("非法的手机号", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (!checkCodeIsExpire(mobile, smsBusinessType)) {
            throw new StatusCodeWithException(CODE_VALID_DURATION_MINUTE + "分钟内禁止多次获取验证码", StatusCode.ILLEGAL_REQUEST);
        }

        // send sms to target mobile
        try {
            String code = generateCode();
            AbstractSendSmsClient sendSmsClient = AliyunSendSmsClient.createClient(configProperties.getAliyunAccessKeyId(), configProperties.getAliyunAccessKeySecret());
            Map<String, Object> smsRequest = new HashMap<>(16);
            smsRequest.put("SignName", configProperties.getSmsAliyunSignName());
            if (smsBusinessType.equals(SmsBusinessType.AccountForgetPasswordVerificationCode)) {
                smsRequest.put("templateCode", configProperties.getSmsAliyunAccountForgetPasswordVerificationCodeTemplateCode());
            } else if (smsBusinessType.equals(SmsBusinessType.MemberRegisterVerificationCode)) {
                smsRequest.put("templateCode", configProperties.getSmsAliyunMemberRegisterVerificationCodeTemplateCode());
            } else {
                throw new StatusCodeWithException("无效的短信业务类型", StatusCode.ILLEGAL_REQUEST);
            }
            smsRequest.put("code", code);

            AbstractSmsResponse smsResponse = sendSmsClient.sendVerificationCode(mobile, smsRequest);
            SmsDetailInfo smsDetailInfo = new SmsDetailInfo();
            smsDetailInfo.setMobile(mobile);
            smsDetailInfo.setReqId(smsResponse.getReqId());
            smsDetailInfo.setReqContent(JObject.create(smsRequest).toString());
            smsDetailInfo.setSupplier(SmsSupplierEnum.Aliyun);
            smsDetailInfo.setSuccess(smsResponse.success());
            smsDetailInfo.setRespContent(smsResponse.getRespBody());
            smsDetailInfo.setBusinessType(smsBusinessType);
            smsDetailInfoReop.save(smsDetailInfo);
            if (!smsResponse.success()) {
                throw new StatusCodeWithException("获取验证码异常:" + smsResponse.getMessage(), StatusCode.SYSTEM_ERROR);
            }

            SmsVerificationCode smsVerificationCode = new SmsVerificationCode();
            smsVerificationCode.setMobile(mobile);
            smsVerificationCode.setCode(code);
            smsVerificationCode.setBusinessType(smsBusinessType);
            smsVerificationCodeReop.saveOrUpdate(smsVerificationCode);
        } catch (StatusCodeWithException e) {
            LOG.error("获取短信验证码异常:", e);
            throw e;
        } catch (Exception e) {
            LOG.error("获取短信验证码异常:", e);
            throw new StatusCodeWithException("获取验证码异常:" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * Check verification code is valid
     *
     * @param mobile target mobile number
     * @param code   Verification Code
     * @return true: valid, false: invalid
     */
    public void checkVerificationCodeValid(String mobile, String code, SmsBusinessType smsBusinessType) throws StatusCodeWithException {
        SmsVerificationCode smsVerificationCode = smsVerificationCodeReop.find(mobile, smsBusinessType);
        if (null == smsVerificationCode) {
            throw new StatusCodeWithException("验证码无效,请重新获取验证码", StatusCode.PARAMETER_VALUE_INVALID);
        }
        long updateTime = smsVerificationCode.getUpdateTime();
        if (System.currentTimeMillis() - updateTime > CODE_VALID_DURATION_MILLISECONDS) {
            throw new StatusCodeWithException("验证码无效,请重新获取验证码", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!smsVerificationCode.getCode().equals(code)) {
            throw new StatusCodeWithException("验证码不正确", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }


    /**
     * Check verification code is valid
     *
     * @param mobile target mobile number
     * @return true: expire, false: non-expire
     */
    private boolean checkCodeIsExpire(String mobile, SmsBusinessType smsBusinessTypeEnum) {
        SmsVerificationCode smsVerificationCode = smsVerificationCodeReop.find(mobile, smsBusinessTypeEnum);
        if (null == smsVerificationCode) {
            return true;
        }
        long updateTime = smsVerificationCode.getUpdateTime();
        return System.currentTimeMillis() - updateTime > CODE_VALID_DURATION_MILLISECONDS;
    }


    private String generateCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
