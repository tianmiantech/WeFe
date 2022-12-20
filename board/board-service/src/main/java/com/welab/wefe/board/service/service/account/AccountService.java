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

package com.welab.wefe.board.service.service.account;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.board.service.api.account.ListAllApi;
import com.welab.wefe.board.service.api.account.QueryMemberAccountsApi;
import com.welab.wefe.board.service.api.account.QueryOnlineApi;
import com.welab.wefe.board.service.api.account.SsoLoginApi;
import com.welab.wefe.board.service.base.LoginAccountInfo;
import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.AccountListAllOutputModel;
import com.welab.wefe.board.service.dto.entity.AccountOutputModel;
import com.welab.wefe.board.service.dto.vo.OnlineAccountOutput;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.WebSocketServer;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.SecurityUtil;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.service.account.SsoAccountInfo;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Zane
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private GlobalConfigService globalConfigService;


    public List<AccountListAllOutputModel> listAll(ListAllApi.Input input) {

        Specification<AccountMysqlModel> where = Where
                .create()
                .contains("nickname", input.getNickname())
                .build(AccountMysqlModel.class);

        List<AccountMysqlModel> list = accountRepository.findAll(where);
        return ModelMapper.maps(list, AccountListAllOutputModel.class);
    }

    /**
     * Paging query account
     */
    public PagingOutput<AccountOutputModel> query(QueryMemberAccountsApi.Input input) throws StatusCodeWithException {

        Specification<AccountMysqlModel> where = Where
                .create()
                .contains("phoneNumber", DatabaseEncryptUtil.encrypt(input.getPhoneNumber()))
                .equal("auditStatus", input.getAuditStatus())
                .contains("nickname", input.getNickname())
                .orderBy("createdTime", OrderBy.desc)
                .build(AccountMysqlModel.class);

        return accountRepository.paging(where, input, AccountOutputModel.class);
    }

    /**
     * query all of account
     */
    public List<AccountMysqlModel> queryAll() {
        return accountRepository.findAll();
    }

    /**
     * Query user information by member ID
     */
    public PagingOutput<AccountOutputModel> queryMemberAccounts(QueryMemberAccountsApi.Input input) throws StatusCodeWithException {
        PagingOutput<AccountOutputModel> pagingOutput = new PagingOutput<>();
        if (CacheObjects.getMemberId().equals(input.getMemberId())) {
            pagingOutput = query(input);
        } else {
            pagingOutput = gatewayService.callOtherMemberBoard(
                    input.getMemberId(),
                    JobMemberRole.promoter,
                    QueryMemberAccountsApi.class,
                    input,
                    pagingOutput.getClass()
            );
        }

        List<AccountOutputModel> accountOutputModelList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(pagingOutput.getList())) {
            for (Object obj : pagingOutput.getList()) {
                AccountOutputModel accountOutputModel = JObject.parseObject(JObject.create(obj).toString(), AccountOutputModel.class);
                // Remove sensitive information
                accountOutputModel.setEmail(null);
                accountOutputModel.setPhoneNumber(null);
                accountOutputModelList.add(accountOutputModel);
            }
            pagingOutput.setList(accountOutputModelList);
        }

        return pagingOutput;
    }

    /**
     * Query the online account of the exchange center
     */
    public List<OnlineAccountOutput> queryOnlineAccount(QueryOnlineApi.Input input) throws StatusCodeWithException {
        List<OnlineAccountOutput> resultList = new ArrayList<>();
        // Don't need to go through the gateway to query the online accounts of your own members
        if (CacheObjects.getMemberId().equals(input.getMemberId()) || input.fromGateway()) {
            WebSocketServer.webSocketMap.forEach((k, v) -> {
                OnlineAccountOutput onlineAccountOutput = new OnlineAccountOutput();
                if (StringUtil.isEmpty(input.getAccountId())) {
                    onlineAccountOutput.setAccountId(k);
                    resultList.add(onlineAccountOutput);
                } else if (k.equals(input.getAccountId())) {
                    onlineAccountOutput.setAccountId(k);
                    resultList.add(onlineAccountOutput);
                }

            });
            return resultList;
        }
        try {
            JObject data = JObject.create().append("memberId", input.getMemberId())
                    .append("accountId", input.getAccountId());

            QueryOnlineApi.Output output = gatewayService.callOtherMemberBoard(
                    input.getMemberId(),
                    JobMemberRole.promoter,
                    QueryOnlineApi.class,
                    data,
                    QueryOnlineApi.Output.class
            );

            return output.getList();
        } catch (Exception e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "系统异常: " + e.getMessage());
        }
    }

    /**
     * Check whether the user with the specified mobile phone number exists
     */
    public boolean exist(String phoneNumber) throws StatusCodeWithException {
        AccountMysqlModel model = accountRepository.findOne("phoneNumber", DatabaseEncryptUtil.encrypt(phoneNumber), AccountMysqlModel.class);
        return model != null;
    }


    public SsoLoginApi.Output ssoLogin() throws StatusCodeWithException {
        SsoAccountInfo accountInfo = CurrentAccountUtil.get();
        AccountMysqlModel accountMysqlModel = accountRepository.findById(accountInfo.getId()).orElse(null);

        if (null == accountMysqlModel) {
            String salt = SecurityUtil.createRandomSalt();
            String password = Sha1.of(UUID.randomUUID().toString().replace("-", "") + salt);
            accountMysqlModel = new AccountMysqlModel();
            accountMysqlModel.setId(accountInfo.getId());
            accountMysqlModel.setNickname(accountInfo.getName());
            accountMysqlModel.setPhoneNumber(accountInfo.getPhoneNumber());
            accountMysqlModel.setPassword(password);
            accountMysqlModel.setSalt(salt);
            accountMysqlModel.setSuperAdminRole(true);
            accountMysqlModel.setAdminRole(true);
            accountMysqlModel.setAuditStatus(AuditStatus.agree);
            accountMysqlModel.setEnable(true);
            accountMysqlModel.setLastActionTime(new Date());
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
            if (StringUtil.isNotEmpty(phoneNumber) && !phoneNumber.equals(accountInfo.getPhoneNumber())) {
                accountMysqlModel.setPhoneNumber(accountInfo.getPhoneNumber());
                needUpdate = true;
            }
            if (StringUtil.isNotEmpty(email) && !email.equals(accountInfo.getEmail())) {
                accountMysqlModel.setEmail(accountInfo.getEmail());
                needUpdate = true;
            }
            if (needUpdate) {
                accountMysqlModel.setUpdatedTime(new Date());
                accountRepository.save(accountMysqlModel);
            }
        }
        CacheObjects.putAccount(accountMysqlModel);

        LoginAccountInfo.getInstance().put(accountMysqlModel.getId(), accountInfo);
        return accountToSsoLoginOutput(accountMysqlModel);
    }

    private SsoLoginApi.Output accountToSsoLoginOutput(AccountMysqlModel accountMysqlModel) throws StatusCodeWithException {
        SsoLoginApi.Output output = new SsoLoginApi.Output();
        output.setId(accountMysqlModel.getId());
        output.setToken(accountMysqlModel.getId());
        output.setPhoneNumber(accountMysqlModel.getPhoneNumber());
        output.setNickname(accountMysqlModel.getNickname());
        output.setSuperAdminRole(accountMysqlModel.getSuperAdminRole());
        output.setAdminRole(accountMysqlModel.getAdminRole());
        output.setUiConfig(accountMysqlModel.getUiConfig());
        output.setMemberId(CacheObjects.getMemberId());
        output.setMemberName(CacheObjects.getMemberName());
        return output;
    }

    public void updateUiConfig(Map<String, Object> config) {
        accountRepository.updateUiConfig(CurrentAccountUtil.get().getId(), JSON.toJSONString(config));
    }
}
