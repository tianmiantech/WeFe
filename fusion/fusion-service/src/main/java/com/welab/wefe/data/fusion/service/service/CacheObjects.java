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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.dto.entity.globalconfig.FusionConfigModel;
import com.welab.wefe.data.fusion.service.dto.entity.globalconfig.MemberInfoModel;
import com.welab.wefe.data.fusion.service.service.bloomfilter.BloomFilterService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Global cache
 * It stores invariable or less variable data in the system to reduce database query and coding complexity.
 * <p>
 * Cache list：
 * - Member information
 *
 * @author Hunter
 */
public class CacheObjects {

    private static String MEMBER_ID;
    private static String RSA_PRIVATE_KEY;
    private static String RSA_PUBLIC_KEY;
    private static String MEMBER_NAME;
    private static Integer OPEN_SOCKET_PORT;

    /**
     * partnerId : partner name
     */
    private static LinkedHashMap<String, String> PARTNER_MAP = new LinkedHashMap<>();

    /**
     * bfId : bf name
     */
    private static LinkedHashMap<String, String> BLOOM_FILTER_MAP = new LinkedHashMap<>();


    /**
     * dataSetId : data set name
     */
    private static LinkedHashMap<String, String> DATA_SET_MAP = new LinkedHashMap<>();

    /**
     * accountId : nickname
     */
    private static final LinkedHashMap<String, String> ACCOUNT_MAP = new LinkedHashMap<>();

    /**
     * accountIds
     */
    private static final List<String> ACCOUNT_ID_LIST = new ArrayList<>();


    public static Integer getOpenSocketPort() {
        if (OPEN_SOCKET_PORT == null) {
            refreshFusionConfig();
        }
        return OPEN_SOCKET_PORT;
    }


    /**
     * @return
     * @throws StatusCodeWithException
     */
    public static LinkedHashMap<String, String> getPartnerMap() throws StatusCodeWithException {
        if (PARTNER_MAP.isEmpty()) {
            refreshPartnerMap();
        }
        return PARTNER_MAP;
    }

    public static String getPartnerName(String partnerId) throws StatusCodeWithException {
        String partnerName = getPartnerMap().get(partnerId);
        if (partnerName == null) {
            CacheObjects.refreshPartnerMap();
            partnerName = getPartnerMap().get(partnerId);
        }

        return partnerName;
    }

    public static LinkedHashMap<String, String> getBloomFilterMap() throws StatusCodeWithException {
        if (BLOOM_FILTER_MAP.isEmpty()) {
            refreshBloomFilterMap();
        }
        return BLOOM_FILTER_MAP;
    }

    public static String getBloomFilterName(String bloomFilterId) throws StatusCodeWithException {
        String bloomFilterName = getBloomFilterMap().get(bloomFilterId);
        if (bloomFilterName == null) {
            CacheObjects.refreshPartnerMap();
            bloomFilterName = getPartnerMap().get(bloomFilterId);
        }

        return bloomFilterName;
    }


    public static LinkedHashMap<String, String> getDataSetMap() throws StatusCodeWithException {
        if (DATA_SET_MAP.isEmpty()) {
            refreshBloomFilterMap();
        }
        return DATA_SET_MAP;
    }

    public static String getDataSetName(String dataSetId) throws StatusCodeWithException {
        String dataSetName = getDataSetMap().get(dataSetId);
        if (dataSetName == null) {
            CacheObjects.refreshDataSetMap();
            dataSetName = getDataSetMap().get(dataSetId);
        }

        return dataSetName;
    }


    /**
     * Reloads the Bloom filter information
     */
    public static void refreshBloomFilterMap() throws StatusCodeWithException {
        BloomFilterService service = Launcher.CONTEXT.getBean(BloomFilterService.class);
        BLOOM_FILTER_MAP.clear();

        List<BloomFilterMySqlModel> bloomFilterMySqlModels = service.list();
        bloomFilterMySqlModels
                .forEach(x -> BLOOM_FILTER_MAP.put(x.getId(), x.getName()));
    }

    /**
     * Reload the federated membership list
     */
    public static void refreshDataSetMap() throws StatusCodeWithException {
        DataSetService service = Launcher.CONTEXT.getBean(DataSetService.class);
        DATA_SET_MAP.clear();

        List<DataSetMySqlModel> dataSetMySqlModels = service.list();
        dataSetMySqlModels
                .forEach(x -> DATA_SET_MAP.put(x.getId(), x.getName()));
    }


    /**
     * Reload partner information
     */
    public static void refreshPartnerMap() throws StatusCodeWithException {
        PartnerService service = Launcher.CONTEXT.getBean(PartnerService.class);
        PARTNER_MAP.clear();

        List<PartnerMySqlModel> partnerMySqlModels = service.list();
        partnerMySqlModels
                .forEach(x -> PARTNER_MAP.put(x.getMemberId(), x.getMemberName()));
    }


    public static String getMemberId() {
        if (MEMBER_ID == null) {
            refreshMemberInfo();
        }
        return MEMBER_ID;
    }




    /**
     * 判断指定的 member_id 是属于当前本地成员
     */
    public static boolean isCurrentMember(String memberId) {
        return getMemberId().equals(memberId);
    }

    public static String getRsaPrivateKey() {
        if (RSA_PRIVATE_KEY == null) {
            refreshMemberInfo();
        }
        return RSA_PRIVATE_KEY;
    }

    public static String getRsaPublicKey() {
        if (RSA_PUBLIC_KEY == null) {
            refreshMemberInfo();
        }
        return RSA_PUBLIC_KEY;
    }

    public static String getMemberName() {
        if (MEMBER_NAME == null) {
            refreshMemberInfo();
        }
        return MEMBER_NAME;
    }


    public static List<String> getAccountIdList() {
        if (ACCOUNT_ID_LIST.isEmpty()) {
            refreshAccountMap();
        }
        return ACCOUNT_ID_LIST;
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
    public static synchronized String getNickname(String accountId) {
        if (accountId == null) {
            return null;
        }
        return getAccountMap().get(accountId) == null ? getMemberName() : getAccountMap().get(accountId);
    }

    /**
     * Determine whether accountId belongs to the current member
     */
    public static synchronized boolean isCurrentMemberAccount(String accountId) {
        return getAccountIdList().contains(accountId);
    }


    /**
     * Reload member information
     */
    public static synchronized void refreshMemberInfo() {
        GlobalConfigService service = Launcher.getBean(GlobalConfigService.class);
        MemberInfoModel model = service.getMemberInfo();

        if (model == null) {
            return;
        }

        MEMBER_ID = model.getMemberId();
        RSA_PUBLIC_KEY = model.getRsaPublicKey();
        RSA_PRIVATE_KEY = model.getRsaPrivateKey();
        MEMBER_NAME = model.getMemberName();
    }


    /**
     * Reload member information
     */
    public static synchronized void refreshFusionConfig() {
        GlobalConfigService service = Launcher.getBean(GlobalConfigService.class);
        FusionConfigModel model = service.getFusionConfig();

        if (model == null) {
            return;
        }

        OPEN_SOCKET_PORT = model.openSocketPort;
    }


    /**
     * Reload account list
     */
    public static synchronized void refreshAccountMap() {
        AccountRepository repo = Launcher.getBean(AccountRepository.class);
        List<AccountMysqlModel> list = repo.findAll(Sort.by("nickname"));

        ACCOUNT_MAP.clear();
        ACCOUNT_ID_LIST.clear();
        for (AccountMysqlModel item : list) {
            ACCOUNT_MAP.put(item.getId(), item.getNickname());
            ACCOUNT_ID_LIST.add(item.getId());
        }
    }


//    /**
//     * Reload member information
//     */
//    public static synchronized void refreshMemberInfo() {
//        GlobalConfigService service = Launcher.getBean(GlobalConfigService.class);
//        MemberInfoModel model = service.getMemberInfo();
//
//        if (model == null) {
//            return;
//        }
//
//        MEMBER_ID = model.getMemberId();
//        RSA_PUBLIC_KEY = model.getRsaPublicKey();
//        RSA_PRIVATE_KEY = model.getRsaPrivateKey();
//        MEMBER_NAME = model.getMemberName();
//    }


}
