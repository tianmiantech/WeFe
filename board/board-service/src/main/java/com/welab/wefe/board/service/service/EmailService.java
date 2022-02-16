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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.entity.MessageMysqlModel;
import com.welab.wefe.board.service.dto.globalconfig.MailServerModel;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.MessageLevel;
import com.welab.wefe.common.wefe.enums.ProducerType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * email service
 *
 * @author aaron.li
 **/
@Service
public class EmailService extends AbstractService {
    private static final String MAIL_DEFAULT_ENCODING = "UTF-8";
    private static final String MAIL_SMTP_AUTH = "true";
    private static final String MAIL_SMTP_WRITE_TIMEOUT = "5000";
    private static final String MAIL_SMTP_TIMEOUT = "5000";
    private static final String MAIL_SMTP_CONNECTION_TIMEOUT = "5000";

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * Send approval task notification email (multiple persons)
     *
     * @return Failed to send email address list
     */
    public Set<String> sendApprovalJobNotifyMail() {
        String subject = "这是一个标题";
        String content = "这是正文";

        // Failed to send mailbox list
        Set<String> sendFailEmails = new HashSet<>(16);
        try {
            Set<String> totalEmails = getTotalEmails();
            if (CollectionUtils.isEmpty(totalEmails)) {
                saveTotalSendFailMessage("邮件接收人为空");
                return sendFailEmails;
            }

            MailServerModel mailServer = globalConfigService.getMailServer();
            sendFailEmails = sendMail(mailServer.getMailUsername(), totalEmails, subject, content);
            // All sent successfully
            if (CollectionUtils.isEmpty(sendFailEmails)) {
                return sendFailEmails;
            }
            // There are some invalid recipients, so you need to filter the invalid address and send it again
            // (because if there is an invalid recipient address in the receiver list, the whole sending will fail)
            totalEmails.removeAll(sendFailEmails);
            if (CollectionUtils.isEmpty(totalEmails)) {
                saveTotalSendFailMessage("不存在有效的邮件接收人地址");
                return sendFailEmails;
            }
            savePartSendFailMessage("部分接收人地址无效：" + sendFailEmails);
            // Send again
            sendFailEmails = sendMail(mailServer.getMailUsername(), totalEmails, subject, content);
        } catch (Exception e) {
            saveTotalSendFailMessage("失败原因：" + e.getMessage());
            LOG.error("Sending mail exception：", e);
        }
        return sendFailEmails;
    }


    /**
     * Send multiple emails
     *
     * @param from    Sender
     * @param to      Recipient list
     * @param subject subject
     * @param content content
     * @return If the sending fails, the list of failed recipient addresses will be returned
     */
    public Set<String> sendMail(String from, Set<String> to, String subject, String content) throws Exception {
        JavaMailSenderImpl javaMailSender = getMailSender();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mineHelper = new MimeMessageHelper(mimeMessage, true);
        mineHelper.setFrom(from);
        mineHelper.setTo(to.toArray(new String[0]));
        mineHelper.setSubject(subject);
        mineHelper.setText(content, true);

        try {
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            LOG.error("The mail recipient has an invalid address：", e);
            // There is an illegal address in the recipient
            return getInvalidAddress(e);
        } catch (Exception e) {
            LOG.error("Sending mail exception：", e);
            throw e;
        }

        return new HashSet<>(16);
    }


    /**
     * Get message sender
     */
    private JavaMailSenderImpl getMailSender() throws Exception {

        MailServerModel mailServer = globalConfigService.getMailServer();
        if (mailServer == null) {
            throw new Exception("邮件服务器未设置");
        }
        if (StringUtil.isEmpty(mailServer.getMailHost())) {
            throw new Exception("邮件服务器地址未设置");
        }
        if (null == mailServer.getMailPort()) {
            throw new Exception("邮件服务器端口未设置");
        }
        if (StringUtil.isEmpty(mailServer.getMailUsername())) {
            throw new Exception("邮件用户名未设置");
        }
        if (StringUtil.isEmpty(mailServer.getMailPassword())) {
            throw new Exception("邮件密码未设置");
        }
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailServer.getMailHost());
        javaMailSender.setPort(mailServer.getMailPort());
        javaMailSender.setDefaultEncoding(MAIL_DEFAULT_ENCODING);
        javaMailSender.setUsername(mailServer.getMailUsername());
        javaMailSender.setPassword(mailServer.getMailPassword());
        javaMailSender.setProtocol(JavaMailSenderImpl.DEFAULT_PROTOCOL);

        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.smtp.auth", MAIL_SMTP_AUTH);
        mailProperties.setProperty("mail.smtp.writetimeout", MAIL_SMTP_WRITE_TIMEOUT);
        mailProperties.setProperty("mail.smtp.timeout", MAIL_SMTP_TIMEOUT);
        mailProperties.setProperty("mail.smtp.connectiontimeout", MAIL_SMTP_CONNECTION_TIMEOUT);
        javaMailSender.setJavaMailProperties(mailProperties);

        return javaMailSender;
    }


    /**
     * Get all email addresses
     */
    private Set<String> getTotalEmails() {
        Set<String> totalEmails = new HashSet<>(16);
        List<AccountMysqlModel> accountMysqlModelList = accountService.queryAll();
        if (CollectionUtils.isNotEmpty(accountMysqlModelList)) {
            for (AccountMysqlModel model : accountMysqlModelList) {
                if (StringUtil.isNotEmpty(model.getEmail())) {
                    totalEmails.add(model.getEmail());
                }
            }
        }
        return totalEmails;
    }


    /**
     * Save sending failure information
     */
    private void saveTotalSendFailMessage(String errorMsg) {
        saveErrorMessage("Board：审批任务邮件发送失败", errorMsg);
    }

    private void savePartSendFailMessage(String errorMsg) {
        saveErrorMessage("Board：审批任务邮件部分发送失败", errorMsg);
    }


    private void saveErrorMessage(String title, String content) {
        try {
            MessageMysqlModel messageMysqlModel = new MessageMysqlModel();
            messageMysqlModel.setProducer(ProducerType.board);
            messageMysqlModel.setLevel(MessageLevel.error);
            messageMysqlModel.setTitle(title);
            messageMysqlModel.setContent(content);
            messageMysqlModel.setUnread(true);
            messageService.add(messageMysqlModel);
        } catch (Exception e) {
            LOG.error("Save sending failure information Exception：", e);
        }
    }


    /**
     * Get invalid recipient list
     */
    private static Set<String> getInvalidAddress(MailSendException e) {
        Set<String> failMails = new HashSet<>();
        for (Exception exception : e.getFailedMessages().values()) {
            if (exception instanceof SendFailedException) {
                for (Address address : ((SendFailedException) exception).getInvalidAddresses()) {
                    failMails.add(address.toString());
                }
            }
        }
        return failMails;
    }
}
