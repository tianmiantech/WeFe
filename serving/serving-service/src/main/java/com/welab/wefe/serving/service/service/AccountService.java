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


import com.welab.wefe.common.SecurityUtil;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.service.account.SsoAccountInfo;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.serving.service.api.account.SsoLoginApi;
import com.welab.wefe.serving.service.database.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.enums.ServingModeEnum;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * @author Zane
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GlobalConfigService globalConfigService;

    public SsoLoginApi.Output ssoLogin() throws StatusCodeWithException {
        if (!globalConfigService.isInitialized()) {
            IdentityInfoModel identityInfoModel = new IdentityInfoModel();
            identityInfoModel.setMemberId(UUID.randomUUID().toString().replaceAll("-", ""));
            identityInfoModel.setMemberName("serving系统");
            identityInfoModel.setMode(ServingModeEnum.standalone.name());
            try {
                SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(SecretKeyType.rsa);
                identityInfoModel.setRsaPrivateKey(keyPair.privateKey);
                identityInfoModel.setRsaPublicKey(keyPair.publicKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            globalConfigService.initializeToStandalone(identityInfoModel);
        }

        SsoAccountInfo accountInfo = CurrentAccountUtil.get();
        AccountMySqlModel accountMysqlModel = accountRepository.findById(accountInfo.getId()).orElse(null);
        if (null == accountMysqlModel) {
            String salt = SecurityUtil.createRandomSalt();
            String password = Sha1.of(UUID.randomUUID().toString().replace("-", "") + salt);
            accountMysqlModel = new AccountMySqlModel();
            accountMysqlModel.setId(accountInfo.getId());
            accountMysqlModel.setNickname(accountInfo.getName());
            accountMysqlModel.setPhoneNumber(accountInfo.getPhoneNumber());
            accountMysqlModel.setPassword(password);
            accountMysqlModel.setSalt(salt);
            accountMysqlModel.setSuperAdminRole(true);
            accountMysqlModel.setAdminRole(true);
            accountMysqlModel.setAuditStatus(AuditStatus.agree);
            accountMysqlModel.setEnable(true);
            accountMysqlModel.setEmail(accountInfo.getEmail());

            accountRepository.save(accountMysqlModel);
        } else {
            String nickName = accountMysqlModel.getNickname();
            String phoneNumber = accountMysqlModel.getPhoneNumber();
            String email = accountMysqlModel.getEmail();
            boolean needUpdate = false;
            if (StringUtil.isNotEmpty(nickName) && !nickName.equals(accountInfo.getName())) {
                accountMysqlModel.setNickname(accountInfo.getName());
                needUpdate = true;
            }
            if (!needUpdate && StringUtil.isNotEmpty(phoneNumber) && !phoneNumber.equals(accountInfo.getPhoneNumber())) {
                accountMysqlModel.setPhoneNumber(accountInfo.getPhoneNumber());
                needUpdate = true;
            }
            if (!needUpdate && StringUtil.isNotEmpty(email) && !email.equals(accountInfo.getEmail())) {
                accountMysqlModel.setEmail(accountInfo.getEmail());
                needUpdate = true;
            }
            if (needUpdate) {
                accountMysqlModel.setUpdatedTime(new Date());
                accountRepository.save(accountMysqlModel);
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
