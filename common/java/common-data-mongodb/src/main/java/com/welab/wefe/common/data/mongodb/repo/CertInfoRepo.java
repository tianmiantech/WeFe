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

package com.welab.wefe.common.data.mongodb.repo;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;

@Repository
public class CertInfoRepo extends AbstractMongoRepo<CertInfo> {

    @Autowired
    protected MongoTemplate mongoManagerTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoManagerTemplate;
    }

    public CertInfo findByPkId(String pkId) {
        Query query = new QueryBuilder().append("pkId", pkId).build();
        return mongoManagerTemplate.findOne(query, CertInfo.class);
    }

    public CertInfo findBySerialNumber(String serialNumber) {
        Query query = new QueryBuilder().append("serialNumber", serialNumber).build();
        return mongoManagerTemplate.findOne(query, CertInfo.class);
    }

    public PageOutput<CertInfo> findCertList(String userId, String pCertId, Boolean isCACert, Boolean isRootCert,
            int pageIndex, int pageSize) {
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtils.isNotBlank(userId)) {
            queryBuilder.append("userId", userId);
        }
        if (StringUtils.isNotBlank(pCertId)) {
            queryBuilder.append("pCertId", pCertId);
        }
        if (isCACert != null) {
            queryBuilder.append("isCACert", isCACert);
        }
        if (isRootCert != null) {
            queryBuilder.append("isRootCert", isRootCert);
        }

        Query query = queryBuilder.page(pageIndex, pageSize).build();
        List<CertInfo> list = mongoManagerTemplate.find(query, CertInfo.class);
        long count = mongoManagerTemplate.count(query, CertInfo.class);
        return new PageOutput<>(pageIndex, count, pageSize, list);
    }

    public List<CertInfo> findCerts(String userId, String pCertId, Boolean isCACert, Boolean isRootCert) {
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtils.isNotBlank(userId)) {
            queryBuilder.append("userId", userId);
        }
        if (StringUtils.isNotBlank(pCertId)) {
            queryBuilder.append("pCertId", pCertId);
        }
        if (isCACert != null) {
            queryBuilder.append("isCACert", isCACert);
        }
        if (isRootCert != null) {
            queryBuilder.append("isRootCert", isRootCert);
        }

        Query query = queryBuilder.build();
        List<CertInfo> list = mongoManagerTemplate.find(query, CertInfo.class);
        return list;
    }

    public long count() {
        return mongoManagerTemplate.count(new Query(), CertInfo.class);
    }

    public List<CertInfo> findAll() {
        return mongoManagerTemplate.findAll(CertInfo.class);
    }

    public void updateStatus(String serialNumber, String status) {
        Query query = new QueryBuilder().append("serialNumber", serialNumber).build();
        Update update = new UpdateBuilder().append("status", status).append("updateTime", System.currentTimeMillis())
                .build();
        mongoManagerTemplate.updateFirst(query, update, CertInfo.class);
    }
}
