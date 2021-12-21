/*
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

package com.welab.wefe.serving.service.api.setting;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.GlobalSettingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "global_setting/initialize", name = "Initialize system", desc = "Initialize the system and set global parameters.")
public class InitializeApi extends AbstractNoneOutputApi<InitializeApi.Input> {

    @Autowired
    private GlobalSettingService globalSettingService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        globalSettingService.initialize(input);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "联邦成员id")
        private String memberId;

        @Check(
                name = "联邦成员名称",
                require = true,
                messageOnEmpty = "请输入成员名称",
                regex = "^[\\u4e00-\\u9fa5（）0-9a-zA-Z ]{3,12}$",
                messageOnInvalid = "成员名称仅支持中文、英文与数字，且长度为 3 - 12。"
        )
        private String memberName;

        @Check(name = "公钥")
        private String rsaPublicKey;

        @Check(name = "私钥")
        private String rsaPrivateKey;

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


        //endregion
    }


}
