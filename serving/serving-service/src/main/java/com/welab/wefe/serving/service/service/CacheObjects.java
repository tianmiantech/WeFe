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

package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.service.database.serving.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.AccountRepository;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Global cache
 * It stores unchanged or little changed data in the system, so as to reduce the complexity of database query and coding.
 * <p>
 * Cache list：
 * - Member information
 *
 * @author Zane
 */
public class CacheObjects {

    private static String ID;
    //    private static String MEMBER_ID;
    private static String RSA_PRIVATE_KEY;
    private static String RSA_PUBLIC_KEY;
    private static String BASE_URL;
    //    private static String MEMBER_NAME;
    private static String NAME;

    /**
     * accountId : nickname
     */
    private static LinkedHashMap<String, String> ACCOUNT_MAP = new LinkedHashMap<>();

    public static String getMemberId() {
        if (ID == null) {
            refreshIdentityInfo();
        }
        return ID;
    }

    public static String getRsaPrivateKey() {
        if (RSA_PRIVATE_KEY == null) {
            refreshIdentityInfo();
        }
        return RSA_PRIVATE_KEY;
    }

    public static String getRsaPublicKey() {
        if (RSA_PUBLIC_KEY == null) {
            refreshIdentityInfo();
        }
        return RSA_PUBLIC_KEY;
    }

    public static String getBaseUrl() {
        if (BASE_URL == null) {
            refreshIdentityInfo();
        }
        return BASE_URL;
    }

    public static String getName() {
        if (NAME == null) {
            refreshIdentityInfo();
        }
        return NAME;
    }


    /**
     * Reload member information
     */
    public static void refreshIdentityInfo() {
        GlobalConfigService service = Launcher.getBean(GlobalConfigService.class);
        IdentityInfoModel model = service.getIdentityInfo();

        if (model == null) {
            return;
        }

        ID = model.getId();
        RSA_PUBLIC_KEY = model.getRsaPublicKey();
        RSA_PRIVATE_KEY = model.getRsaPrivateKey();
        NAME = model.getName();
    }


    /**
     * Reload account list
     */
    public static void refreshAccountMap() {
        AccountRepository repo = Launcher.CONTEXT.getBean(AccountRepository.class);
        List<AccountMySqlModel> list = repo.findAll(Sort.by("nickname"));

        ACCOUNT_MAP.clear();

        for (AccountMySqlModel item : list) {
            ACCOUNT_MAP.put(item.getId(), item.getNickname());
        }
    }

    public static LinkedHashMap<String, String> getAccountMap() {
        if (ACCOUNT_MAP.isEmpty()) {
            refreshAccountMap();
        }
        return ACCOUNT_MAP;
    }

    /**
     * Get the account's nickname
     */
    public static String getNickname(String accountId) {
        if (accountId == null) {
            return null;
        }
        return getAccountMap().get(accountId) == null ? "未知" : getAccountMap().get(accountId);
    }
}
