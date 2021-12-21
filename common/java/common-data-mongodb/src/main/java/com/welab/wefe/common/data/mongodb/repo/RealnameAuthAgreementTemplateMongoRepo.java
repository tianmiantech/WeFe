/*
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

package com.welab.wefe.common.data.mongodb.repo;

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.entity.union.ext.RealnameAuthAgreementTemplateExtJSON;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author aaron.li
 **/
@Repository
public class RealnameAuthAgreementTemplateMongoRepo extends AbstractMongoRepo {
    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    public List<RealnameAuthAgreementTemplate> find() {
        return mongoUnionTemplate.find(new QueryBuilder().sort("createdTime").build(), RealnameAuthAgreementTemplate.class);
    }

    public RealnameAuthAgreementTemplate findByTemplateFileSign(String templateFileSign) {
        return mongoUnionTemplate.findOne(
                new QueryBuilder().append("templateFileSign", templateFileSign).notRemoved().build(),
                RealnameAuthAgreementTemplate.class);
    }

    public RealnameAuthAgreementTemplate findByTemplateFileId(String templateFileId) {
        return mongoUnionTemplate.findOne(
                new QueryBuilder().append("templateFileId", templateFileId).notRemoved().build(),
                RealnameAuthAgreementTemplate.class);
    }


    public RealnameAuthAgreementTemplate findByEnable(boolean enable) {
        return mongoUnionTemplate.findOne(
                new QueryBuilder().append("enable", String.valueOf(enable ? 1 : 0)).notRemoved().build(),
                RealnameAuthAgreementTemplate.class);
    }

    public boolean updateEnable(String templateFileId, String enable, String updatedTime) {
        if (StringUtils.isEmpty(templateFileId)) {
            return false;
        }
        Query query = new QueryBuilder().append("templateFileId", templateFileId).build();
        Update udpate = new UpdateBuilder()
                .append("enable", enable)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, RealnameAuthAgreementTemplate.class);
        return updateResult.wasAcknowledged();
    }

    public boolean updateExtJSONById(String templateFileId, RealnameAuthAgreementTemplateExtJSON extJSON, String updatedTime) {
        if (StringUtils.isEmpty(templateFileId)) {
            return false;
        }
        Query query = new QueryBuilder().append("templateFileId", templateFileId).build();
        Update update = new UpdateBuilder()
                .append("extJson", extJSON)
                .append("updatedTime", updatedTime)
                .build();

        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, RealnameAuthAgreementTemplate.class);
        return updateResult.wasAcknowledged();
    }

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }
}
