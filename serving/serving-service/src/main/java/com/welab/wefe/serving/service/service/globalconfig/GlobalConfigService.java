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

package com.welab.wefe.serving.service.service.globalconfig;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.api.system.GlobalConfigUpdateApi;
import com.welab.wefe.serving.service.api.system.UpdateRsaKeyApi;
import com.welab.wefe.serving.service.database.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.dto.globalconfig.UnionInfoModel;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.utils.ServingSM4Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Zane
 */
@Service
public class GlobalConfigService extends BaseGlobalConfigService {

    @Autowired
    AccountRepository accountRepository;

    /**
     * Is the system initialized
     */
    public boolean isInitialized() {
        return getIdentityInfo() != null;
    }

    /**
     * check initialized
     */
    private void checkInitialized() throws StatusCodeWithException {
        if (isInitialized()) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "The system has been initialized and cannot be repeated.");
        }
    }

    /**
     * Initialize system
     */
    public void initializeToStandalone(IdentityInfoModel model) throws StatusCodeWithException {

        checkInitialized();

        setIdentityInfo(model);

        CacheObjects.refreshIdentityInfo();
    }

    /**
     * Initialize system by union
     */
    public void initializeToUnion(IdentityInfoModel identityInfoModel, UnionInfoModel unionInfoModel) throws StatusCodeWithException {

        checkInitialized();

        setIdentityInfo(identityInfoModel);

        setUnionInfo(unionInfoModel);

        CacheObjects.refreshIdentityInfo();
    }

    public void update(GlobalConfigUpdateApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.ILLEGAL_REQUEST.throwException("只有管理员才能执行此操作。");
        }

        for (Map.Entry<String, Map<String, String>> group : input.groups.entrySet()) {
            String groupName = group.getKey();
            Map<String, String> groupItems = group.getValue();
            for (Map.Entry<String, String> item : groupItems.entrySet()) {
                if (item.getKey().equals("id")) {
                    continue;
                }
                if (item.getKey().equalsIgnoreCase("rsa_public_key")) {
                    continue;
                }
                if (item.getKey().equalsIgnoreCase("rsa_private_key")) {
                    continue;
                }
                String key = item.getKey();
                String value = item.getValue();
                put(groupName, key, value, null);
            }
        }
    }


    public void updateRsaKeyByBoard(UpdateRsaKeyApi.Input input) throws StatusCodeWithException {
        IdentityInfoModel model = new IdentityInfoModel();
        model.setRsaPrivateKey(input.getRsaPrivateKey());
        model.setRsaPublicKey(input.getRsaPublicKey());

        setIdentityInfo(model);
    }




    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRsaKey() throws StatusCodeWithException {

        AccountMySqlModel account = accountRepository.findByPhoneNumber(ServingSM4Util.encryptPhoneNumber(CurrentAccount.phoneNumber()));
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException("您没有编辑权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
        }

        IdentityInfoModel model = getIdentityInfo();

        try {
            SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(SecretKeyType.rsa);
            model.setRsaPrivateKey(keyPair.privateKey);
            model.setRsaPublicKey(keyPair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        // notify union
        setIdentityInfo(model);

        CacheObjects.refreshIdentityInfo();
    }


    public IdentityInfoModel getIdentityInfo() {
        return getModel(Group.IDENTITY_INFO, IdentityInfoModel.class);
    }

    public void setIdentityInfo(IdentityInfoModel model) throws StatusCodeWithException {
        put(Group.IDENTITY_INFO, model);
    }

    public UnionInfoModel getUnionInfoModel() {
        return getModel(Group.WEFE_UNION, UnionInfoModel.class);
    }


    public void setUnionInfo(UnionInfoModel model) throws StatusCodeWithException {
        put(Group.WEFE_UNION, model);
    }
}
