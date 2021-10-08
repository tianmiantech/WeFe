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

package com.welab.wefe.serving.service.api.account;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.repository.GlobalSettingRepository;
import com.welab.wefe.serving.service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "account/login", name = "login", login = false)
public class LoginApi extends AbstractApi<LoginApi.Input, LoginApi.Output> {

    @Autowired
    private AccountService accountService;

    @Autowired
    private GlobalSettingRepository globalSettingRepository;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        Output output = accountService.login(input.phoneNumber, input.password, input.getKey(), input.getCode());

        /**
         * After successful login, check whether the system has been initialized
         *
         * An exception is thrown when it is not initialized. When the front end obtains the exception, it will jump to the initialization interface.
         */
        if (globalSettingRepository.count() < 1) {
            StatusCode status = StatusCode.SYSTEM_NOT_BEEN_INITIALIZED;
            return fail(status.getCode(), status.getMessage(), output);
        }

        return success(output);
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true)
        private String phoneNumber;

        @Check(require = true)
        private String password;


        @Check(require = true, desc = "验证码标识")
        private String key;

        @Check(require = true, desc = "验证码")
        private String code;


        //region getter/setter

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

//endregion
    }

    public static class Output extends AbstractApiOutput {

        private String token;

        private String phoneNumber;

        private String nickname;

        private String email;

        private Boolean superAdminRole;

        private Boolean adminRole;


        //region getter/setter

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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getSuperAdminRole() {
            return superAdminRole;
        }

        public void setSuperAdminRole(Boolean superAdminRole) {
            this.superAdminRole = superAdminRole;
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
