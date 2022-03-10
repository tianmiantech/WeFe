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

package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.service.CaptchaService;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
public class RegisterInput extends AbstractApiInput {
    @Check(require = true)
    private String account;
    @Check(require = true)
    private String password;
    @Check(require = true)
    private String realname;
    private String email;
    @Check(require = true, desc = "验证码标识")
    private String key;
    @Check(require = true, desc = "验证码")
    private String code;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();
        // Verification code verification
        if (!CaptchaService.verify(key, code)) {
            throw new StatusCodeWithException("验证码错误！", StatusCode.PARAMETER_VALUE_INVALID);
        }

    }


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

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
