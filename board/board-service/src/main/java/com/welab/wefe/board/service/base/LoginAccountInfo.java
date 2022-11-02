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

package com.welab.wefe.board.service.base;

import com.welab.wefe.common.web.service.account.SsoAccountInfo;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * 登录用户信息
 */
public class LoginAccountInfo {
    private static LoginAccountInfo loginAccountInfo = new LoginAccountInfo();

    /**
     * 当前登录用户信息(Board的WeSocket需要用到)
     * KEY：当前登录TOKEN
     * VALUE：用户信息
     */
    private static ExpiringMap<String, SsoAccountInfo> LOGIN_ACCOUNT_INFO = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(60, TimeUnit.MINUTES).build();

    private LoginAccountInfo() {
    }

    public static LoginAccountInfo getInstance() {
        return loginAccountInfo;
    }

    public void put(String id, SsoAccountInfo accountInfo) {
        LOGIN_ACCOUNT_INFO.put(id, accountInfo);
    }

    public SsoAccountInfo get(String id) {
        return LOGIN_ACCOUNT_INFO.get(id);
    }
}
