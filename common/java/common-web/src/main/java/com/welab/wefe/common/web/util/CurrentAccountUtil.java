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

package com.welab.wefe.common.web.util;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.service.account.AccountInfo2;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前登录用户工具类
 */
public class CurrentAccountUtil {
    /**
     * IAM请求头：当前登录用户ID
     */
    private final static String IAM_HEADER_KEY_USER_ID = "x-jwt-user-id";
    /**
     * IAM请求头：当前登录用户名称
     */
    private final static String IAM_HEADER_KEY_USER_NAME = "x-jwt-user-name";
    /**
     * IAM请求头：当前登录用户手机号
     */
    private final static String IAM_HEADER_KEY_PHONE_NUMBER = "x-jwt-phone-number";
    /**
     * IAM请求头：当前登录用户邮箱
     */
    private final static String IAM_HEADER_KEY_EMAIL = "x-jwt-email";

    /**
     * 默认用户ID
     */
    private static final String DEFAULT_ACCOUNT_ID = "ac1173fef3bc4d8493f660a66e7f004a";
    /**
     * 默认用户名称
     */
    private static final String DEFAULT_ACCOUNT_NAME = "WeFe隐式计算";
    /**
     * 默认用户手机号
     */
    private static final String DEFAULT_ACCOUNT_PHONE_NUMBER = "18888888888";
    /**
     * 默认邮箱
     */
    private static final String DEFAULT_ACCOUNT_EMAIL = "12346@163.com";

    /**
     * 当前登录用户
     */
    private static final ThreadLocal<AccountInfo2> CURRENT_ACCOUNT_INFO = new InheritableThreadLocal();


    public static void set(HttpServletRequest httpServletRequest) {
        String userId = httpServletRequest.getHeader(IAM_HEADER_KEY_USER_ID);
        String userName = httpServletRequest.getHeader(IAM_HEADER_KEY_USER_NAME);
        String phoneNumber = httpServletRequest.getHeader(IAM_HEADER_KEY_PHONE_NUMBER);
        String email = httpServletRequest.getHeader(IAM_HEADER_KEY_EMAIL);
        userId = StringUtil.isEmpty(userId) ? DEFAULT_ACCOUNT_ID : userId;
        userName = StringUtil.isEmpty(userName) ? DEFAULT_ACCOUNT_NAME : userName;
        phoneNumber = StringUtil.isEmpty(phoneNumber) ? DEFAULT_ACCOUNT_PHONE_NUMBER : phoneNumber;
        email = StringUtil.isEmpty(email) ? DEFAULT_ACCOUNT_EMAIL : email;

        AccountInfo2 accountInfo = buildAccountInfo(userId, userName, phoneNumber, email);
        CURRENT_ACCOUNT_INFO.set(accountInfo);
    }

    public static AccountInfo2 get() {
        AccountInfo2 accountInfo = CURRENT_ACCOUNT_INFO.get();
        return null == accountInfo ? buildAccountInfo(DEFAULT_ACCOUNT_ID, DEFAULT_ACCOUNT_NAME, DEFAULT_ACCOUNT_PHONE_NUMBER, DEFAULT_ACCOUNT_EMAIL) : accountInfo;
    }

    public static void remove() {
        CURRENT_ACCOUNT_INFO.remove();
    }

    /**
     * 创建用户
     */
    private static AccountInfo2 buildAccountInfo(String id, String name, String phoneNumber, String email) {
        AccountInfo2 accountInfo = new AccountInfo2();
        accountInfo.setId(id);
        accountInfo.setName(name);
        accountInfo.setPhoneNumber(phoneNumber);
        accountInfo.setEmail(email);
        return accountInfo;
    }
}
