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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.member.InitializeApi;
import com.welab.wefe.board.service.api.member.UpdateMemberInfoApi;
import com.welab.wefe.board.service.api.member.UpdateMemberLogoApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.database.repository.data_resource.ImageDataSetRepository;
import com.welab.wefe.board.service.database.repository.data_resource.TableDataSetRepository;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.common.wefe.dto.global_config.MemberInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

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
    private ServingService servingService;

    @Autowired
    private TableDataSetRepository tableDataSetRepository;
    @Autowired
    private ImageDataSetRepository imageDataSetRepository;
    @Autowired
    private BloomFilterRepository bloomFilterRepository;

    @Autowired
    private Config config;

    /**
     * Synchronize member information to union for the recovery of membership after union data is lost.
     */
    public synchronized void syncMemberToUnion() throws StatusCodeWithException {

        AccountMysqlModel account = accountRepository.findByPhoneNumber(DatabaseEncryptUtil.encrypt(CurrentAccountUtil.get().getPhoneNumber()));
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException(StatusCode.INVALID_USER, "您没有初始化系统的权限，请联系超级管理员（第一个注册的人）进行操作。");
        }

        unionService.initializeSystem(globalConfigService.getModel(MemberInfoModel.class));

        for (TableDataSetMysqlModel model : tableDataSetRepository.findAll()) {
            unionService.upsertDataResource(model);
        }
        for (ImageDataSetMysqlModel model : imageDataSetRepository.findAll()) {
            unionService.upsertDataResource(model);
        }
        for (BloomFilterMysqlModel model : bloomFilterRepository.findAll()) {
            unionService.upsertDataResource(model);
        }
    }


    /**
     * Synchronize member information to union for the recovery of membership after union data is lost.
     */
    public synchronized void syncMemberToServing() throws StatusCodeWithException {
        servingService.refreshMemberInfo(globalConfigService.getModel(MemberInfoModel.class), config.getUnionBaseUrl());
    }

    /**
     * Is the system initialized
     */
    public boolean isInitialized() {
        MemberInfoModel member = globalConfigService.getModel(MemberInfoModel.class);
        return member.memberInitialized;
    }

    /**
     * Initialize the system
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void initialize(InitializeApi.Input input) throws StatusCodeWithException {
        if (isInitialized()) {
            throw StatusCodeWithException.of(StatusCode.UNSUPPORTED_HANDLE, "系统已初始化，不能重复操作。");
        }

        MemberInfoModel model = globalConfigService.getModel(MemberInfoModel.class);
        model.setMemberName(input.getMemberName());
        model.setMemberEmail(input.getMemberEmail());
        model.setMemberMobile(input.getMemberMobile());
        model.setMemberAllowPublicDataSet(input.getMemberAllowPublicDataSet());
        model.setMemberHidden(false);
        model.setMemberInitialized(true);

        try {
            input.setSecretKeyType(null == input.getSecretKeyType() ? SecretKeyType.rsa : input.getSecretKeyType());
            SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(input.getSecretKeyType());
            model.setRsaPrivateKey(keyPair.privateKey);
            model.setRsaPublicKey(keyPair.publicKey);
            model.setSecretKeyType(input.getSecretKeyType());
        } catch (NoSuchAlgorithmException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        globalConfigService.put(model);

        CacheObjects.refreshMemberInfo();

        unionService.initializeSystem(model);

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMemberInfo(UpdateMemberInfoApi.Input input) throws StatusCodeWithException {

        MemberInfoModel model = globalConfigService.getModel(MemberInfoModel.class);
        model.setMemberName(input.getMemberName());
        model.setMemberEmail(input.getMemberEmail());
        model.setMemberMobile(input.getMemberMobile());
        model.setMemberAllowPublicDataSet(input.getMemberAllowPublicDataSet());
        model.setMemberGatewayUri(input.getMemberGatewayUri());
        model.setMemberHidden(input.getMemberHidden());
        model.setMemberGatewayTlsEnable(input.getMemberGatewayTlsEnable());
        globalConfigService.put(model);

        unionService.uploadMemberInfoExcludeLogo(model);

        CacheObjects.refreshMemberInfo();
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRsaKey() throws StatusCodeWithException {

        MemberInfoModel model = globalConfigService.getModel(MemberInfoModel.class);

        try {
            SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(model.getSecretKeyType());
            model.setRsaPrivateKey(keyPair.privateKey);
            model.setRsaPublicKey(keyPair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        // notify union
        unionService.resetPublicKey(model);
        globalConfigService.put(model);

        // Update serving global settings
//        servingService.asynRefreshMemberInfo(model);

        CacheObjects.refreshMemberInfo();

        // Notify the gateway to also refresh the corresponding cache
        gatewayService.refreshSystemConfigCache();
    }


    /**
     * Update member logo
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberLogo(UpdateMemberLogoApi.Input input) throws StatusCodeWithException {

        MemberInfoModel model = globalConfigService.getModel(MemberInfoModel.class);
        model.setMemberLogo(input.getMemberLogo());
        globalConfigService.put(model);

        unionService.updateMemberLogo(model);
    }

}

