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

import com.welab.wefe.common.web.service.account.AccountInfo;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Information about the account currently requesting the API
 *
 * @author Zane
 */
public class CurrentAccount {

    /**
     * token : Info
     */
    private static ExpiringMap<String, AccountInfo> ACCOUNT_MAP_BY_TOKEN = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(60, TimeUnit.MINUTES)
            .build();

    public static void init() {
    }


    /**
     * Token of the current user
     */
    private static ThreadLocal<String> tokens = new ThreadLocal<>();

    public synchronized static void logined(String token, AccountInfo accountInfo) {
        // To avoid multi-point login, locate and delete the old token.
        ACCOUNT_MAP_BY_TOKEN.entrySet().removeIf(item -> accountInfo.getId().equals(item.getValue().getId()));
        ACCOUNT_MAP_BY_TOKEN.put(token, accountInfo);
    }

    /**
     * Log out
     */
    public synchronized static void logout() {
        String token = token();
        if (ACCOUNT_MAP_BY_TOKEN.containsKey(token)) {
            ACCOUNT_MAP_BY_TOKEN.remove(token);
        }
    }

    /**
     * Log out of the specified account
     */
    public static void logout(String id) {
        synchronized (ACCOUNT_MAP_BY_TOKEN) {
            ACCOUNT_MAP_BY_TOKEN.entrySet().removeIf(item -> id.equals(item.getValue().getId()));
        }
    }

    /**
     * Gets the ID of the current user
     */
    public static String id() {
        AccountInfo accountInfo = get();
        if (accountInfo == null) {
            return null;
        }
        return accountInfo.id;
    }

    /**
     * Gets the mobile number of the current user
     */
    public static String phoneNumber() {
        AccountInfo accountInfo = get();
        if (accountInfo == null) {
            return null;
        }
        return accountInfo.phoneNumber;
    }

    /**
     * Administrator or not
     */
    public static boolean isAdmin() {
        AccountInfo accountInfo = get();
        if (accountInfo == null) {
            return false;
        }
        return accountInfo.isAdminRole();
    }

    /**
     * Whether to be a super administrator
     */
    public static boolean isSuperAdmin() {
        AccountInfo accountInfo = get();
        if (accountInfo == null) {
            return false;
        }
        return accountInfo.isSuperAdminRole();
    }

    /**
     * Get the current user information
     */
    public static AccountInfo get() {
        String token = tokens.get();
        if (token == null) {
            return null;
        }

        return ACCOUNT_MAP_BY_TOKEN.get(token);
    }

    /**
     * Obtain the user information of the specified token
     */
    public static AccountInfo get(String token) {
        return ACCOUNT_MAP_BY_TOKEN.get(token);
    }

    /**
     * Saves the token of the current user
     */
    public static void token(String token) {
        tokens.set(token);
    }

    /**
     * Gets the token of the current user
     */
    public static String token() {
        return tokens.get();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
