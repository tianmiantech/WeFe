package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.manager.service.dto.base.PageInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
public class QueryUserInput extends PageInput {
    private String account;
    private String nickname;
    private Boolean adminRole;

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

    public Boolean getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Boolean adminRole) {
        this.adminRole = adminRole;
    }
}
