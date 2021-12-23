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

package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.api.setting.GlobalSettingUpdateApi;
import com.welab.wefe.serving.service.api.setting.InitializeApi;
import com.welab.wefe.serving.service.database.serving.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.GlobalSettingMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.AccountRepository;
import com.welab.wefe.serving.service.database.serving.repository.GlobalSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hunter.zhao
 */
@Service
public class GlobalSettingService {
    @Autowired
    private GlobalSettingRepository globalSettingRepository;


    @Autowired
    private AccountRepository accountRepository;

    /**
     * Is the system initialized
     */
    private boolean isInitialized() {
        return globalSettingRepository.count() > 0;
    }

    public String findMemberId() {
        GlobalSettingMySqlModel mysqlModel = globalSettingRepository.findAll().get(0);

        if (mysqlModel == null) {
            return "";
        }

        return mysqlModel.getMemberId();
    }


    /**
     * Initialize system
     */
    public void initialize(InitializeApi.Input input) throws StatusCodeWithException {
        if (isInitialized()) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "The system has been initialized and cannot be repeated.");
        }

        AccountMySqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("You do not have permission to initialize the system. Please contact the super administrator (the first person to register) for operation.", StatusCode.INVALID_USER);
        }

        GlobalSettingMySqlModel model = new GlobalSettingMySqlModel();
        model.setCreatedBy(CurrentAccount.id());
        model.setMemberName(input.getMemberName());
        model.setMemberId(input.getMemberId());
        model.setRsaPrivateKey(input.getRsaPrivateKey());
        model.setRsaPublicKey(input.getRsaPublicKey());

        globalSettingRepository.save(model);

        CacheObjects.refreshMemberInfo();
    }


    public void updateMemberInfo(GlobalSettingUpdateApi.Input input) {

        GlobalSettingMySqlModel model = globalSettingRepository.singleton();

        model.setUpdatedBy(CurrentAccount.id());
        model.setMemberName(input.getMemberName());
        model.setMemberId(input.getMemberId());
        model.setRsaPublicKey(input.getRsaPublicKey());

        globalSettingRepository.save(model);

        CacheObjects.refreshMemberInfo();
    }
}
