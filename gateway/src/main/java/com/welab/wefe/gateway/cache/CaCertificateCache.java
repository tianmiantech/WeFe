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
import com.welab.wefe.gateway.service.CaCertificateService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CA certificate cache
 * Get CA certificate from union service
 */
public class CaCertificateCache {
    private final Logger LOG = LoggerFactory.getLogger(CaCertificateCache.class);

    private final static CaCertificateCache caCertificateCache = new CaCertificateCache();

    private ConcurrentHashMap<String, CaCertificate> data = new ConcurrentHashMap<>();

    private CaCertificateCache() {
    }

    public static CaCertificateCache getInstance() {
        return caCertificateCache;
    }


    public boolean refreshCache() {
        try {
            List<CaCertificate> caCertificateList = GatewayServer.CONTEXT.getBean(CaCertificateService.class).findAll();
            if (CollectionUtils.isEmpty(caCertificateList)) {
                data.clear();
                return true;
            }

            // Update cache
            List<String> queryIds = new ArrayList<>();
            for (CaCertificate caCertificate : caCertificateList) {
                data.put(caCertificate.getId(), caCertificate);
                queryIds.add(caCertificate.getId());
            }
            List<String> deletedIds = new ArrayList<>();
            data.forEach((key, value) -> {
                if (!queryIds.contains(key)) {
                    deletedIds.add(key);
                }
            });
            deletedIds.forEach(id -> {
                data.remove(id);
            });
            return true;
        } catch (Exception e) {
            LOG.error("Refresh ca certificate cache exception: ", e);
            return false;
        }
    }


    public CaCertificate get(String id) {
        return data.get(id);
    }

    public List<CaCertificate> getAll() {
        return new ArrayList<>(data.values());
    }


    public static class CaCertificate {
        private String id;
        private String name;
        private String content;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
