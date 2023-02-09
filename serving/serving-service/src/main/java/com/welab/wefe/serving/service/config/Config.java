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

    @Value("${wefe.serving.file-path}")
    private String fileBasePath;

    @Value("${email.account.forget.password.subject:忘记密码}")
    private String emailAccountForgetPasswordSubject;

    @Value("${email.account.forget.password.content:您正在执行忘记密码操作。您的验证码是#code#，2分钟内有效，请勿泄漏于他人!}")
    private String emailAccountForgetPasswordContent;
    
    @Value("${psi.batch.size:400000}")
    private int psiBatchSize;

    /**
     * 系统在自身初始化时生成的公私钥类型
     */
    @Value("${initialize.secret.key.type:sm2}")
    private String initializeSecretKeyType;

    public String getFileBasePath() {
        return fileBasePath;
    }

    public void setFileBasePath(String fileBasePath) {
        this.fileBasePath = fileBasePath;
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

    public int getPsiBatchSize() {
        return psiBatchSize;
    }

    public void setPsiBatchSize(int psiBatchSize) {
        this.psiBatchSize = psiBatchSize;
    }

    public String getInitializeSecretKeyType() {
        return initializeSecretKeyType;
    }

    public void setInitializeSecretKeyType(String initializeSecretKeyType) {
        this.initializeSecretKeyType = initializeSecretKeyType;
    }
}
