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

package com.welab.wefe.board.service.api.account;

import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.StandardFieldType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "account/update", name = "update account info")
public class UpdateApi extends AbstractApi<UpdateApi.Input, NoneApiOutput> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<NoneApiOutput> handle(UpdateApi.Input input) throws StatusCodeWithException {
        accountService.update(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "用户Id")
        public String id;

        @Check(name = "昵称")
        private String nickname;

        @Check(name = "邮箱", type = StandardFieldType.Email)
        private String email;

        @Check(name = "管理员权限")
        private Boolean adminRole;

        //region getter/setter

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getAdminRole() {
            return adminRole;
        }

        public void setAdminRole(Boolean adminRole) {
            this.adminRole = adminRole;
        }

        //endregion
    }
}
