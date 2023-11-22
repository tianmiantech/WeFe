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
import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.data.mongodb.repo.AccountMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.RandomUtil;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.service.account.AbstractAccountService;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.web.service.account.HistoryPasswordItem;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.manager.service.api.account.AuditApi;
import com.welab.wefe.manager.service.dto.account.QueryAccountInput;
import com.welab.wefe.manager.service.dto.account.UpdateInput;
import com.welab.wefe.manager.service.mapper.AccountMapper;
import com.welab.wefe.manager.service.util.ManagerSM4Util;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", readOnly = true,rollbackFor = Exception.class)
public class AccountService extends AbstractAccountService {


    @Autowired
    private AccountMongoRepo accountMongoRepo;
    private AccountMapper mAccountMapper = Mappers.getMapper(AccountMapper.class);

    public void register(Account account) throws StatusCodeWithException {
        boolean isExist = accountMongoRepo.checkAccountIsExist(ManagerSM4Util.encryptPhoneNumber(account.getPhoneNumber()));
        if (isExist) {
            throw new StatusCodeWithException("该账号已存在", StatusCode.PARAMETER_VALUE_INVALID);
        }

        String salt = createRandomSalt();

        account.setPassword(hashPasswordWithSalt(account.getPassword(),salt));
        account.setSalt(salt);

        account.setSuperAdminRole(accountMongoRepo.count() < 1);
        account.setAdminRole(account.getSuperAdminRole());
        account.setEnable(true);
        account.setLastActionTime(new Date());

        if (account.getSuperAdminRole()) {
            account.setAuditStatus(AuditStatus.agree);
        } else {
            account.setAuditStatus(AuditStatus.auditing);
        }
        accountMongoRepo.save(encryptPhoneNumber(account));
    }

    @Override
    public void saveSelfPassword(String password, String salt, JSONArray historyPasswords) throws StatusCodeWithException {
        accountMongoRepo.updatePassword(CurrentAccount.id(), password, salt, historyPasswords);
    }


    public String resetPassword(String accountId,String operatorPassword) throws StatusCodeWithException {

        if (!super.verifyPassword(CurrentAccount.get().getPassword(), operatorPassword, CurrentAccount.get().getSalt())) {
            throw new StatusCodeWithException("管理员密码错误，身份核实失败，已退出登录。", StatusCode.PERMISSION_DENIED);
        }


        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("非管理员无法重置密码。", StatusCode.PERMISSION_DENIED);
        }
        Account account = decryptPhoneNumber(accountMongoRepo.findByAccountId(accountId));
        if (account.getSuperAdminRole()) {
            throw new StatusCodeWithException("不能重置超级管理员密码", StatusCode.PERMISSION_DENIED);
        }

        if (account.getAdminRole() && !CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("只有超级管理员才能重置管理员的密码", StatusCode.PERMISSION_DENIED);
        }

        String historyPassword = account.getPassword();
        String historySalt = account.getSalt();
        JSONArray historyPasswordList = account.getHistoryPasswordList();
        if(historyPasswordList == null){
            historyPasswordList = new JSONArray();
        }

        historyPasswordList.add(new HistoryPasswordItem(historyPassword,historySalt));

        // Regenerate salt
        String salt = createRandomSalt();

        String newPassword = RandomUtil.generateRandomPwd(6);

        String websitePassword = account.getPhoneNumber() + newPassword + account.getPhoneNumber() + account.getPhoneNumber().substring(0, 3) + newPassword.substring(newPassword.length() - 3);

        account.setSalt(salt);
        account.setPassword(hashPasswordWithSalt(Md5.of(websitePassword),salt));
        account.setNeedUpdatePassword(true);
        account.setUpdatedBy(CurrentAccount.id());
        account.setUpdateTime(System.currentTimeMillis());
        account.setHistoryPasswordList(historyPasswordList);

        accountMongoRepo.save(encryptPhoneNumber(account));
        CurrentAccount.logout(accountId);
        return newPassword;
    }



    public void enableUser(String accountId, boolean enable) throws StatusCodeWithException {

        if (!CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法操作。", StatusCode.PERMISSION_DENIED);
        }

        if (accountId.equals(CurrentAccount.id())) {
            throw new StatusCodeWithException("无法对自己进行此操作。", StatusCode.PERMISSION_DENIED);
        }

        String auditComment = enable
                ? CurrentAccount.get().nickname  + "启用了该账号"
                : CurrentAccount.get().nickname + "禁用了该账号";

        accountMongoRepo.enableAccount(accountId, enable, CurrentAccount.id(), auditComment);
    }



    @Transactional(transactionManager = "transactionManagerManager", rollbackFor = Exception.class)
    public void changeSuperAdmin(String accountId) throws StatusCodeWithException {

        if (!CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法操作。", StatusCode.PERMISSION_DENIED);
        }

        if (accountId.equals(CurrentAccount.id())) {
            throw new StatusCodeWithException("无法对自己进行此操作。", StatusCode.PERMISSION_DENIED);
        }


        accountMongoRepo.changeAccountToSuperAdminRole(accountId,CurrentAccount.id());

        accountMongoRepo.cancelSuperAdmin(CurrentAccount.id());

        CurrentAccount.logout(accountId);

        CurrentAccount.logout(accountId);


    }

    /**
     * The administrator reviews the account
     */
    public void audit(AuditApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("您不是管理员，无权执行审核操作！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        accountMongoRepo.auditAccount(input.getAccountId(), input.getAuditStatus(), input.getAuditComment());

    }


    public void changeAccountRole(String accountId, boolean adminRole) throws StatusCodeWithException {
        if (!CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法操作。", StatusCode.PERMISSION_DENIED);
        }
        accountMongoRepo.changeAdminRole(accountId, adminRole);
    }

    public void update(UpdateInput input) {
        accountMongoRepo.update(CurrentAccount.id(), input.getNickname(), input.getEmail());
    }

    public PageOutput<Account> findList(QueryAccountInput input) throws StatusCodeWithException {
        PageOutput<Account> accountPageOutput = accountMongoRepo.findList(
                ManagerSM4Util.encryptPhoneNumber(input.getPhoneNumber()),
                input.getNickname(),
                input.getAdminRole(),
                input.getPageIndex(),
                input.getPageSize()
        );
        List<Account> list = accountPageOutput.getList();
        for(Account account : list) {
            decryptPhoneNumber(account);
        }
        return accountPageOutput;
    }

    @Override
    public AccountInfo getAccountInfo(String phoneNumber) throws StatusCodeWithException {
        Account account = decryptPhoneNumber(accountMongoRepo.findByPhoneNumber(ManagerSM4Util.encryptPhoneNumber(phoneNumber)));
        return toAccountInfo(account);
    }

    @Override
    public AccountInfo getSuperAdmin() throws StatusCodeWithException {
        Account account = decryptPhoneNumber(accountMongoRepo.getSuperAdmin());
        return toAccountInfo(account);
    }

    private AccountInfo toAccountInfo(Account model) {
        if (model == null) {
            return null;
        }

        AccountInfo info = new AccountInfo();
        info.setId(model.getAccountId());
        info.setPhoneNumber(model.getPhoneNumber());
        info.setNickname(model.getNickname());
        info.setPassword(model.getPassword());
        info.setSalt(model.getSalt());
        info.setAuditStatus(model.getAuditStatus());
        info.setAuditComment(model.getAuditComment());
        info.setAdminRole(model.getAdminRole());
        info.setSuperAdminRole(model.getSuperAdminRole());
        info.setEnable(model.getEnable());
        info.setCancelled(model.isCancelled());
        info.setNeedUpdatePassword(model.isNeedUpdatePassword());
        info.setHistoryPasswordList(model.getHistoryPasswordList());

        return info;
    }

    private Account encryptPhoneNumber(Account account) throws StatusCodeWithException {
        if(null == account) {
            return null;
        }
        account.setPhoneNumber(ManagerSM4Util.encryptPhoneNumber(account.getPhoneNumber()));
        return account;
    }

    private Account decryptPhoneNumber(Account account) throws StatusCodeWithException {
        if(null == account) {
            return null;
        }
        account.setPhoneNumber(ManagerSM4Util.decryptPhoneNumber(account.getPhoneNumber()));
        return account;
    }
}
