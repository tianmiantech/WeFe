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

package com.welab.wefe.common.web;

import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * Login Security Policy
 * <p>
 * Six wrong passwords in 10 minutes will shut down the black room for an hour
 *
 * @author zane.luo
 */
public class LoginSecurityPolicy {
    /**
     * Number of login failures by user name
     * <p>
     * Number of login failures (incorrect verification code or password) within 1 minute when the user saves each user name
     */
    private static ExpiringMap<String, Integer> LOGIN_FAIL_COUNT_MAP = ExpiringMap
            .builder()
            .expiration(10, TimeUnit.MINUTES)
            .build();

    /**
     * The little black house
     * <p>
     * Too many failed login attempts to shut down the dark room users
     */
    private static ExpiringMap<String, Integer> A_DARK_ROOM = ExpiringMap
            .builder()
            .expiration(60, TimeUnit.MINUTES)
            .build();

    /**
     * In the dark room
     */
    public static boolean inDarkRoom(String username) {
        return A_DARK_ROOM.containsKey(username);
    }

    /**
     * Record the event of a login failure
     */
    public synchronized static void onLoginFail(String username) {
        if (!LOGIN_FAIL_COUNT_MAP.containsKey(username)) {
            LOGIN_FAIL_COUNT_MAP.put(username, 0);
        }
        Integer count = LOGIN_FAIL_COUNT_MAP.get(username);
        count++;

        // Too many mistakes and you're in the dark room
        if (count > 5) {
            A_DARK_ROOM.put(username, 0);
        }

        LOGIN_FAIL_COUNT_MAP.put(username, count);
    }

    /**
     * Record a successful login event
     */
    public synchronized static void onLoginSuccess(String username) {
        LOGIN_FAIL_COUNT_MAP.put(username, 0);
    }
}
