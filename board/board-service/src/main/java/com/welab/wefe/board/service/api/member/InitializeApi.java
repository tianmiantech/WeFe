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

package com.welab.wefe.board.service.api.member;

import com.welab.wefe.board.service.service.SystemInitializeService;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.StandardFieldType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "member/initialize", name = "initialize system")
public class InitializeApi extends AbstractNoneOutputApi<InitializeApi.Input> {

    @Autowired
    private SystemInitializeService systemInitializeService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        systemInitializeService.initialize(input);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(
                name = "联邦成员名称",
                require = true,
                messageOnEmpty = "请输入成员名称",
                regex = "^[\\u4e00-\\u9fa5（）0-9a-zA-Z ]{3,12}$",
                messageOnInvalid = "成员名称仅支持中文、英文与数字，且长度为 3 - 12。"
        )
        private String memberName;

        @Check(name = "邮箱", type = StandardFieldType.Email)
        private String memberEmail;

        @Check(
                name = "电话",
                regex = "[0-9\\-\\+]{6,18}",
                messageOnInvalid = "请输入正确的联系电话"
        )
        private String memberMobile;

        @Check(name = "是否允许对外公开数据集基础信息", require = true)
        private Boolean memberAllowPublicDataSet;

        @Check(name = "密钥类型")
        private SecretKeyType secretKeyType = SecretKeyType.rsa;

        //region getter/setter

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getMemberEmail() {
            return memberEmail;
        }

        public void setMemberEmail(String memberEmail) {
            this.memberEmail = memberEmail;
        }

        public String getMemberMobile() {
            return memberMobile;
        }

        public void setMemberMobile(String memberMobile) {
            this.memberMobile = memberMobile;
        }

        public Boolean getMemberAllowPublicDataSet() {
            return memberAllowPublicDataSet;
        }

        public void setMemberAllowPublicDataSet(Boolean memberAllowPublicDataSet) {
            this.memberAllowPublicDataSet = memberAllowPublicDataSet;
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
