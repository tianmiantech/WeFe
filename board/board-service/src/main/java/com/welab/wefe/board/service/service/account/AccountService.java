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

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.board.service.api.account.*;
import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.AccountOutputModel;
import com.welab.wefe.board.service.dto.vo.AccountInputModel;
import com.welab.wefe.board.service.dto.vo.OnlineAccountOutput;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.WebSocketServer;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.board.service.service.verificationcode.VerificationCodeService;
import com.welab.wefe.board.service.util.BoardSM4Util;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.service.account.AbstractAccountService;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.BoardUserSource;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.VerificationCodeBusinessType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author Zane
 */
@Service
public class AccountService extends AbstractAccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * Paging query account
     */
    public PagingOutput<AccountOutputModel> query(QueryApi.Input input) throws StatusCodeWithException {

        Specification<AccountMysqlModel> where = Where
                .create()
                .contains("phoneNumber", BoardSM4Util.encryptPhoneNumber(input.getPhoneNumber()))
                .equal("auditStatus", input.getAuditStatus())
                .contains("nickname", input.getNickname())
                .orderBy("createdTime", OrderBy.desc)
                .build(AccountMysqlModel.class);

        return accountRepository.paging(where, input, AccountOutputModel.class);
    }

    /**
     * register a account
     */
    public void register(AccountInputModel input, BoardUserSource userSource) throws StatusCodeWithException {

        // Determine whether the account is registered
        AccountMysqlModel one = accountRepository.findOne("phoneNumber", BoardSM4Util.encryptPhoneNumber(input.getPhoneNumber()), AccountMysqlModel.class);
        if (one != null) {
            throw new StatusCodeWithException("该手机号已被注册！", StatusCode.DATA_EXISTED);
        }

        // generate salt
        String salt = createRandomSalt();

        // sha hash
        String password = Sha1.of(input.getPassword() + salt);

        AccountMysqlModel model = new AccountMysqlModel();
        model.setCreatedBy(CurrentAccount.id());
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
                    globalConfigService.getBoardConfig().accountNeedAuditWhenRegister
                            ? AuditStatus.auditing
                            : AuditStatus.agree
            );
        }


        accountRepository.save(model);

        CacheObjects.refreshAccountMap();
    }

    @Override
    public void saveSelfPassword(String password, String salt, JSONArray historyPasswords) throws StatusCodeWithException {
        AccountMysqlModel model = accountRepository.findById(CurrentAccount.id()).orElse(null);
        model.setPassword(password);
        model.setSalt(salt);
        model.setHistoryPasswordList(historyPasswords);
        accountRepository.save(model);
    }

    /**
     * query all of account
     */
    public List<AccountMysqlModel> queryAll() {
        return accountRepository.findAll();
    }

    /**
     * The administrator reviews the account
     */
    public void audit(AuditApi.Input input) throws StatusCodeWithException {
        AccountMysqlModel auditor = accountRepository.findById(CurrentAccount.id()).orElse(null);
        if (!auditor.getAdminRole()) {
            throw new StatusCodeWithException("您不是管理员，无权执行审核操作！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        AccountMysqlModel account = accountRepository.findById(input.getAccountId()).orElse(null);
        if (account.getAuditStatus() != AuditStatus.auditing) {
            throw new StatusCodeWithException("该用户已被审核，请勿重复操作！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        account.setAuditStatus(input.getAuditStatus());
        account.setAuditComment(CacheObjects.getNickname(CurrentAccount.id()) + "：" + input.getAuditComment());
        account.setUpdatedBy(CurrentAccount.id());
        accountRepository.save(account);

    }

    @Override
    public AccountInfo getAccountInfo(String phoneNumber) throws StatusCodeWithException {
        AccountMysqlModel model = accountRepository.findByPhoneNumber(BoardSM4Util.encryptPhoneNumber(phoneNumber));
        return toAccountInfo(model);
    }

    private AccountInfo toAccountInfo(AccountMysqlModel model) throws StatusCodeWithException {
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


    @Override
    public AccountInfo getSuperAdmin() throws StatusCodeWithException {
        List<AccountMysqlModel> list = accountRepository.findAll(Where
                .create()
                .equal("superAdminRole", true)
                .build(AccountMysqlModel.class)
        );

        if (list.isEmpty()) {
            return null;
        }

        return toAccountInfo(list.get(0));
    }

    /**
     * Update user basic information
     */
    public void update(UpdateApi.Input input) throws StatusCodeWithException {
        /**
         * 这里分为两种情况
         * 1. 用户修改自己的资料
         * 2. 超级管理员把普通用户设置为管理员，或将管理员设置为普通用户。这种情况理论上不应该存在，但是由于档期问题暂时共用一个接口，等前端有时间之后再单独开一个接口。
         */
        if (CurrentAccount.id().equals(input.id)) {
            updateBaseInfo(input);
        } else {
            updateAdminRole(input);
        }
    }

    private void updateAdminRole(UpdateApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法进行此操作。", StatusCode.PERMISSION_DENIED);
        }

        if (input.getAdminRole() == null) {
            return;
        }

        AccountMysqlModel account = accountRepository.findById(input.id).orElse(null);

        if (account == null) {
            throw new StatusCodeWithException("找不到更新的用户信息。", StatusCode.DATA_NOT_FOUND);
        }

        account.setAdminRole(input.getAdminRole());
        account.setUpdatedBy(CurrentAccount.id());
        account.setUpdatedTime(new Date());

        accountRepository.save(account);
    }

    private void updateBaseInfo(UpdateApi.Input input) throws StatusCodeWithException {
        AccountMysqlModel account = accountRepository.findById(CurrentAccount.id()).orElse(null);

        if (StringUtil.isNotEmpty(input.getNickname())) {
            account.setNickname(input.getNickname());
        }

        if (StringUtil.isNotEmpty(input.getEmail())) {
            account.setEmail(input.getEmail());
        }

        account.setUpdatedBy(CurrentAccount.id());
        account.setUpdatedTime(new Date());

        accountRepository.save(account);
    }

    /**
     * Update the user's enable status
     */
    public void enable(EnableApi.Input input) throws StatusCodeWithException {

        if (!CurrentAccount.isAdmin() && !CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("普通用户无法进行此操作。", StatusCode.PERMISSION_DENIED);
        }

        if (input.getId().equals(CurrentAccount.id())) {
            throw new StatusCodeWithException("无法对自己进行此操作。", StatusCode.PERMISSION_DENIED);
        }

        AccountMysqlModel account = accountRepository.findById(input.getId()).orElse(null);
        if (account == null) {
            throw new StatusCodeWithException("找不到更新的用户信息。", StatusCode.DATA_NOT_FOUND);
        }

        if (account.getAdminRole() && !CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("非超级管理员无法进行此操作。", StatusCode.PERMISSION_DENIED);
        }

        account.setEnable(input.getEnable());
        account.setUpdatedBy(CurrentAccount.id());
        account.setUpdatedTime(new Date());
        account.setAuditComment(input.getEnable()
                ? CacheObjects.getNickname(CurrentAccount.id()) + "启用了该账号"
                : CacheObjects.getNickname(CurrentAccount.id()) + "禁用了该账号");

        accountRepository.save(account);

        CurrentAccount.logout(input.getId());
    }

    /**
     * Reset user password (administrator rights)
     */
    public String resetPassword(ResetPasswordApi.Input input) throws StatusCodeWithException {
        // 操作者
        AccountMysqlModel operator = accountRepository.findById(CurrentAccount.id()).orElse(null);
        if (!super.verifyPassword(operator.getPassword(), input.getOperatorPassword(), operator.getSalt())) {
            throw new StatusCodeWithException("密码错误，身份核实失败，已退出登录。", StatusCode.PERMISSION_DENIED);
        }

        // 被重置密码的账号
        AccountMysqlModel model = accountRepository.findById(input.getId()).orElse(null);

        if (model == null) {
            throw new StatusCodeWithException("找不到更新的用户信息。", StatusCode.DATA_NOT_FOUND);
        }

        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("非管理员无法重置密码。", StatusCode.PERMISSION_DENIED);
        }

        if (model.getSuperAdminRole()) {
            throw new StatusCodeWithException("不能重置超级管理员密码。", StatusCode.PERMISSION_DENIED);
        }

        if (model.getAdminRole() && !CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException("只有超级管理员才能重置管理员的密码", StatusCode.PERMISSION_DENIED);
        }

        String salt = createRandomSalt();
        String newPassword = RandomStringUtils.randomAlphanumeric(2) + new Random().nextInt(999999);

        String websitePassword = model.getPhoneNumber() + newPassword + model.getPhoneNumber() + model.getPhoneNumber().substring(0, 3) + newPassword.substring(newPassword.length() - 3);

        model.setSalt(salt);
        model.setPassword(Sha1.of(Md5.of(websitePassword) + salt));
        model.setUpdatedBy(CurrentAccount.id());
        model.setUpdatedTime(new Date());
        accountRepository.save(model);

        CurrentAccount.logout(model.getId());

        return newPassword;
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
                    QueryApi.class,
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
            throw new StatusCodeWithException("系统异常: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * Check whether the user with the specified mobile phone number exists
     */
    public boolean exist(String phoneNumber) throws StatusCodeWithException {
        AccountMysqlModel model = accountRepository.findOne("phoneNumber", BoardSM4Util.encryptPhoneNumber(phoneNumber), AccountMysqlModel.class);
        return model != null;
    }


    /**
     * Transfer the super administrator status to another account
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeSuperAdmin(AccountMysqlModel account) throws StatusCodeWithException {
        account.setAdminRole(true);
        account.setSuperAdminRole(true);
        account.setUpdatedBy(CurrentAccount.id());
        account.setUpdatedTime(new Date());

        // Update designated user as super administrator
        accountRepository.save(account);
        // Cancel the super administrator privileges of the current account
        accountRepository.cancelSuperAdmin(CurrentAccount.id());

        CurrentAccount.logout(account.getId());
        CurrentAccount.logout(CurrentAccount.id());

    }

    public void forgetPassword(ForgetPasswordApi.Input input) throws StatusCodeWithException {
        if (StringUtil.isEmpty(input.getPhoneNumber())) {
            throw new StatusCodeWithException("手机号不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(input.getPassword())) {
            throw new StatusCodeWithException("密码不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(input.getSmsVerificationCode())) {
            throw new StatusCodeWithException("验证码不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        AccountMysqlModel model = accountRepository.findOne("phoneNumber", BoardSM4Util.encryptPhoneNumber(input.getPhoneNumber()), AccountMysqlModel.class);
        // phone number error
        if (model == null) {
            throw new StatusCodeWithException("手机号错误，该用户不存在。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员。", StatusCode.PERMISSION_DENIED);
        }

        // Check verification code is valid?
        verificationCodeService.checkVerificationCode(input.getPhoneNumber(), input.getSmsVerificationCode(), VerificationCodeBusinessType.accountForgetPassword);

        // Regenerate salt
        String salt = createRandomSalt();
        model.setSalt(salt);
        model.setPassword(Sha1.of(input.getPassword() + salt));
        accountRepository.save(model);
    }
}
