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

import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.data.mongodb.repo.AccountMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class PrivacyDatabaseEncryptService {
    @Autowired
    private AccountMongoRepo accountMongoRepo;

    public void encrypt() throws StatusCodeWithException {
        List<Account> list = accountMongoRepo.findAll();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Account account : list) {
            account.setPhoneNumber(DatabaseEncryptUtil.encrypt(account.getPhoneNumber()));
            account.setUpdateTime(System.currentTimeMillis());
            accountMongoRepo.save(account);
        }
    }
}
