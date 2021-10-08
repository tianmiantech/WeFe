/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.data.fusion.service.api.system.InitializeApi;
import com.welab.wefe.data.fusion.service.database.entity.GlobalSettingMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.GlobalSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

/**
 * @author hunter.zhao
 */
@Service
public class GlobalSettingService {
    @Autowired
    GlobalSettingRepository globalSettingRepository;

    private boolean isInitialized() {
        return globalSettingRepository.count() > 0;
    }

    /**
     * Initializing the system
     */
    @Transactional(rollbackFor = Exception.class)
    public void initialize(InitializeApi.Input input) throws StatusCodeWithException {
        if (isInitialized()) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "系统已初始化，不能重复操作。");
        }

        GlobalSettingMySqlModel model = new GlobalSettingMySqlModel();
        model.setCreatedBy(CurrentAccount.id());
        model.setPartnerName(input.getPartnerName());

        try {
            RSAUtil.RsaKeyPair pair = RSAUtil.generateKeyPair();
            model.setRsaPrivateKey(pair.privateKey);
            model.setRsaPublicKey(pair.publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        globalSettingRepository.save(model);

        CacheObjects.refreshMemberInfo();
    }

}
