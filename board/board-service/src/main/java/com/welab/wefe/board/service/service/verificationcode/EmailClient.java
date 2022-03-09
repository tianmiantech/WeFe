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

package com.welab.wefe.board.service.service.verificationcode;

import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.dto.globalconfig.MailServerModel;
import com.welab.wefe.board.service.service.EmailService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.VerificationCodeBusinessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
        AccountMysqlModel model = accountRepository.findOne("phoneNumber", mobile, AccountMysqlModel.class);
        if (StringUtil.isEmpty(model.getEmail())) {
            throw new StatusCodeWithException("用户未设置邮箱地址", StatusCode.PERMISSION_DENIED);
        }

        EmailService emailService = Launcher.CONTEXT.getBean(EmailService.class);
        GlobalConfigService globalConfigService = Launcher.CONTEXT.getBean(GlobalConfigService.class);
        MailServerModel mailServer = globalConfigService.getMailServer();
        String businessTypeStr = String.valueOf(getExtendParams().get("businessType"));
        VerificationCodeBusinessType businessType = VerificationCodeBusinessType.valueOf(businessTypeStr);
        String subject = String.valueOf(getExtendParams().get("subject"));
        String content = String.valueOf(getExtendParams().get("content"));;
        if (VerificationCodeBusinessType.accountForgetPassword.equals(businessType)) {
            content = content.replace("#code#", verificationCode);
        }

        EmailSendResult emailSendResult = new EmailSendResult();
        emailSendResult.setMessage("发送成功");
        emailService.sendMail(mailServer.getMailUsername(), model.getEmail(), subject, content);
        return new EmailResponse(emailSendResult);
    }
}