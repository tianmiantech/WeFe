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
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.BoardUserSource;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.dto.vo.AccountInputModel;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Zane
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    //    @Autowired
//    private GatewayService gatewayService;
    @Autowired
    private GlobalConfigService globalConfigService;


    /**
     * register a account
     */
    public void register(AccountInputModel input, BoardUserSource userSource) throws StatusCodeWithException {

        // Determine whether the account is registered
        AccountMysqlModel one = accountRepository.findOne("phoneNumber", DatabaseEncryptUtil.encrypt(input.getPhoneNumber()), AccountMysqlModel.class);
        if (one != null) {
            throw new StatusCodeWithException("该手机号已被注册！", StatusCode.DATA_EXISTED);
        }

        // generate salt
        String salt = SecurityUtil.createRandomSalt();

        // sha hash
        String password = Sha1.of(input.getPassword() + salt);

        AccountMysqlModel model = new AccountMysqlModel();
        model.setCreatedBy(CurrentAccountUtil.get().getId());
        model.setPhoneNumber(input.getPhoneNumber());
        model.setNickname(input.getNickname());
        model.setEmail(input.getEmail());
        model.setPassword(password);
        model.setSalt(salt);
        model.setSuperAdminRole(accountRepository.count() < 1);
        model.setAdminRole(model.getSuperAdminRole());
        model.setEnable(true);
        model.setLastActionTime(new Date());

        // Super administrator does not need to review
        if (model.getSuperAdminRole() || userSource == BoardUserSource.online_demo) {
            model.setAuditStatus(AuditStatus.agree);

            if (userSource == BoardUserSource.online_demo) {
                model.setAuditComment("来自在线体验账号申请");
            }
        }
        // Whether others want to review it depends on the configuration.
        else {
            model.setAuditStatus(
                    globalConfigService.getFusionConfig().accountNeedAuditWhenRegister
                            ? AuditStatus.auditing
                            : AuditStatus.agree
            );
        }


        accountRepository.save(model);

        CacheObjects.refreshAccountMap();
    }


    /**
     * query all of account
     */
    public List<AccountMysqlModel> queryAll() {
        return accountRepository.findAll();
    }

}
