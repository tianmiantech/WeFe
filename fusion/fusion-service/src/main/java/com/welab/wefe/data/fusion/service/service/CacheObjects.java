/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.GlobalSettingMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.GlobalSettingRepository;
import com.welab.wefe.data.fusion.service.service.bloomfilter.BloomFilterService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;

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

    private static String PARTNER_ID;
    private static String RSA_PRIVATE_KEY;
    private static String RSA_PUBLIC_KEY;
    private static String PARTNER_NAME;
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


    public static final String getPartnerId() {
        if (PARTNER_ID == null) {
            refreshMemberInfo();
        }
        return PARTNER_ID;
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

    public static Integer getOpenSocketPort() {
        if (OPEN_SOCKET_PORT == null) {
            refreshMemberInfo();
        }
        return OPEN_SOCKET_PORT;
    }

    public static final String getMemberName() {
        if (PARTNER_NAME == null) {
            refreshMemberInfo();
        }
        return PARTNER_NAME;
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
     * Reload the member information
     */
    public static void refreshMemberInfo() {
        GlobalSettingRepository repo = Launcher.CONTEXT.getBean(GlobalSettingRepository.class);
        GlobalSettingMySqlModel model = repo.singleton();

        PARTNER_ID = model.getPartnerId();
        RSA_PUBLIC_KEY = model.getRsaPublicKey();
        RSA_PRIVATE_KEY = model.getRsaPrivateKey();
        PARTNER_NAME = model.getPartnerName();
        OPEN_SOCKET_PORT = model.getOpenSocketPort();
    }

    /**
     * Reload partner information
     */
    public static void refreshPartnerMap() throws StatusCodeWithException {
        PartnerService service = Launcher.CONTEXT.getBean(PartnerService.class);
        PARTNER_MAP.clear();

        List<PartnerMySqlModel> partnerMySqlModels = service.list();
        partnerMySqlModels
                .forEach(x -> PARTNER_MAP.put(x.getPartnerId(), x.getName()));
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

}
