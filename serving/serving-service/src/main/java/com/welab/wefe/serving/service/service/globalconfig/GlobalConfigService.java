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
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import com.welab.wefe.mpc.pir.server.PrivateInformationRetrievalServer;
import com.welab.wefe.serving.service.api.system.GlobalConfigUpdateApi;
import com.welab.wefe.serving.service.api.system.UpdateRsaKeyByBoardApi;
import com.welab.wefe.serving.service.database.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.dto.globalconfig.ServiceCacheConfigModel;
import com.welab.wefe.serving.service.dto.globalconfig.UnionInfoModel;
import com.welab.wefe.serving.service.dto.globalconfig.base.AbstractConfigModel;
import com.welab.wefe.serving.service.enums.ServingModeEnum;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.utils.RedisIntermediateCache;
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
        return getModel(IdentityInfoModel.class) != null;
    }

    /**
     * check initialized
     */
    private void checkInitialized() throws StatusCodeWithException {
        if (isInitialized()) {
            throw StatusCodeWithException.of(StatusCode.UNSUPPORTED_HANDLE, "系统已初始化，无法重复操作。");
        }
    }

    /**
     * Initialize system
     */
    public void initializeToStandalone(IdentityInfoModel model) throws StatusCodeWithException {

        checkInitialized();

        put(model);

        CacheObjects.refreshGlobalConfig();
    }

    /**
     * Initialize system by union
     */
    public void initializeToUnion(IdentityInfoModel identityInfoModel, UnionInfoModel unionInfoModel) throws StatusCodeWithException {

//        checkInitialized();

        put(identityInfoModel);

        put(unionInfoModel);

        CacheObjects.refreshGlobalConfig();
    }
//
//    public void update(GlobalConfigUpdateApi.Input input) throws StatusCodeWithException {
//        if (!CurrentAccount.isAdmin()) {
//            StatusCode.ILLEGAL_REQUEST.throwException("只有管理员才能执行此操作。");
//        }
//
//        for (Map.Entry<String, Map<String, String>> group : input.groups.entrySet()) {
//            String groupName = group.getKey();
//            Map<String, String> groupItems = group.getValue();
//            for (Map.Entry<String, String> item : groupItems.entrySet()) {
//                if (item.getKey().equals("id")) {
//                    continue;
//                }
//                if (item.getKey().equalsIgnoreCase("rsa_public_key")) {
//                    continue;
//                }
//                if (item.getKey().equalsIgnoreCase("rsa_private_key")) {
//                    continue;
//                }
//                String key = item.getKey();
//                String value = item.getValue();
//                put(groupName, key, value, null);
//            }
//        }
//
//        CacheObjects.refreshGlobalConfig();
//    }


    public void update(GlobalConfigUpdateApi.Input input) throws Exception {
        for (Map.Entry<String, Map<String, String>> group : input.groups.entrySet()) {
            AbstractConfigModel model = toModel(group.getKey(), group.getValue());
            put(model);
        }
        ServiceCacheConfigModel cacheConfigModel = getModel(ServiceCacheConfigModel.class);
        if (cacheConfigModel == null) {
            return;
        }
        // update PrivateInformationRetrievalServer
        if (ServiceCacheConfigModel.CacheType.redis.equals(cacheConfigModel.getType())) {
            PrivateInformationRetrievalServer.set(100,
                    new RedisIntermediateCache(cacheConfigModel.getRedisHost(),
                            Integer.valueOf(cacheConfigModel.getRedisPort()),
                            cacheConfigModel.getRedisPassword()));
        } else {
            PrivateInformationRetrievalServer.set(100, null);
        }
    }


    public void updateRsaKeyByBoard(UpdateRsaKeyByBoardApi.Input input) throws StatusCodeWithException {
        IdentityInfoModel model = getModel(IdentityInfoModel.class);
        if (ServingModeEnum.standalone.name().equals(model.getMode())) {
            StatusCode.ILLEGAL_REQUEST.throwException("当前Serving系统为独立模式，无法将board密钥同步！");
        }

        model.setRsaPrivateKey(input.getRsaPrivateKey());
        model.setRsaPublicKey(input.getRsaPublicKey());
        put(model);
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRsaKey() throws StatusCodeWithException {

        AccountMySqlModel account = accountRepository.findByPhoneNumber(DatabaseEncryptUtil.encrypt(CurrentAccountUtil.get().getPhoneNumber()));
        if (!account.getSuperAdminRole()) {
            throw new StatusCodeWithException(StatusCode.INVALID_USER, "您没有编辑权限，请联系超级管理员（第一个注册的人）进行操作。");
        }

        IdentityInfoModel model = getModel(IdentityInfoModel.class);

        try {
            SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(SecretKeyType.rsa);
            model.setRsaPrivateKey(keyPair.privateKey);
            model.setRsaPublicKey(keyPair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        model.setMode(ServingModeEnum.standalone.name());

        // notify union
        put(model);

        CacheObjects.refreshGlobalConfig();
    }

}
