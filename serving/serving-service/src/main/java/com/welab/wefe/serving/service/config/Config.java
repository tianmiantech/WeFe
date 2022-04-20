package com.welab.wefe.serving.service.config;

import com.welab.wefe.common.web.config.CommonConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = {"file:${config.path}"}, encoding = "utf-8")
@ConfigurationProperties
public class Config extends CommonConfig {

    @Value("${wefe.serving.base-url}")
    private String SERVING_BASE_URL;

    @Value("${wefe.serving.file-path}")
    private String fileBasePath;

    @Value("${wefe.redis.host}")
    private String redisHost;

    @Value("${wefe.redis.port}")
    private String redisPort;

    @Value("${wefe.redis.password}")
    private String redisPassword;

    @Value("${wefe.service.cache.type}")
    private String serviceCacheType;

    @Value("${sm4.secret.key:}")
    private String sm4SecretKey;

    @Value("${verification.code.send.channel:email}")
    private String verificationCodeSendChannel;

    @Value("${sms.aliyun.sign.name:xxx}")
    private String smsAliyunSignName;

    @Value("${sms.aliyun.account.forget.password.verification.code.template.code:xxx}")
    private String smsAliyunAccountForgetPasswordVerificationCodeTemplateCode;

    @Value("${sms.aliyun.member.register.verification.code.template.code:xxx}")
    private String smsAliyunMemberregisterVerificationCodeTemplateCode;

    @Value("${email.account.forget.password.subject:忘记密码}")
    private String emailAccountForgetPasswordSubject;

    @Value("${email.account.forget.password.content:您正在执行忘记密码操作。您的验证码是#code#，2分钟内有效，请勿泄漏于他人!}")
    private String emailAccountForgetPasswordContent;

    @Value("${sms.access_key_id:xxx}")
    private String smsAccessKeyId;

    @Value("${sms.access_key_secret:xxx}")
    private String smsAccessKeySecret;
    
    @Value("${wefe.mail_server.mail_host}")
    private String mailHost;
    
    @Value("${wefe.mail_server.mail_password}")
    private String mailPassword;
    
    @Value("${wefe.mail_server.port}")
    private String mailPort;
    
    @Value("${wefe.mail_server.username}")
    private String mailUsername;
    
    
    @Value("${encrypt.phone.number.open:false}")
    private boolean encryptPhoneNumberOpen;


    public String getFileBasePath() {
        return fileBasePath;
    }

    public void setFileBasePath(String fileBasePath) {
        this.fileBasePath = fileBasePath;
    }

    public String getSERVING_BASE_URL() {
        return SERVING_BASE_URL;
    }

    public void setSERVING_BASE_URL(String sERVING_BASE_URL) {
        SERVING_BASE_URL = sERVING_BASE_URL;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getServiceCacheType() {
        return serviceCacheType;
    }

    public void setServiceCacheType(String serviceCacheType) {
        this.serviceCacheType = serviceCacheType;
    }

    public String getSm4SecretKey() {
        return sm4SecretKey;
    }

    public void setSm4SecretKey(String sm4SecretKey) {
        this.sm4SecretKey = sm4SecretKey;
    }

    public String getVerificationCodeSendChannel() {
        return verificationCodeSendChannel;
    }

    public void setVerificationCodeSendChannel(String verificationCodeSendChannel) {
        this.verificationCodeSendChannel = verificationCodeSendChannel;
    }

    public String getSmsAliyunSignName() {
        return smsAliyunSignName;
    }

    public void setSmsAliyunSignName(String smsAliyunSignName) {
        this.smsAliyunSignName = smsAliyunSignName;
    }

    public String getSmsAliyunAccountForgetPasswordVerificationCodeTemplateCode() {
        return smsAliyunAccountForgetPasswordVerificationCodeTemplateCode;
    }

    public void setSmsAliyunAccountForgetPasswordVerificationCodeTemplateCode(
            String smsAliyunAccountForgetPasswordVerificationCodeTemplateCode) {
        this.smsAliyunAccountForgetPasswordVerificationCodeTemplateCode = smsAliyunAccountForgetPasswordVerificationCodeTemplateCode;
    }

    public String getSmsAliyunMemberregisterVerificationCodeTemplateCode() {
        return smsAliyunMemberregisterVerificationCodeTemplateCode;
    }

    public void setSmsAliyunMemberregisterVerificationCodeTemplateCode(
            String smsAliyunMemberregisterVerificationCodeTemplateCode) {
        this.smsAliyunMemberregisterVerificationCodeTemplateCode = smsAliyunMemberregisterVerificationCodeTemplateCode;
    }

    public String getEmailAccountForgetPasswordSubject() {
        return emailAccountForgetPasswordSubject;
    }

    public void setEmailAccountForgetPasswordSubject(String emailAccountForgetPasswordSubject) {
        this.emailAccountForgetPasswordSubject = emailAccountForgetPasswordSubject;
    }

    public String getEmailAccountForgetPasswordContent() {
        return emailAccountForgetPasswordContent;
    }

    public void setEmailAccountForgetPasswordContent(String emailAccountForgetPasswordContent) {
        this.emailAccountForgetPasswordContent = emailAccountForgetPasswordContent;
    }

    public String getSmsAccessKeyId() {
        return smsAccessKeyId;
    }

    public void setSmsAccessKeyId(String smsAccessKeyId) {
        this.smsAccessKeyId = smsAccessKeyId;
    }

    public String getSmsAccessKeySecret() {
        return smsAccessKeySecret;
    }

    public void setSmsAccessKeySecret(String smsAccessKeySecret) {
        this.smsAccessKeySecret = smsAccessKeySecret;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public String getMailPort() {
        return mailPort;
    }

    public void setMailPort(String mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailUsername() {
        return mailUsername;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }
    
    public boolean isEncryptPhoneNumberOpen() {
        return encryptPhoneNumberOpen;
    }

    public void setEncryptPhoneNumberOpen(boolean encryptPhoneNumberOpen) {
        this.encryptPhoneNumberOpen = encryptPhoneNumberOpen;
    }
}
