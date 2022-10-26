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
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Sha1;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.BoardUserSource;
import com.welab.wefe.data.fusion.service.api.account.AuditApi;
import com.welab.wefe.data.fusion.service.api.account.ForgetPasswordApi;
import com.welab.wefe.data.fusion.service.api.account.QueryApi;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.vo.AccountInputModel;
import com.welab.wefe.data.fusion.service.dto.vo.AccountOutputModel;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
     * Paging query account
     */
    public PagingOutput<AccountOutputModel> query(QueryApi.Input input) throws StatusCodeWithException {

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

    /**
     * The administrator reviews the account
     */
    public void audit(AuditApi.Input input) throws StatusCodeWithException {
        AccountMysqlModel auditor = accountRepository.findById(CurrentAccountUtil.get().getId()).orElse(null);
        if (!auditor.getAdminRole()) {
            throw new StatusCodeWithException("您不是管理员，无权执行审核操作！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        AccountMysqlModel account = accountRepository.findById(input.getAccountId()).orElse(null);
//        if (account.getAuditStatus() != AuditStatus.auditing) {
//            throw new StatusCodeWithException("该用户已被审核，请勿重复操作！", StatusCode.PARAMETER_VALUE_INVALID);
//        }

        account.setAuditStatus(input.getAuditStatus());
        account.setAuditComment(CacheObjects.getNickname(CurrentAccountUtil.get().getId()) + "：" + input.getAuditComment());
        account.setUpdatedBy(CurrentAccountUtil.get().getId());
        accountRepository.save(account);
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

        AccountMysqlModel model = accountRepository.findOne("phoneNumber", DatabaseEncryptUtil.encrypt(input.getPhoneNumber()), AccountMysqlModel.class);
        // phone number error
        if (model == null) {
            throw new StatusCodeWithException("手机号错误，该用户不存在。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员。", StatusCode.PERMISSION_DENIED);
        }

        // Regenerate salt
        String salt = SecurityUtil.createRandomSalt();
        model.setSalt(salt);
        model.setPassword(Sha1.of(input.getPassword() + salt));
        accountRepository.save(model);
    }

    private AccountInfo toAccountInfo(AccountMysqlModel model) {
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
}
