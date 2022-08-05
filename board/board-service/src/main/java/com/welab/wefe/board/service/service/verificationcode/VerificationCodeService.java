/*
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

package com.welab.wefe.board.service.service.verificationcode;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.entity.VerificationCodeMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.database.repository.VerificationCodeRepository;
import com.welab.wefe.board.service.dto.globalconfig.AlertConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.AliyunSmsChannelConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.MailServerModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.verification.code.AbstractResponse;
import com.welab.wefe.common.verification.code.common.CaptchaSendChannel;
import com.welab.wefe.common.verification.code.common.VerificationCodeBusinessType;
import com.welab.wefe.common.verification.code.email.EmailClient;
import com.welab.wefe.common.verification.code.service.AbstractVerificationCodeService;
import com.welab.wefe.common.verification.code.sms.AliyunSmsClient;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Verification code service class
 *
 * @author aaron.li
 * @date 2022/1/19 11:29
 **/
@Service
public class VerificationCodeService extends AbstractVerificationCodeService {
    protected final Logger LOG = LoggerFactory.getLogger(VerificationCodeService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private Config config;

    @Override
    public void check(String mobile) throws StatusCodeWithException {
        if (StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException("手机号不能为空", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }

        if (!StringUtil.checkPhoneNumber(mobile)) {
            throw new StatusCodeWithException("非法的手机号", StatusCode.PARAMETER_VALUE_INVALID);
        }
        AccountMysqlModel model = accountRepository.findOne("phoneNumber", DatabaseEncryptUtil.encrypt(mobile), AccountMysqlModel.class);
        // phone number error
        if (model == null) {
            throw new StatusCodeWithException("手机号错误，该用户不存在", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员", StatusCode.PERMISSION_DENIED);
        }

        // check channel config
        CaptchaSendChannel sendChannel = getVerificationCodeSendChannel();
        if (CaptchaSendChannel.email.equals(sendChannel)) {
            checkEmailConfig();
        } else {
            checkSmsConfig();
        }
    }

    @Override
    public Map<String, Object> buildExtendParams(String mobile, String verificationCode, VerificationCodeBusinessType businessType) throws StatusCodeWithException {
        CaptchaSendChannel sendChannel = getVerificationCodeSendChannel();
        AccountMysqlModel accountMysqlModel = accountRepository.findOne("phoneNumber", DatabaseEncryptUtil.encrypt(mobile), AccountMysqlModel.class);
        // email
        if (CaptchaSendChannel.email.equals(sendChannel)) {
            String subject = "忘记密码";
            String content = "您正在执行忘记密码操作。您的验证码是" + verificationCode + "，2分钟内有效，请勿泄漏于他人!";
            MailServerModel mailServerModel = globalConfigService.getModel(MailServerModel.class);
            return EmailClient.buildExtendParams(mailServerModel.getMailHost(), mailServerModel.getMailPort(), mailServerModel.getMailUsername(),
                    mailServerModel.getMailPassword(), mailServerModel.getMailUsername(), accountMysqlModel.getEmail(), subject, content);
        }
        // aliyun sms
        AliyunSmsChannelConfigModel aliyunSmsChannelConfigModel = globalConfigService.getModel(AliyunSmsChannelConfigModel.class);
        return AliyunSmsClient.buildExtendParams(aliyunSmsChannelConfigModel.accessKeyId, aliyunSmsChannelConfigModel.accessKeySecret,
                aliyunSmsChannelConfigModel.signName, aliyunSmsChannelConfigModel.retrievePasswordTemplateCode);
    }

    @Override
    public void saveSendRecord(String mobile, String verificationCode, VerificationCodeBusinessType businessType, AbstractResponse response) {
        VerificationCodeMysqlModel model = new VerificationCodeMysqlModel();
        model.setBizId(UUID.randomUUID().toString().replace("-", ""));
        model.setMobile(mobile);
        model.setCode(verificationCode);
        model.setSuccess(String.valueOf(response.success()));
        model.setSendChannel(getVerificationCodeSendChannel());
        model.setBusinessType(businessType);
        model.setRespContent(response.getRespBody());
        verificationCodeRepository.save(model);
    }

    @Override
    public CaptchaSendChannel getVerificationCodeSendChannel() {
        AlertConfigModel alertConfigModel = globalConfigService.getModel(AlertConfigModel.class);
        return alertConfigModel.retrievePasswordCaptchaChannel;
    }

    private void checkEmailConfig() throws StatusCodeWithException {
        MailServerModel mailServerModel = globalConfigService.getModel(MailServerModel.class);
        if (null == mailServerModel) {
            throw new StatusCodeWithException("邮件服务器未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(mailServerModel.getMailHost())) {
            throw new StatusCodeWithException("邮件服务器地址未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (null == mailServerModel.getMailPort()) {
            throw new StatusCodeWithException("邮件服务器端口未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(mailServerModel.getMailUsername())) {
            throw new StatusCodeWithException("邮件用户名未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(mailServerModel.getMailPassword())) {
            throw new StatusCodeWithException("邮件密码未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    private void checkSmsConfig() throws StatusCodeWithException {
        AliyunSmsChannelConfigModel aliyunSmsChannelConfigModel = globalConfigService.getModel(AliyunSmsChannelConfigModel.class);
        if (null == aliyunSmsChannelConfigModel) {
            throw new StatusCodeWithException("阿里云短信通道未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(aliyunSmsChannelConfigModel.accessKeyId)) {
            throw new StatusCodeWithException("阿里云短信通道AccessKeyId未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(aliyunSmsChannelConfigModel.accessKeySecret)) {
            throw new StatusCodeWithException("阿里云短信通道AccessKeySecret未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(aliyunSmsChannelConfigModel.signName)) {
            throw new StatusCodeWithException("阿里云短信签名未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(aliyunSmsChannelConfigModel.retrievePasswordTemplateCode)) {
            throw new StatusCodeWithException("阿里云短信模板码未设置", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

}
