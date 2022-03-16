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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.LoginSecurityPolicy;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.service.CaptchaService;

import java.security.SecureRandom;
import java.util.List;
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
     * 保存新的密码
     */
    public abstract void saveSelfPassword(String password, String salt, JSONArray historyPasswords) throws StatusCodeWithException;

    public void updatePassword(String oldPassword, String newPassword) throws StatusCodeWithException {
        String phoneNumber = CurrentAccount.phoneNumber();
        if (phoneNumber == null) {
            throw new StatusCodeWithException(StatusCode.LOGIN_REQUIRED);
        }
        AccountInfo model = getAccountInfo(phoneNumber);
        // 检查旧密码是否正确
        if (!StringUtil.equals(model.getPassword(), hashPasswordWithSalt(oldPassword, model.getSalt()))) {
            CurrentAccount.logout();
            throw new StatusCodeWithException("您输入的旧密码不正确，为确保安全，请重新登录后重试。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        int historyCount = 4;
        if (inHistoryPassword(newPassword, historyCount, model)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("您输入的新密码必须与前四次设置的密码不一致");
        }

        // 当前密码成为历史
        model
                .getHistoryPasswordList()
                .add(
                        new HistoryPasswordItem(model.getPassword(), model.getSalt())
                );

        // 历史密码
        String historyPasswordListString = JSON.toJSONString(model.getPasswordHistoryList(historyCount - 1));
        // 生成新的盐和密码
        String salt = createRandomSalt();
        newPassword = hashPasswordWithSalt(newPassword, salt);

        saveSelfPassword(newPassword, salt, JSON.parseArray(historyPasswordListString));
        CurrentAccount.logout(model.getId());
    }

    /**
     * 检查密码是否在历史密码中
     *
     * @param newPassword 新密码
     * @param count       历史密码的个数
     */
    public boolean inHistoryPassword(String newPassword, int count, AccountInfo model) {
        // 检查新密码是否与当前密码一样
        if (model.password.equals(hashPasswordWithSalt(newPassword, model.getSalt()))) {
            return true;
        }
        // 当前密码算一次，所以要减一。
        count -= 1;
        List<HistoryPasswordItem> list = model.getPasswordHistoryList(count);
        for (HistoryPasswordItem item : list) {
            if (item.password.equals(hashPasswordWithSalt(newPassword, item.salt))) {
                return true;
            }
        }
        return false;
    }

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
        if (account == null || !account.password.equals(hashPasswordWithSalt(password, account.salt))) {
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
    protected String hashPasswordWithSalt(String inputPassword, String salt) {
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
