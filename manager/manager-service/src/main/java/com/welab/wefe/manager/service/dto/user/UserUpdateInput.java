package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
public class UserUpdateInput extends AbstractApiInput {
    @Check(require = true)
    private String userId;
    private String nickname;
    private String email;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
