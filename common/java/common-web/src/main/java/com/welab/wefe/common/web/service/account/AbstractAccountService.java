/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.web.service.account;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.LoginSecurityPolicy;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.service.CaptchaService;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author zane
 * @date 2022/3/16
 */
public abstract class AbstractAccountService {

    /**
     * 使用 username 获取用户信息
     *
     * @param phoneNumber 登录账号，通常是手机号。
     */
    public abstract AccountInfo getAccountInfo(String phoneNumber);

    /**
     * 获取系统中的超级管理员
     */
    public abstract AccountInfo getSuperAdmin();

    /**
     * @param phoneNumber 用户唯一标识（用户登录账号：通常是手机号）
     * @param password    登录密码
     * @param captchaKey  验证码key
     * @param captchaCode 验证码
     * @return 登录成功后的 token
     */
    public String login(String phoneNumber, String password, String captchaKey, String captchaCode) throws StatusCodeWithException {

        CommonConfig config = Launcher.getBean(CommonConfig.class);

        if (!config.getEnvName().isTestEnv()) {
            // Verification code verification
            if (!CaptchaService.verify(captchaKey, captchaCode)) {
                throw new StatusCodeWithException("验证码错误！", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        // Check if it's in the small black room
        if (LoginSecurityPolicy.inDarkRoom(phoneNumber)) {
            throw new StatusCodeWithException("【小黑屋】账号已被禁止登陆，请一个小时后再试。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        AccountInfo account = getAccountInfo(phoneNumber);
        // phone number error
        if (account == null || !account.password.equals(hashInputPassword(password, account.salt))) {
            if (account != null) {
                LoginSecurityPolicy.onLoginFail(phoneNumber);
            }
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("手机号或密码错误，连续错误 6 次会被禁止登陆，可以联系管理员重置密码找回账号。");
        }

        if (!account.enable) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员。", StatusCode.PERMISSION_DENIED);
        }

        if (account.cancelled) {
            throw new StatusCodeWithException("账号已被注销，无法使用此账号。", StatusCode.PERMISSION_DENIED);
        }

        // Check audit status
        if (account.auditStatus != null) {
            switch (account.auditStatus) {
                case auditing:
                    AccountInfo superAdmin = getSuperAdmin();

                    throw new StatusCodeWithException("账号尚未审核，请联系管理员 " + superAdmin.nickname + " （或其他任意管理员）对您的账号进行审核后再尝试登录！", StatusCode.PARAMETER_VALUE_INVALID);
                case disagree:
                    throw new StatusCodeWithException("账号审核不通过：" + account.auditStatus, StatusCode.PARAMETER_VALUE_INVALID);
                default:
            }
        }

        String token = CurrentAccount.generateToken();

        CurrentAccount.logined(token, account);

        // Record a successful login event
        LoginSecurityPolicy.onLoginSuccess(phoneNumber);

        return token;
    }

    /**
     * 对输入的密码进行 hash，用于与数据库储存的密码进行比对。
     * <p>
     * 如果hash方式与此默认方式不同，请在子类中重写此方法。
     */
    protected String hashInputPassword(String inputPassword, String salt) {
        return Sha1.of(inputPassword + salt);
    }

    /**
     * 生产随机盐
     */
    protected String createRandomSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[16];
        r.nextBytes(salt);

        return Base64Util.encode(salt);
    }
}
