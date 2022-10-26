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


import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.account.ListAllApi;
import com.welab.wefe.serving.service.api.account.QueryAllApi.Output;
import com.welab.wefe.serving.service.api.account.QueryApi;
import com.welab.wefe.serving.service.database.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.AccountListAllOutputModel;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.verificationcode.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Service
public class AccountService  {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VerificationCodeService verificationCodeService;

    public List<AccountListAllOutputModel> listAll(ListAllApi.Input input) {

        Specification<AccountMySqlModel> where = Where
                .create()
                .contains("nickname", input.getNickname())
                .build(AccountMySqlModel.class);

        List<AccountMySqlModel> list = accountRepository.findAll(where);
        return ModelMapper.maps(list, AccountListAllOutputModel.class);
    }


    private AccountInfo toAccountInfo(AccountMySqlModel model) {
        if (model == null) {
            return null;
        }

        AccountInfo info = new AccountInfo();
        info.setId(model.getId());
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
        info.setHistoryPasswordList(model.getHistoryPasswordList());
        return info;
    }

    public List<Output> queryAll() {
        List<AccountMySqlModel> accounts = accountRepository.findAll();
        return accounts.stream().map(x -> com.welab.wefe.common.web.util.ModelMapper.map(x, Output.class))
                .collect(Collectors.toList());
    }

    public List<Output> query() {
        List<AccountMySqlModel> accounts = accountRepository.findAll();
        return accounts.stream().map(x -> com.welab.wefe.common.web.util.ModelMapper.map(x, Output.class))
                .collect(Collectors.toList());
    }

    /**
     * Paging query account
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) throws StatusCodeWithException {
        Specification<AccountMySqlModel> where = Where.create().contains("phoneNumber", DatabaseEncryptUtil.encrypt(input.getPhoneNumber()))
                .equal("auditStatus", input.getAuditStatus()).contains("nickname", input.getNickname())
                .orderBy("createdTime", OrderBy.desc).build(AccountMySqlModel.class);

        return accountRepository.paging(where, input, QueryApi.Output.class);
    }
}
