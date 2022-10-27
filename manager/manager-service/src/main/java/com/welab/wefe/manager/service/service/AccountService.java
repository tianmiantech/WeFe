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

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.data.mongodb.repo.AccountMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.manager.service.dto.account.QueryAccountInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", readOnly = true,rollbackFor = Exception.class)
public class AccountService {

    @Autowired
    private AccountMongoRepo accountMongoRepo;

    public PageOutput<Account> findList(QueryAccountInput input) throws StatusCodeWithException {
        PageOutput<Account> accountPageOutput = accountMongoRepo.findList(
                DatabaseEncryptUtil.encrypt(input.getPhoneNumber()),
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
        account.setPhoneNumber(DatabaseEncryptUtil.encrypt(account.getPhoneNumber()));
        return account;
    }

    private Account decryptPhoneNumber(Account account) throws StatusCodeWithException {
        if(null == account) {
            return null;
        }
        account.setPhoneNumber(DatabaseEncryptUtil.decrypt(account.getPhoneNumber()));
        return account;
    }
}
