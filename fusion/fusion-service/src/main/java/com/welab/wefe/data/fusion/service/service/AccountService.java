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
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.LoginSecurityPolicy;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.BoardUserSource;
import com.welab.wefe.data.fusion.service.api.account.*;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.vo.AccountInputModel;
import com.welab.wefe.data.fusion.service.dto.vo.AccountOutputModel;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author Zane
 */
@Service
public class AccountService extends AbstractService {

    @Autowired
    private AccountRepository accountRepository;

    //    @Autowired
//    private GatewayService gatewayService;
    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * Paging query account
     */
    public PagingOutput<AccountOutputModel> query(QueryApi.Input input) throws StatusCodeWithException {

        Specification<AccountMysqlModel> where = Where
                .create()
                .contains("phoneNumber", input.getPhoneNumber())
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
        AccountMysqlModel one = accountRepository.findOne("phoneNumber", input.getPhoneNumber(), AccountMysqlModel.class);
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
     * login
     */
    public LoginApi.Output login(String phoneNumber, String password, String key, String code) throws StatusCodeWithException {

        // Check if it's in the small black room
        if (LoginSecurityPolicy.inDarkRoom(phoneNumber)) {
            throw new StatusCodeWithException("账号已被禁止登陆，请一个小时后再试，或联系管理员。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        AccountMysqlModel model = accountRepository.findOne("phoneNumber", phoneNumber, AccountMysqlModel.class);
        // phone number error
        if (model == null) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("手机号或密码错误，连续错误 6 次会被禁止登陆，可以联系管理员重置密码找回账号。");
        }

        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员。", StatusCode.PERMISSION_DENIED);
        }

        // wrong password
        if (!model.getPassword().equals(Sha1.of(password + model.getSalt()))) {

            // Log a login failure event
            LoginSecurityPolicy.onLoginFail(phoneNumber);
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("手机号或密码错误，连续错误 6 次会被禁止登陆，可以联系管理员重置密码找回账号。");
        }

        // Check audit status
        if (model.getAuditStatus() != null) {
            switch (model.getAuditStatus()) {
                case auditing:
                    AccountMysqlModel superAdmin = findSuperAdmin();

                    throw new StatusCodeWithException("账号尚未审核，请联系管理员 " + superAdmin.getNickname() + " （或其他任意管理员）对您的账号进行审核后再尝试登录！", StatusCode.PARAMETER_VALUE_INVALID);
                case disagree:
                    throw new StatusCodeWithException("账号审核不通过：" + model.getAuditComment(), StatusCode.PARAMETER_VALUE_INVALID);
                default:
            }
        }

        String token = UUID.randomUUID().toString();
        CurrentAccount.logined(token, model.getId(), model.getPhoneNumber(), model.getAdminRole(), model.getSuperAdminRole());

        LoginApi.Output output = ModelMapper.map(model, LoginApi.Output.class);
        output.setToken(token);

        // Record a successful login event
        LoginSecurityPolicy.onLoginSuccess(phoneNumber);

        return output;
    }

    /**
     * update password
     */
    public void updatePassword(String oldPassword, String newPassword) throws StatusCodeWithException {

        String phoneNumber = CurrentAccount.phoneNumber();
        if (phoneNumber == null) {
            throw new StatusCodeWithException(StatusCode.LOGIN_REQUIRED);
        }

        AccountMysqlModel model = accountRepository.findByPhoneNumber(phoneNumber);

        // Check old password
        if (!StringUtil.equals(model.getPassword(), Sha1.of(oldPassword + model.getSalt()))) {
            throw new StatusCodeWithException("您输入的旧密码不正确", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Regenerate salt
        String salt = createRandomSalt();

        // sha hash
        newPassword = Sha1.of(newPassword + salt);

        model.setSalt(salt);
        model.setPassword(newPassword);

        accountRepository.save(model);

        CurrentAccount.logout(model.getId());
    }


    /**
     * query all of account
     */
    public List<AccountMysqlModel> queryAll() {
        return accountRepository.findAll();
    }

    private String createRandomSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[16];
        r.nextBytes(salt);

        return Base64Util.encode(salt);
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

    /**
     * Query super administrator
     */
    public AccountMysqlModel findSuperAdmin() {
        List<AccountMysqlModel> list = accountRepository.findAll(Where
                .create()
                .equal("superAdminRole", true)
                .build(AccountMysqlModel.class)
        );

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    /**
     * Update user basic information
     */
    public void update(UpdateApi.Input input) throws StatusCodeWithException {

        AccountMysqlModel account = accountRepository.findById(CurrentAccount.id()).orElse(null);

        if (account == null) {
            throw new StatusCodeWithException("找不到更新的用户信息。", StatusCode.DATA_NOT_FOUND);
        }

        if (StringUtil.isNotEmpty(input.getNickname())) {
            account.setNickname(input.getNickname());
        }

        if (StringUtil.isNotEmpty(input.getEmail())) {
            account.setEmail(input.getEmail());
        }

        // Set someone else to be an administrator
        if (input.getAdminRole() != null) {
            if (!CurrentAccount.isSuperAdmin()) {
                throw new StatusCodeWithException("非超级管理员无法进行此操作。", StatusCode.PERMISSION_DENIED);
            }
            account.setAdminRole(input.getAdminRole());
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
//
//
//
//    /**
//     * Query the online account of the exchange center
//     */
//    public List<OnlineAccountOutput> queryOnlineAccount(QueryOnlineApi.Input input) throws StatusCodeWithException {
//        List<OnlineAccountOutput> resultList = new ArrayList<>();
//        // Don't need to go through the gateway to query the online accounts of your own members
//        if (CacheObjects.getMemberId().equals(input.getMemberId()) || input.fromGateway()) {
//            WebSocketServer.webSocketMap.forEach((k, v) -> {
//                OnlineAccountOutput onlineAccountOutput = new OnlineAccountOutput();
//                if (StringUtil.isEmpty(input.getAccountId())) {
//                    onlineAccountOutput.setAccountId(k);
//                    resultList.add(onlineAccountOutput);
//                } else if (k.equals(input.getAccountId())) {
//                    onlineAccountOutput.setAccountId(k);
//                    resultList.add(onlineAccountOutput);
//                }
//
//            });
//            return resultList;
//        }
//        try {
//            JObject data = JObject.create().append("memberId", input.getMemberId())
//                    .append("accountId", input.getAccountId());
//
//            QueryOnlineApi.Output output = gatewayService.callOtherMemberBoard(
//                    input.getMemberId(),
//                    JobMemberRole.promoter,
//                    QueryOnlineApi.class,
//                    data,
//                    QueryOnlineApi.Output.class
//            );
//
//            return output.getList();
//        } catch (Exception e) {
//            throw new StatusCodeWithException("系统异常: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
//        }
//    }
//
//    /**
//     * Check whether the user with the specified mobile phone number exists
//     */
//    public boolean exist(String phoneNumber) {
//        AccountMysqlModel model = accountRepository.findOne("phoneNumber", phoneNumber, AccountMysqlModel.class);
//        return model != null;
//    }
//
//
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
    }

    public void forgetPassword(ForgetPasswordApi.Input input) throws StatusCodeWithException {
        if (StringUtil.isEmpty(input.getPhoneNumber())) {
            throw new StatusCodeWithException("手机号不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(input.getPassword())) {
            throw new StatusCodeWithException("密码不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (StringUtil.isEmpty(input.getSmsVerificationCode())) {
            throw new StatusCodeWithException("短信验证码不能为空。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        AccountMysqlModel model = accountRepository.findOne("phoneNumber", input.getPhoneNumber(), AccountMysqlModel.class);
        // phone number error
        if (model == null) {
            throw new StatusCodeWithException("手机号错误，该用户不存在。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员。", StatusCode.PERMISSION_DENIED);
        }

        // Regenerate salt
        String salt = createRandomSalt();
        model.setSalt(salt);
        model.setPassword(Sha1.of(input.getPassword() + salt));
        accountRepository.save(model);
    }
}