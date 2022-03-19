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

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.RandomUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.service.account.AbstractAccountService;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.manager.service.api.user.AuditApi;
import com.welab.wefe.manager.service.dto.user.QueryUserInput;
import com.welab.wefe.manager.service.dto.user.UserUpdateInput;
import com.welab.wefe.manager.service.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", rollbackFor = Exception.class)
public class UserService extends AbstractAccountService {


    @Autowired
    private UserMongoRepo userMongoRepo;
    private UserMapper mUserMapper = Mappers.getMapper(UserMapper.class);

    public boolean checkSuperAdminAccountIsExist(String account) {
        boolean result = false;
        User user = userMongoRepo.findByAccount(account);
        if (user != null && user.isSuperAdminRole() && user.isAdminRole()) {
            result = true;
        }
        return result;
    }


    public void register(User user) throws StatusCodeWithException {
        boolean isExist = checkSuperAdminAccountIsExist(user.getAccount());
        if (isExist) {
            throw new StatusCodeWithException("该账号已存在", StatusCode.PARAMETER_VALUE_INVALID);
        }
        String salt = createRandomSalt();
        user.setPassword(hashPasswordWithSalt(user.getPassword(),salt));
        user.setSalt(salt);

        if (!user.isSuperAdminRole()) {
            user.setAuditStatus(AuditStatus.auditing);
        }
        userMongoRepo.save(user);
    }

    @Override
    public void saveSelfPassword(String password, String salt, JSONArray historyPasswords) throws StatusCodeWithException {
        userMongoRepo.changePassword(CurrentAccount.id(), password, salt, historyPasswords);
    }


    @Override
    protected String hashPasswordWithSalt(String inputPassword, String salt) {
        return Md5.of(inputPassword + salt);
    }

    public String resetPassword(String userId,String adminPassword) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("非管理员无法重置密码。", StatusCode.PERMISSION_DENIED);
        }
        User user = userMongoRepo.findByUserId(userId);
        if (user.isSuperAdminRole()) {
            throw new StatusCodeWithException("不能重置超级管理员密码", StatusCode.PERMISSION_DENIED);
        }

        if (!super.verifyPassword(CurrentAccount.get().getPassword(), adminPassword, CurrentAccount.get().getSalt())) {
            throw new StatusCodeWithException("管理员密码错误，身份核实失败，已退出登录。", StatusCode.PERMISSION_DENIED);
        }

        // Regenerate salt
        String salt = createRandomSalt();

        String newPassword =RandomUtil.generateRandomPwd(8);
        user.setSalt(salt);
        user.setPassword(hashPasswordWithSalt(Md5.of(newPassword),salt));
        user.setNeedUpdatePassword(true);
        userMongoRepo.save(user);
        return newPassword;
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

    @Override
    public AccountInfo getAccountInfo(String account) {
        User user = userMongoRepo.findByAccount(account);
        return toAccountInfo(user);
    }

    @Override
    public AccountInfo getSuperAdmin() {
        User user = userMongoRepo.getSuperAdmin();
        return toAccountInfo(user);
    }

    private AccountInfo toAccountInfo(User model) {
        if (model == null) {
            return null;
        }

        AccountInfo info = new AccountInfo();
        info.setId(model.getUserId());
        info.setPhoneNumber(model.getAccount());
        info.setNickname(model.getRealname());
        info.setPassword(model.getPassword());
        info.setSalt(model.getSalt());
        info.setAuditStatus(model.getAuditStatus());
        info.setAuditComment(model.getAuditComment());
        info.setAdminRole(model.isAdminRole());
        info.setSuperAdminRole(model.isSuperAdminRole());
        info.setEnable(model.isEnable());
        info.setCancelled(model.isCancelled());
        info.setNeedUpdatePassword(model.isNeedUpdatePassword());
        info.setHistoryPasswordList(model.getHistoryPasswordList());
        return info;
    }
}
