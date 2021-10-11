/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.member.InitializeApi;
import com.welab.wefe.board.service.api.member.UpdateMemberInfoApi;
import com.welab.wefe.board.service.api.member.UpdateMemberLogoApi;
import com.welab.wefe.board.service.database.entity.AccountMySqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.database.repository.DataSetRepository;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author zane.luo
 */
@Service
public class SystemInitializeService extends AbstractService {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UnionService unionService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private ServingService servingService;

    @Autowired
    private DataSetRepository dataSetRepository;

    /**
     * Synchronize member information to union for the recovery of membership after union data is lost.
     */
    public synchronized void syncMemberToUnion() throws StatusCodeWithException {

        AccountMySqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有初始化系统的权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        unionService.initializeSystem(globalConfigService.getMemberInfo());

        for (DataSetMysqlModel model : dataSetRepository.findAll()) {
            unionService.uploadDataSet(model);
        }
    }


    /**
     * Is the system initialized
     */
    public boolean isInitialized() {
        return globalConfigService.getMemberInfo() != null;
    }

    /**
     * Initialize the system
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void initialize(InitializeApi.Input input) throws StatusCodeWithException {
        if (isInitialized()) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "系统已初始化，不能重复操作。");
        }

        AccountMySqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有初始化系统的权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        MemberInfoModel model = new MemberInfoModel();
        model.setMemberId(UUID.randomUUID().toString().replaceAll("-", ""));
        model.setMemberName(input.getMemberName());
        model.setMemberEmail(input.getMemberEmail());
        model.setMemberMobile(input.getMemberMobile());
        model.setMemberAllowPublicDataSet(input.getMemberAllowPublicDataSet());
        model.setMemberHidden(false);

        try {
            RSAUtil.RsaKeyPair pair = RSAUtil.generateKeyPair();
            model.setRsaPrivateKey(pair.privateKey);
            model.setRsaPublicKey(pair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        globalConfigService.setMemberInfo(model);

        CacheObjects.refreshMemberInfo();

        unionService.initializeSystem(model);

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMemberInfo(UpdateMemberInfoApi.Input input) throws StatusCodeWithException {

        AccountMySqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有编辑权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        MemberInfoModel model = globalConfigService.getMemberInfo();
        model.setMemberName(input.getMemberName());
        model.setMemberEmail(input.getMemberEmail());
        model.setMemberMobile(input.getMemberMobile());
        model.setMemberAllowPublicDataSet(input.getMemberAllowPublicDataSet());
        model.setMemberGatewayUri(input.getMemberGatewayUri());
        if (StringUtil.isNotEmpty(input.getMemberLogo()) && input.getMemberLogo().contains(",")) {
            LOG.info("压缩前：" + input.getMemberLogo().length());
            String[] strs = input.getMemberLogo().split(",");
            model.setMemberLogo(strs[0] + "," + FileUtil.compressPicForScale(strs[1], 200, 0.7));
            LOG.info("压缩后：" + model.getMemberLogo().length());
        }
        model.setMemberHidden(input.getMemberHidden());

        globalConfigService.setMemberInfo(model);

        unionService.uploadMemberInfoExcludeLogo(model);

        CacheObjects.refreshMemberInfo();
        // Notify the gateway to also refresh the corresponding cache
        gatewayService.refreshSystemConfigCache();
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRsaKey() throws StatusCodeWithException {

        AccountMySqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有编辑权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        MemberInfoModel model = globalConfigService.getMemberInfo();

        try {
            RSAUtil.RsaKeyPair pair = RSAUtil.generateKeyPair();
            model.setRsaPrivateKey(pair.privateKey);
            model.setRsaPublicKey(pair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        // notify union
        unionService.resetPublicKey(model);
        globalConfigService.setMemberInfo(model);

        // Update serving global settings
        servingService.asynRefreshMemberInfo(model);

        CacheObjects.refreshMemberInfo();

        // Notify the gateway to also refresh the corresponding cache
        gatewayService.refreshSystemConfigCache();
    }


    /**
     * Update member logo
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberLogo(UpdateMemberLogoApi.Input input) throws StatusCodeWithException {
        AccountMySqlModel account = accountRepository.findByPhoneNumber(CurrentAccount.phoneNumber());
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有编辑权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        MemberInfoModel model = globalConfigService.getMemberInfo();
        model.setMemberLogo(input.getMemberLogo());
        globalConfigService.setMemberInfo(model);

        unionService.updateMemberLogo(model);
    }

}

