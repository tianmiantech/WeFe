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

package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.LoginSecurityPolicy;
import com.welab.wefe.common.web.service.CaptchaService;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.manager.service.api.user.AuditApi;
import com.welab.wefe.manager.service.constant.UserConstant;
import com.welab.wefe.manager.service.dto.user.LoginInput;
import com.welab.wefe.manager.service.dto.user.LoginOutput;
import com.welab.wefe.manager.service.dto.user.QueryUserInput;
import com.welab.wefe.manager.service.dto.user.UserUpdateInput;
import com.welab.wefe.manager.service.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", rollbackFor = Exception.class)
public class UserService {


    @Autowired
    private UserMongoRepo userMongoRepo;
    private UserMapper mUserMapper = Mappers.getMapper(UserMapper.class);

    public boolean checkAdminAccountIsExist(String account) {
        boolean result = false;
        User user = userMongoRepo.findByAccount(account);
        if (user != null && user.isSuperAdminRole() && user.isAdminRole()) {
            result = true;
        }
        return result;
    }


    public void register(User user) throws StatusCodeWithException {
        boolean isExist = checkAdminAccountIsExist(user.getAccount());
        if (isExist) {
            throw new StatusCodeWithException("该账号已存在", StatusCode.PARAMETER_VALUE_INVALID);
        }
        String salt = createRandomSalt();
        user.setPassword(Md5.of(user.getPassword() + salt));
        user.setSalt(salt);

        if (!user.isSuperAdminRole()) {
            user.setAuditStatus(AuditStatus.auditing);
        }
        userMongoRepo.save(user);
    }

    public void changePassword(String oldPassword, String newPassword) throws StatusCodeWithException {
        User user = userMongoRepo.findByUserId(CurrentAccount.id());

        // Check old password
        if (!user.getPassword().equals(Md5.of(oldPassword + user.getSalt()))) {
            CurrentAccount.logout(CurrentAccount.id());
            throw new StatusCodeWithException("账号已被禁止登陆，请一个小时后再试，或联系管理员。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Regenerate salt
        String salt = createRandomSalt();

        newPassword = Md5.of(newPassword + salt);

        userMongoRepo.changePassword(CurrentAccount.id(), newPassword, salt);
        CurrentAccount.logout(CurrentAccount.id());
    }

    public void resetPassword(String userId) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("非管理员无法重置密码。", StatusCode.PERMISSION_DENIED);
        }
        User user = userMongoRepo.findByUserId(userId);
        if (user.isSuperAdminRole()) {
            throw new StatusCodeWithException("不能重置超级管理员密码", StatusCode.PERMISSION_DENIED);
        }

        // Regenerate salt
        String salt = createRandomSalt();
        String newPassword = Md5.of(Md5.of(UserConstant.DEFAULT_PASSWORD) + salt);
        user.setSalt(salt);
        user.setPassword(newPassword);
        userMongoRepo.save(user);
    }

    public void enableUser(String userId, boolean enable) throws StatusCodeWithException {
        if (!CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法操作。", StatusCode.PERMISSION_DENIED);
        }

        userMongoRepo.enableUser(userId, enable);
    }

    /**
     * The administrator reviews the account
     */
    public void audit(AuditApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("您不是管理员无法进行此操作。", StatusCode.PERMISSION_DENIED);
        }

        userMongoRepo.auditUser(input.getUserId(), input.getAuditStatus(), input.getAuditComment());

    }


    public void changeUserRole(String userId, boolean adminRole) throws StatusCodeWithException {
        if (!CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法操作。", StatusCode.PERMISSION_DENIED);
        }
        userMongoRepo.changeUserRole(userId, adminRole);
    }

    public void update(UserUpdateInput input) {
        userMongoRepo.update(CurrentAccount.id(), input.getRealname(), input.getEmail());
    }

    public PageOutput<User> findList(QueryUserInput input) {
        return userMongoRepo.findList(
                input.getAccount(),
                input.getRealname(),
                input.getAdminRole(),
                input.getPageIndex(),
                input.getPageSize()
        );
    }

    private String createRandomSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[16];
        r.nextBytes(salt);

        return Base64Util.encode(salt);
    }


    public LoginOutput login(LoginInput input) throws StatusCodeWithException {
        // Verification code verification
        if (!CaptchaService.verify(input.getKey(), input.getCode())) {
            throw new StatusCodeWithException("验证码错误！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        User user = userMongoRepo.findByAccount(input.getAccount());
        if (user == null) {
            throw new StatusCodeWithException("账号不存在!", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Check if it's in the small black room
        if (LoginSecurityPolicy.inDarkRoom(input.getAccount())) {
            throw new StatusCodeWithException("账号已被禁止登陆，请一个小时后再试，或联系管理员。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (!user.getPassword().equals(Md5.of(input.getPassword() + user.getSalt()))) {
            // Log a login failure event
            LoginSecurityPolicy.onLoginFail(input.getAccount());
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("密码错误, 连续错误 6 次会被禁止登陆，可以联系管理员重置密码找回账号。");
        }

        if (user.getAuditStatus() != AuditStatus.agree) {
            throw new StatusCodeWithException("账号尚未审核，请联系管理员对您的账号审核后再尝试登录！", StatusCode.PARAMETER_VALUE_INVALID);
        }


        if (!user.isEnable()) {
            throw new StatusCodeWithException("账号被禁用，请联系管理员!", StatusCode.PARAMETER_VALUE_INVALID);
        }


        LoginOutput output = mUserMapper.transfer(user);
        String token = CurrentAccount.generateToken();
        output.setToken(token);
        CurrentAccount.logined(token, user.getUserId(), user.getAccount(), user.isAdminRole(), user.isSuperAdminRole(), user.isEnable());
        // Record a successful login event
        LoginSecurityPolicy.onLoginSuccess(input.getAccount());
        return output;
    }
}
