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
import com.welab.wefe.serving.service.database.entity.*;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.database.repository.PartnerRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.database.repository.TableServiceRepository;
import com.welab.wefe.serving.service.dto.globalconfig.IdentityInfoModel;
import com.welab.wefe.serving.service.dto.globalconfig.UnionInfoModel;
import com.welab.wefe.serving.service.enums.ServingModeEnum;
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

    private static String MEMBER_ID; // 系统ID
    private static String RSA_PRIVATE_KEY; // 私钥
    private static String RSA_PUBLIC_KEY; // 公钥
    private static String SERVING_BASE_URL; // Serving服务地址
    private static String UNION_BASE_URL; // Union服务地址
    //    private static String MEMBER_NAME;
    private static String MEMBER_NAME; // 系统名称
    private static String MODE; // 运行模式 standalone-独立模式 union-联邦模式

    /**
     * accountId : nickname
     */
    private static LinkedHashMap<String, String> ACCOUNT_MAP = new LinkedHashMap<>();


    /**
     * partnerId : partnerName
     */
    private static LinkedHashMap<String, String> PARTNER_MAP = new LinkedHashMap<>();

    /**
     * serviceId : serviceName
     */
    private static LinkedHashMap<String, String> SERVICE_MAP = new LinkedHashMap<>();


    public static String getMemberId() {
        if (MEMBER_ID == null) {
            refreshGlobalConfig();
        }
        return MEMBER_ID;
    }

    public static String getRsaPrivateKey() {
        if (RSA_PRIVATE_KEY == null) {
            refreshGlobalConfig();
        }
        return RSA_PRIVATE_KEY;
    }

    public static String getRsaPublicKey() {
        if (RSA_PUBLIC_KEY == null) {
            refreshGlobalConfig();
        }
        return RSA_PUBLIC_KEY;
    }

    public static String getServingBaseUrl() {
        if (SERVING_BASE_URL == null) {
            refreshGlobalConfig();
        }
        return SERVING_BASE_URL;
    }

    public static String getUnionBaseUrl() {
        if (UNION_BASE_URL == null) {
            refreshGlobalConfig();
        }
        return UNION_BASE_URL;
    }

    public static String getMemberName() {
        if (MEMBER_NAME == null) {
            refreshGlobalConfig();
        }
        return MEMBER_NAME;
    }

    public static String getMODE() {
        if (MODE == null) {
            refreshGlobalConfig();
        }
        return MODE;
    }

    public static boolean isUnionModel() {
        return ServingModeEnum.union.name().equalsIgnoreCase(getMODE());
    }

    /**
     * Reload member information
     */
    public static void refreshGlobalConfig() {
        GlobalConfigService service = Launcher.getBean(GlobalConfigService.class);
        IdentityInfoModel identityModel = service.getIdentityInfo();
        UnionInfoModel unionModel = service.getUnionInfoModel();
        if (identityModel != null) {
            MEMBER_ID = identityModel.getMemberId();
            RSA_PUBLIC_KEY = identityModel.getRsaPublicKey();
            RSA_PRIVATE_KEY = identityModel.getRsaPrivateKey();
            MEMBER_NAME = identityModel.getMemberName();
            MODE = identityModel.getMode();
            SERVING_BASE_URL = identityModel.getServingBaseUrl();
        }
        if (unionModel != null) {
            UNION_BASE_URL = unionModel.getIntranetBaseUri();
        }
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


    /**
     * Reload account list
     */
    public static void refreshPartnerMap() {
        PartnerRepository repo = Launcher.CONTEXT.getBean(PartnerRepository.class);
        List<PartnerMysqlModel> list = repo.findAll(Sort.by("name"));

        PARTNER_MAP.clear();

        for (PartnerMysqlModel item : list) {
            PARTNER_MAP.put(item.getId(), item.getName());
        }
    }

    public static LinkedHashMap<String, String> getPartnerMap() {
        if (PARTNER_MAP.isEmpty()) {
            refreshPartnerMap();
        }
        return PARTNER_MAP;
    }

    /**
     * Get the account's nickname
     */
    public static String getPartnerName(String partnerId) {
        if (partnerId == null) {
            return null;
        }
        return getPartnerMap().get(partnerId) == null ? "未知" : getPartnerMap().get(partnerId);
    }


    /**
     * Reload account list
     */
    public static void refreshServiceMap() {
        TableModelRepository repo = Launcher.CONTEXT.getBean(TableModelRepository.class);
        List<TableModelMySqlModel> modelServicelist = repo.findAll(Sort.by("name"));

        TableServiceRepository tableServiceRepository = Launcher.CONTEXT.getBean(TableServiceRepository.class);
        List<TableServiceMySqlModel> serviceList = tableServiceRepository.findAll(Sort.by("name"));

        SERVICE_MAP.clear();

        for (BaseServiceMySqlModel item : modelServicelist) {
            SERVICE_MAP.put(item.getServiceId(), item.getName());
        }
        for (BaseServiceMySqlModel item : serviceList) {
            SERVICE_MAP.put(item.getServiceId(), item.getName());
        }
    }

    public static LinkedHashMap<String, String> getServiceMap() {
        if (SERVICE_MAP.isEmpty()) {
            refreshServiceMap();
        }
        return SERVICE_MAP;
    }

    /**
     * Get the account's nickname
     */
    public static String getServiceName(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        return getServiceMap().get(serviceId) == null ? "未知" : getServiceMap().get(serviceId);
    }
}
