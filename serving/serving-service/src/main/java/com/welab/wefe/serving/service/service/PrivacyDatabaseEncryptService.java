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

package com.welab.wefe.serving.service.service;


import com.welab.wefe.serving.service.database.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.database.repository.DataSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Service
public class PrivacyDatabaseEncryptService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Transactional(rollbackFor = Exception.class)
    public void encrypt() {
        encryptAccountMySqlModel();
        encryptDataSourceMysqlModel();
    }

    private void encryptAccountMySqlModel() {
        List<AccountMySqlModel> accountMysqlModelList = accountRepository.findAll();
        if (!CollectionUtils.isEmpty(accountMysqlModelList)) {
            for (AccountMySqlModel model : accountMysqlModelList) {
                model.setUpdatedTime(new Date());
                accountRepository.save(model);
            }
        }
    }

    private void encryptDataSourceMysqlModel() {
        List<DataSourceMySqlModel> dataSourceMysqlModelList = dataSourceRepository.findAll();
        if (!CollectionUtils.isEmpty(dataSourceMysqlModelList)) {
            for (DataSourceMySqlModel model : dataSourceMysqlModelList) {
                model.setUpdatedTime(new Date());
                dataSourceRepository.save(model);
            }
        }
    }
}
