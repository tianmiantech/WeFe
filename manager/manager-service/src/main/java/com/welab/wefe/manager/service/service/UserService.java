/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import com.welab.wefe.manager.service.constant.UserConstant;
import com.welab.wefe.manager.service.dto.user.QueryUserInput;
import com.welab.wefe.manager.service.dto.user.UserUpdateInput;
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
        userMongoRepo.save(user);
    }

    public void changePassword(String oldPassword, String newPassword) throws StatusCodeWithException {
        User user = userMongoRepo.findByUserId(CurrentAccount.id());

        // Check old password
        if (!user.getPassword().equals(Md5.of(oldPassword + user.getSalt()))) {
            throw new StatusCodeWithException("您输入的旧密码不正确", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Regenerate salt
        String salt = createRandomSalt();

        newPassword = Md5.of(newPassword + salt);

        userMongoRepo.changePassword(CurrentAccount.id(), newPassword, salt);
    }

    public void resetPassword(String userId) throws StatusCodeWithException {
        if (CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("非管理员无法重置密码。", StatusCode.PERMISSION_DENIED);
        }
        // Regenerate salt
        String salt = createRandomSalt();
        String newPassword = Md5.of(Md5.of(UserConstant.DEFAULT_PASSWORD) + salt);

        userMongoRepo.changePassword(userId, newPassword, salt);
    }

    public void changeUserRole(String userId, boolean adminRole) throws StatusCodeWithException {
        if (CurrentAccount.isSuperAdmin()) {
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
                input.getNickname(),
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
}
