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

import com.welab.wefe.common.SecurityUtil;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.service.account.AccountInfo2;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.data.fusion.service.api.account.SsoLoginApi;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (null == globalConfigService.getMemberInfo()) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "系统尚未初始化.");
        }
        AccountInfo2 accountInfo = CurrentAccountUtil.get();
        AccountMysqlModel accountMysqlModel = accountRepository.findById(accountInfo.getId()).orElse(null);
        if (null == accountMysqlModel) {
            // generate salt
            String salt = SecurityUtil.createRandomSalt();
            // sha hash
            String password = Sha1.of(UUID.randomUUID().toString().replace("-", "") + salt);

            AccountMysqlModel model = new AccountMysqlModel();
            model.setCreatedBy(CurrentAccountUtil.get().getId());
            model.setPhoneNumber(accountInfo.getPhoneNumber());
            model.setNickname(accountInfo.getName());
            model.setEmail(accountInfo.getEmail());
            model.setPassword(password);
            model.setSalt(salt);
            model.setSuperAdminRole(accountRepository.count() < 1);
            model.setAdminRole(model.getSuperAdminRole());
            model.setEnable(true);
            model.setAuditStatus(AuditStatus.agree);
            model.setLastActionTime(new Date());
            accountRepository.save(model);
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
