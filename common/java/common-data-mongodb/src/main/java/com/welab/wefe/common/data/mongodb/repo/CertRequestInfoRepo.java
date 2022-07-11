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
import org.springframework.stereotype.Repository;

import com.welab.wefe.common.data.mongodb.entity.manager.CertRequestInfo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;

@Repository
public class CertRequestInfoRepo extends AbstractMongoRepo<CertRequestInfo> {

    @Autowired
    protected MongoTemplate mongoManagerTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoManagerTemplate;
    }

    public CertRequestInfo findByPkId(String pkId) {
        Query query = new QueryBuilder().append("pkId", pkId).build();
        return mongoManagerTemplate.findOne(query, CertRequestInfo.class);
    }

    public CertRequestInfo findBypCertIdAndSubjectKeyId(String pCertId, String subjectKeyId) {
        Query query = new QueryBuilder().append("pCertId", pCertId).append("subjectKeyId", subjectKeyId).build();
        return mongoManagerTemplate.findOne(query, CertRequestInfo.class);
    }

    public List<CertRequestInfo> findCertRequestList(String userId, String subjectKeyId, String pCertId,
            String subjectOrg, String subjectCN, String pCertUserId) {
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtils.isNotBlank(userId)) {
            queryBuilder.append("userId", userId);
        }
        if (StringUtils.isNotBlank(subjectKeyId)) {
            queryBuilder.append("subjectKeyId", subjectKeyId);
        }
        if (StringUtils.isNotBlank(pCertId)) {
            queryBuilder.append("pCertId", pCertId);
        }
        if (StringUtils.isNotBlank(subjectOrg)) {
            queryBuilder.append("subjectOrg", subjectOrg);
        }
        if (StringUtils.isNotBlank(subjectCN)) {
            queryBuilder.append("subjectCN", subjectCN);
        }
        if (StringUtils.isNotBlank(pCertUserId)) {
            queryBuilder.append("pCertUserId", pCertUserId);
        }
        Query query = queryBuilder.build();
        List<CertRequestInfo> list = mongoManagerTemplate.find(query, CertRequestInfo.class);
        return list;
    }

}
