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

import com.welab.wefe.common.SecurityUtil;
import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.data.mongodb.repo.AccountMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.service.account.SsoAccountInfo;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.manager.service.api.account.SsoLoginApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", readOnly = true, rollbackFor = Exception.class)
public class AccountService {

    @Autowired
    private AccountMongoRepo accountMongoRepo;


    public SsoLoginApi.Output ssoLogin() throws StatusCodeWithException {
        SsoAccountInfo accountInfo = CurrentAccountUtil.get();
        Account account = accountMongoRepo.findByAccountId(accountInfo.getId());
        if (null == account) {
            String salt = SecurityUtil.createRandomSalt();
            String password = Sha1.of(UUID.randomUUID().toString().replace("-", "") + salt);
            account = new Account();
            account.setAccountId(accountInfo.getId());
            account.setPhoneNumber(DatabaseEncryptUtil.encrypt(accountInfo.getPhoneNumber()));
            account.setSalt(salt);
            account.setPassword(password);
            account.setNickname(accountInfo.getName());
            account.setEmail(accountInfo.getEmail());
            account.setSuperAdminRole(true);
            account.setAdminRole(true);
            account.setAuditStatus(AuditStatus.agree);
            account.setEnable(true);
            account.setCancelled(false);
            account.setNeedUpdatePassword(false);
            account.setLastActionTime(new Date());
            account.setUpdatedBy(accountInfo.getId());
            accountMongoRepo.save(account);
        } else {
            String nickName = account.getNickname();
            String phoneNumber = DatabaseEncryptUtil.decrypt(account.getPhoneNumber());
            String email = account.getEmail();
            boolean needUpdate = false;
            if (StringUtil.isNotEmpty(nickName) && !nickName.equals(accountInfo.getName())) {
                account.setNickname(accountInfo.getName());
                needUpdate = true;
            }
            if (StringUtil.isNotEmpty(phoneNumber) && !phoneNumber.equals(accountInfo.getPhoneNumber())) {
                account.setPhoneNumber(DatabaseEncryptUtil.encrypt(accountInfo.getPhoneNumber()));
                needUpdate = true;
            }
            if (StringUtil.isNotEmpty(email) && !email.equals(accountInfo.getEmail())) {
                account.setEmail(accountInfo.getEmail());
                needUpdate = true;
            }
            if (needUpdate) {
                account.setUpdateTime(System.currentTimeMillis());
                accountMongoRepo.save(account);
            }
        }

        SsoLoginApi.Output output = new SsoLoginApi.Output();
        output.setId(accountInfo.getId());
        output.setToken(accountInfo.getId());
        output.setPhoneNumber(accountInfo.getPhoneNumber());
        output.setNickname(accountInfo.getName());
        return output;
    }
}
