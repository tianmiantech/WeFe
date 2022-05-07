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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.api.setting.GlobalConfigUpdateApi;
import com.welab.wefe.serving.service.database.serving.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.dto.globalconfig.UnionInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Zane
 */
@Service
public class GlobalConfigService extends BaseGlobalConfigService {

    @Autowired
    private AccountRepository accountRepository;


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
                String key = item.getKey();
                String value = item.getValue();
                put(groupName, key, value, null);
            }
        }
    }

//    @Transactional(rollbackFor = Exception.class)
//    public void updateMemberRsaKey() throws StatusCodeWithException {
//
//        AccountMysqlModel account = accountRepository.findByPhoneNumber(FusionSM4Util.encryptPhoneNumber(CurrentAccount.phoneNumber()));
//        if (!account.getSuperAdminRole()) {
//            throw new StatusCodeWithException("您没有编辑权限，请联系超级管理员（第一个注册的人）进行操作。", StatusCode.INVALID_USER);
//        }
//
//        MemberInfoModel model = getMemberInfo();
//
//        try {
//            SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(SecretKeyType.rsa);
//            model.setRsaPrivateKey(keyPair.privateKey);
//            model.setRsaPublicKey(keyPair.publicKey);
//        } catch (NoSuchAlgorithmException e) {
//            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
//        }
//
//        // notify union
//        setMemberInfo(model);
//
//        CacheObjects.refreshMemberInfo();
//    }


    public IdentityInfoModel getIdentityInfo() {
        return getModel(Group.IDENTITY_INFO, IdentityInfoModel.class);
    }

    public UnionInfoModel getUnionInfoModel() {
        return getModel(Group.WEFE_UNION, UnionInfoModel.class);
    }


//
//    public void setAlertConfig(AlertConfigModel model) throws StatusCodeWithException {
//        put(Group.ALERT_CONFIG, model);
//    }
//
//    public AlertConfigModel getAlertConfig() {
//        return getModel(Group.ALERT_CONFIG, AlertConfigModel.class);
//    }


}
