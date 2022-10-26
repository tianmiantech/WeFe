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

package com.welab.wefe.board.service.api.account;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.service.SystemInitializeService;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 模拟登录
 * <p>
 * 由于存在WebSocket接口，而WebSocket中无法从到HttpServletRequest请求头中获取当前登录用户信息，因此需前端调用该接口记录当前登录用户信息，
 * 从把该用户信息缓存并保存到本地缓存中以该在WebSocketServer类中使用（WebSocketServer必须要求前端前入登录用户凭证，后台没法自动获取）
 * </p>
 */
@Api(path = "account/sso_login", name = "sso_login", login = false)
public class SsoLoginApi extends AbstractNoneInputApi<SsoLoginApi.Output> {
    @Autowired
    private AccountService accountService;
    @Autowired
    private SystemInitializeService systemInitializeService;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        if (!systemInitializeService.isInitialized()) {
            return fail("The system has not been initialized.");
        }
        return success(accountService.ssoLogin());
    }

    public static class Output extends AbstractApiOutput {
        private String id;
        private String token;
        private String phoneNumber;
        private String nickname;
        private String email;
        private Boolean superAdminRole;
        private Boolean adminRole;
        private JSONObject uiConfig;

        private String memberId;
        private String memberName;

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

        public JSONObject getUiConfig() {
            return uiConfig;
        }

        public void setUiConfig(JSONObject uiConfig) {
            this.uiConfig = uiConfig;
        }

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
    }
}
