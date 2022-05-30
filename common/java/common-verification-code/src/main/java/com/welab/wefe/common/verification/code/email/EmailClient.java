/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.verification.code.email;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Email client
 *
 * @author aaron.li
 * @date 2022/1/19 15:49
 **/
public class EmailClient extends AbstractClient {
    protected final Logger LOG = LoggerFactory.getLogger(EmailClient.class);
    public final static String MAIL_HOST = "mailHost";
    public final static String MAIL_PORT = "mailPort";
    public final static String MAIL_USERNAME = "mailUsername";
    public final static String MAIL_PASSWORD = "mailPassword";
    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String SUBJECT = "subject";
    public final static String CONTENT = "content";

    public EmailClient(Map<String, Object> extendParams) {
        super(extendParams);
    }

    @Override
    public AbstractResponse send(String mobile, String verificationCode) throws Exception {
        JObject extendParams = JObject.create(getExtendParams());
        JavaMailSender javaMailSender = new JavaMailSender(extendParams.getString(MAIL_HOST),
                extendParams.getInteger(MAIL_PORT),
                extendParams.getString(MAIL_USERNAME), extendParams.getString(MAIL_PASSWORD));
        EmailSendResult emailSendResult = new EmailSendResult();
        emailSendResult.setMessage("发送成功");
        javaMailSender.sendHtmlMail(extendParams.getString(FROM), extendParams.getString(TO),
                extendParams.getString(SUBJECT), extendParams.getString(CONTENT));
        return new EmailResponse(emailSendResult);
    }

    public static Map<String, Object> buildExtendParams(String mailHost, int mailPort, String mailUsername, String mailPassword,
                                                        String from, String to, String subject, String content) {
        Map<String, Object> extendParams = new HashMap<>(16);
        extendParams.put(MAIL_HOST, mailHost);
        extendParams.put(MAIL_PORT, mailPort);
        extendParams.put(MAIL_USERNAME, mailUsername);
        extendParams.put(MAIL_PASSWORD, mailPassword);
        extendParams.put(FROM, from);
        extendParams.put(TO, to);
        extendParams.put(SUBJECT, subject);
        extendParams.put(CONTENT, content);
        return extendParams;
    }
}