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

package com.welab.wefe.serving.service.api.account;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.service.account.AccountInfo2;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.enums.ServingModeEnum;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Api(path = "account/sso_login", name = "sso_login", login = false)
public class SsoLoginApi extends AbstractNoneInputApi<SsoLoginApi.Output> {
    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        if (!globalConfigService.isInitialized()) {
            IdentityInfoModel identityInfoModel = new IdentityInfoModel();
            identityInfoModel.setMemberId(UUID.randomUUID().toString().replaceAll("-", ""));
            identityInfoModel.setMemberName("serving系统");
            identityInfoModel.setMode(ServingModeEnum.standalone.name());
            try {
                SignUtil.KeyPair keyPair = SignUtil.generateKeyPair(SecretKeyType.rsa);
                identityInfoModel.setRsaPrivateKey(keyPair.privateKey);
                identityInfoModel.setRsaPublicKey(keyPair.publicKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            globalConfigService.initializeToStandalone(identityInfoModel);
        }
        AccountInfo2 accountInfo = CurrentAccountUtil.get();
        Output output = new Output();
        output.setId(accountInfo.getId());
        output.setToken(accountInfo.getId());
        output.setPhoneNumber(accountInfo.getPhoneNumber());
        output.setNickname(accountInfo.getName());

        return success(output);
    }

    public static class Output extends AbstractApiOutput {
        private String id;

        private String token;

        private String phoneNumber;

        private String nickname;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
