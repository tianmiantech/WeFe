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
package com.welab.wefe.gateway.cache;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.entity.PartnerConfigEntity;
import com.welab.wefe.gateway.service.PartnerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PartnerConfigCache {
    private final Logger LOG = LoggerFactory.getLogger(PartnerConfigCache.class);

    private static PartnerConfigCache partnerConfigCache = new PartnerConfigCache();

    private ConcurrentHashMap<String, PartnerConfigEntity> data = new ConcurrentHashMap<>();

    private PartnerConfigCache() {
    }

    public static PartnerConfigCache getInstance() {
        return partnerConfigCache;
    }

    public PartnerConfigEntity get(String memberId) {
        return data.get(memberId);
    }

    public boolean refreshCache() {
        try {
            PartnerConfigService partnerConfigService = GatewayServer.CONTEXT.getBean(PartnerConfigService.class);
            List<PartnerConfigEntity> partnerConfigEntityList = partnerConfigService.findAll();
            if (CollectionUtils.isEmpty(partnerConfigEntityList)) {
                data.clear();
                return true;
            }

            List<String> queryMemberIds = new ArrayList<>();
            for (PartnerConfigEntity entity : partnerConfigEntityList) {
                queryMemberIds.add(entity.getMemberId());
                data.put(entity.getMemberId(), entity);
            }
            // Clear deleted member information
            List<String> delMemberIds = new ArrayList<>();
            data.forEach((key, value) -> {
                if (!queryMemberIds.contains(key)) {
                    delMemberIds.add(key);
                }
            });
            for (String delMemberId : delMemberIds) {
                data.remove(delMemberId);
            }

            return true;
        } catch (Exception e) {
            LOG.error("Refresh partner config cache exception: ", e);
            return false;
        }
    }
}
