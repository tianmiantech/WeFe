package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.web.dto.AbstractApiOutput;

/**
 * @author yuxin.zhang
 */
public class LoginOutput extends AbstractApiOutput {

    private String token;

    private String account;

    private String nickname;

    private String email;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
