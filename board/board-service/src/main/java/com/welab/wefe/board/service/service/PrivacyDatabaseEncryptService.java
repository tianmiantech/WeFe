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

package com.welab.wefe.board.service.service;


import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.database.entity.GlobalConfigMysqlModel;
import com.welab.wefe.board.service.database.entity.VerificationCodeMysqlModel;
import com.welab.wefe.board.service.database.listener.GlobalConfigMysqlModelListener;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.database.repository.DataSourceRepository;
import com.welab.wefe.board.service.database.repository.GlobalConfigRepository;
import com.welab.wefe.board.service.database.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class PrivacyDatabaseEncryptService {
    @Autowired
    private Config config;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    protected GlobalConfigRepository globalConfigRepository;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Transactional(rollbackFor = Exception.class)
    public void encrypt() {
        // Account privacy data encrypt
        encryptAccountMysqlModel();
        // Verification privacy data encrypt
        encryptVerificationCodeMysqlModel();
        // GlobalConfig privacy data encrypt
        encryptGlobalConfigMysqlModel();
        // DataSource privacy data encrypt
        encryptDataSourceMysqlModel();
    }

    private void encryptAccountMysqlModel() {
        List<AccountMysqlModel> accountMysqlModelList = accountRepository.findAll();
        if (!CollectionUtils.isEmpty(accountMysqlModelList)) {
            for (AccountMysqlModel model : accountMysqlModelList) {
                model.setUpdatedTime(new Date());
                accountRepository.save(model);
            }
        }
    }

    private void encryptVerificationCodeMysqlModel() {
        List<VerificationCodeMysqlModel> verificationCodeMysqlModelList = verificationCodeRepository.findAll();
        if (!CollectionUtils.isEmpty(verificationCodeMysqlModelList)) {
            for (VerificationCodeMysqlModel model : verificationCodeMysqlModelList) {
                model.setUpdatedTime(new Date());
                verificationCodeRepository.save(model);
            }
        }
    }

    private void encryptGlobalConfigMysqlModel() {
        List<GlobalConfigMysqlModel> totalGlobalConfigMysqlModelList = new ArrayList<>();
        Set<String> globalConfigGroupSet = GlobalConfigMysqlModelListener.DB_PROTECTED_FIELD_MAP.keySet();
        for (String group : globalConfigGroupSet) {
            List<GlobalConfigMysqlModel> globalConfigMysqlModelList = globalConfigRepository.findByGroup(group);
            totalGlobalConfigMysqlModelList.addAll(CollectionUtils.isEmpty(globalConfigMysqlModelList) ? new ArrayList<>() : globalConfigMysqlModelList);
        }
        for (GlobalConfigMysqlModel model : totalGlobalConfigMysqlModelList) {
            model.setUpdatedTime(new Date());
            globalConfigRepository.save(model);
        }
    }

    private void encryptDataSourceMysqlModel() {
        List<DataSourceMysqlModel> dataSourceMysqlModelList = dataSourceRepository.findAll();
        if (!CollectionUtils.isEmpty(dataSourceMysqlModelList)) {
            for (DataSourceMysqlModel model : dataSourceMysqlModelList) {
                model.setUpdatedTime(new Date());
                dataSourceRepository.save(model);
            }
        }
    }
}
