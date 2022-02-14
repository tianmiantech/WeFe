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
    private static ExpiringMap<String, Info> ACCOUNT_MAP_BY_TOKEN = ExpiringMap
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

    /**
     * After a user logs in successfully, this method is invoked to register and obtain its information.
     */
    public synchronized static void logined(String token, String id, String phoneNumber, boolean adminRole, boolean superAdminRole) {
        // To avoid multi-point login, locate and delete the old token.
        synchronized (ACCOUNT_MAP_BY_TOKEN) {
            ACCOUNT_MAP_BY_TOKEN.entrySet().removeIf(item -> id.equals(item.getValue().getId()));
        }

        ACCOUNT_MAP_BY_TOKEN.put(token, new Info(id, phoneNumber, adminRole, superAdminRole));
    }

    public synchronized static void logined(String token, String id, String phoneNumber) {
        ACCOUNT_MAP_BY_TOKEN.put(token, new Info(id, phoneNumber));
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
        Info info = get();
        if (info == null) {
            return null;
        }
        return info.id;
    }

    /**
     * Gets the mobile number of the current user
     */
    public static String phoneNumber() {
        Info info = get();
        if (info == null) {
            return null;
        }
        return info.phoneNumber;
    }

    /**
     * Administrator or not
     */
    public static boolean isAdmin() {
        Info info = get();
        if (info == null) {
            return false;
        }
        return info.isAdminRole();
    }

    /**
     * Whether to be a super administrator
     */
    public static boolean isSuperAdmin() {
        Info info = get();
        if (info == null) {
            return false;
        }
        return info.isSuperAdminRole();
    }

    /**
     * Get the current user information
     */
    public static Info get() {
        String token = tokens.get();
        if (token == null) {
            return null;
        }

        return ACCOUNT_MAP_BY_TOKEN.get(token);
    }

    /**
     * Obtain the user information of the specified token
     */
    public static Info get(String token) {
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
        return UUID.randomUUID().toString();
    }

    public static class Info {
        public String id;
        public String phoneNumber;
        public boolean adminRole;
        public boolean superAdminRole;
        public boolean enable;

        public Info(String id, String phoneNumber) {
            this.id = id;
            this.phoneNumber = phoneNumber;
        }

        public Info(String id, String phoneNumber, boolean adminRole, boolean superAdminRole) {
            this.id = id;
            this.phoneNumber = phoneNumber;
            this.adminRole = adminRole;
            this.superAdminRole = superAdminRole;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public boolean isAdminRole() {
            return adminRole;
        }

        public void setAdminRole(boolean adminRole) {
            this.adminRole = adminRole;
        }

        public boolean isSuperAdminRole() {
            return superAdminRole;
        }

        public void setSuperAdminRole(boolean superAdminRole) {
            this.superAdminRole = superAdminRole;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }
}
