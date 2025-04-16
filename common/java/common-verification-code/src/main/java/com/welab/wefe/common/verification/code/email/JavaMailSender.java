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

package com.welab.wefe.common.verification.code.email;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class JavaMailSender {
    private static final String MAIL_DEFAULT_ENCODING = "UTF-8";
    private static final String MAIL_SMTP_AUTH = "true";
    private static final String MAIL_SMTP_WRITE_TIMEOUT = "30000";
    private static final String MAIL_SMTP_TIMEOUT = "30000";
    private static final String MAIL_SMTP_CONNECTION_TIMEOUT = "30000";
    private static final String MAIL_SMTP_SSL_ENABLE = "true";
    private static final String MAIL_DEBUG = "true";

    private JavaMailSenderImpl javaMailSenderImpl;

    public JavaMailSender(String mailHost, int mailPort, String mailUsername, String mailPassword) {
        javaMailSenderImpl = new JavaMailSenderImpl();
        javaMailSenderImpl.setHost(mailHost);
        javaMailSenderImpl.setPort(mailPort);
        javaMailSenderImpl.setUsername(mailUsername);
        javaMailSenderImpl.setPassword(mailPassword);
        javaMailSenderImpl.setDefaultEncoding(MAIL_DEFAULT_ENCODING);
        javaMailSenderImpl.setProtocol(JavaMailSenderImpl.DEFAULT_PROTOCOL);
        javaMailSenderImpl.setJavaMailProperties(getDefaultProperties());
    }

    public void setJavaMailProperties(Properties properties) {
        javaMailSenderImpl.setJavaMailProperties(properties);
    }

    public void sendHtmlMail(String from, String to, String subject, String content) throws Exception {
        sendMail(from, to, subject, content, true);
    }

    public void sendMail(String from, String to, String subject, String content, boolean html) throws Exception {
        MimeMessage mimeMessage = javaMailSenderImpl.createMimeMessage();
        MimeMessageHelper mineHelper = new MimeMessageHelper(mimeMessage, true);
        mineHelper.setFrom(from);
        mineHelper.setTo(to);
        mineHelper.setSubject(subject);
        mineHelper.setText(content, html);
        javaMailSenderImpl.send(mimeMessage);
    }

    private Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", MAIL_SMTP_AUTH);
        properties.setProperty("mail.smtp.writetimeout", MAIL_SMTP_WRITE_TIMEOUT);
        properties.setProperty("mail.smtp.timeout", MAIL_SMTP_TIMEOUT);
        properties.setProperty("mail.smtp.connectiontimeout", MAIL_SMTP_CONNECTION_TIMEOUT);
        properties.setProperty("mail.smtp.ssl.enable", MAIL_SMTP_SSL_ENABLE);
        properties.setProperty("mail.debug", MAIL_DEBUG);
        return properties;
    }
}
