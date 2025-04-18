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

package com.welab.wefe.serving.service.api.system;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.dto.globalconfig.UnionInfoModel;
import com.welab.wefe.serving.service.enums.ServingModeEnum;
import com.welab.wefe.serving.service.service.AccountService;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(
        path = "global_config/initialize/union",
        name = "Initialize system",
        desc = "Initialize the system and set global parameters.",
        login = false
)
public class InitializeUnionModeApi extends AbstractNoneOutputApi<InitializeUnionModeApi.Input> {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        //initialize
        IdentityInfoModel identityInfoModel = input.convertToIdentityInfoModel();
        UnionInfoModel unionInfoModel = input.convertToUnionInfoModel();
        globalConfigService.initializeToUnion(identityInfoModel, unionInfoModel);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "联邦成员id", require = true)
        private String memberId;

        @Check(
                name = "联邦成员名称",
                require = true,
                messageOnEmpty = "请输入成员名称",
                regex = "^[\\u4e00-\\u9fa5（）0-9a-zA-Z ]{3,12}$",
                messageOnInvalid = "成员名称仅支持中文、英文与数字，且长度为 3 - 12。"
        )
        private String memberName;

        @Check(name = "公钥", require = true)
        private String rsaPublicKey;

        @Check(name = "私钥", require = true)
        private String rsaPrivateKey;

        @Check(name = "union地址", require = true)
        private String unionBaseUrl;

        @Check(name = "密钥类型")
        private SecretKeyType secretKeyType = SecretKeyType.rsa;


        public IdentityInfoModel convertToIdentityInfoModel() {
            IdentityInfoModel model = new IdentityInfoModel();
            model.setMemberId(memberId);
            model.setMemberName(memberName);
            model.setRsaPrivateKey(rsaPrivateKey);
            model.setRsaPublicKey(rsaPublicKey);
            model.setSecretKeyType(null != secretKeyType ? secretKeyType : SecretKeyType.rsa);
            model.setMode(ServingModeEnum.union.name());
            return model;
        }

        public UnionInfoModel convertToUnionInfoModel() {
            UnionInfoModel model = new UnionInfoModel();
            model.setIntranetBaseUri(unionBaseUrl);
            return model;
        }

        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getRsaPublicKey() {
            return rsaPublicKey;
        }

        public void setRsaPublicKey(String rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
        }

        public String getRsaPrivateKey() {
            return rsaPrivateKey;
        }

        public void setRsaPrivateKey(String rsaPrivateKey) {
            this.rsaPrivateKey = rsaPrivateKey;
        }

        public String getUnionBaseUrl() {
            return unionBaseUrl;
        }

        public void setUnionBaseUrl(String unionBaseUrl) {
            this.unionBaseUrl = unionBaseUrl;
        }

        public SecretKeyType getSecretKeyType() {
            return secretKeyType;
        }

        public void setSecretKeyType(SecretKeyType secretKeyType) {
            this.secretKeyType = secretKeyType;
        }

        //endregion
    }


}
