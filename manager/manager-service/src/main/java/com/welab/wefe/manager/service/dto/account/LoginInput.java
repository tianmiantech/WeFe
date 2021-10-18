package com.welab.wefe.manager.service.dto.account;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @Author Jervis
 * @Date 2020-06-04
 **/
public class LoginInput extends AbstractApiInput {

    @Check(require = true)
    private String account;

    @Check(require = true)
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
