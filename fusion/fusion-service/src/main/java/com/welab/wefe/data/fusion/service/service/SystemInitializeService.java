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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.data.fusion.service.api.system.InitializeApi;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.database.repository.GlobalSettingRepository;
import com.welab.wefe.data.fusion.service.dto.entity.globalconfig.MemberInfoModel;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author hunter.zhao
 */
@Service
public class SystemInitializeService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    GlobalSettingRepository globalSettingRepository;

    @Autowired
    GlobalConfigService globalConfigService;

//    private boolean isInitialized() {
//        return globalSettingRepository.count() > 0;
//    }

    /**
     * Is the system initialized
     */
    public boolean isInitialized() {
        return globalConfigService.getMemberInfo() != null;
    }


    /**
     * Initializing the system
     */
    @Transactional(rollbackFor = Exception.class)
    public void initialize(InitializeApi.Input input) throws StatusCodeWithException {

        if (isInitialized()) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "系统已初始化，不能重复操作。");
        }

        AccountMysqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有初始化系统的权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        MemberInfoModel model = new MemberInfoModel();
        model.setMemberId(UUID.randomUUID().toString().replaceAll("-", ""));
        model.setMemberName(input.getMemberName());
        model.setMemberEmail(input.getMemberEmail());
        model.setMemberMobile(input.getMemberMobile());

        try {
            RSAUtil.RsaKeyPair pair = RSAUtil.generateKeyPair();
            model.setRsaPrivateKey(pair.privateKey);
            model.setRsaPublicKey(pair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

//        GlobalSettingMySqlModel model = new GlobalSettingMySqlModel();
//        model.setCreatedBy(CurrentAccount.id());
//        model.setPartnerName(input.getPartnerName());
//        globalSettingRepository.save(model);

        globalConfigService.setMemberInfo(model);

        CacheObjects.refreshMemberInfo();
    }

}
