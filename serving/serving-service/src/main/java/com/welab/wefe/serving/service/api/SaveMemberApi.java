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

package com.welab.wefe.serving.service.api;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(
        path = "member_save",
        name = "保存成员信息",
        login = false,
        rsaVerify = true,
        domain = Caller.Board
)
public class SaveMemberApi extends AbstractNoneOutputApi<SaveMemberApi.Input> {

    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<?> handler(Input input) {
        memberService.save(input.getMemberId(), input.getName(), input.getBaseUrl(), input.getPublicKey());

        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "身份id")
        private String memberId;
        @Check(require = true, name = "成员名称")
        private String name;
        @Check(require = true, name = "调用路径")
        private String baseUrl;
        @Check(require = true, name = "公钥")
        private String publicKey;


        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        //endregion
    }
}
