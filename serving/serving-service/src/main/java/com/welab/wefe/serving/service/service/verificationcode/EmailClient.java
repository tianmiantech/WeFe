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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.VerificationCodeBusinessType;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.MailServerModel;
import com.welab.wefe.serving.service.service.EmailService;
import com.welab.wefe.serving.service.utils.ServingSM4Util;

/**
 * Email client
 *
 * @author aaron.li
 * @date 2022/1/19 15:49
 **/
public class EmailClient extends AbstractClient {
    protected final Logger LOG = LoggerFactory.getLogger(EmailClient.class);

    public EmailClient(Map<String, Object> extendParams) {
        super(extendParams);
    }

    @Override
    public AbstractResponse send(String mobile, String verificationCode) throws Exception {
        AccountRepository accountRepository = Launcher.CONTEXT.getBean(AccountRepository.class);
        AccountMySqlModel model = accountRepository.findOne("phoneNumber", ServingSM4Util.encryptPhoneNumber(mobile), AccountMySqlModel.class);
        if (StringUtil.isEmpty(model.getEmail())) {
            throw new StatusCodeWithException("用户未设置邮箱地址", StatusCode.PERMISSION_DENIED);
        }

        Config config = Launcher.CONTEXT.getBean(Config.class);
        MailServerModel mailServer = new MailServerModel(config.getMailHost(),
                Integer.valueOf(config.getMailPort()), config.getMailUsername(), config.getMailPassword());
        String businessTypeStr = String.valueOf(getExtendParams().get("businessType"));
        VerificationCodeBusinessType businessType = VerificationCodeBusinessType.valueOf(businessTypeStr);
        String subject = String.valueOf(getExtendParams().get("subject"));
        String content = String.valueOf(getExtendParams().get("content"));;
        if (VerificationCodeBusinessType.accountForgetPassword.equals(businessType)) {
            content = content.replace("#code#", verificationCode);
        }

        EmailSendResult emailSendResult = new EmailSendResult();
        emailSendResult.setMessage("发送成功");
        EmailService emailService = Launcher.CONTEXT.getBean(EmailService.class);
        emailService.sendMail(mailServer.getMailUsername(), model.getEmail(), subject, content);
        return new EmailResponse(emailSendResult);
    }
}