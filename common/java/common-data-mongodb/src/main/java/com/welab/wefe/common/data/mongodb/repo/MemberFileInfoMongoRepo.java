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
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberFileInfoExtJSON;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @author yuxin.zhang
 **/
@Repository
public class MemberFileInfoMongoRepo extends AbstractMongoRepo {
    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }


    public MemberFileInfo findByMemberId(String memberId) {
        return mongoUnionTemplate.findOne(
                new QueryBuilder()
                        .append("memberId", memberId)
                        .notRemoved()
                        .build()
                ,
                MemberFileInfo.class);
    }

    public MemberFileInfo findByFileId(String fileId) {
        return mongoUnionTemplate.findOne(
                new QueryBuilder()
                        .append("fileId", fileId)
                        .notRemoved()
                        .build()
                ,
                MemberFileInfo.class);
    }

    public MemberFileInfo findByFileSign(String fileSign) {
        return mongoUnionTemplate.findOne(
                new QueryBuilder()
                        .append("fileSign", fileSign)
                        .notRemoved()
                        .build()
                ,
                MemberFileInfo.class);
    }


    public boolean updateEnable(String fileId, String enable, String updatedTime) {
        if (StringUtils.isEmpty(fileId)) {
            return false;
        }
        Query query = new QueryBuilder().append("fileId", fileId).build();
        Update udpate = new UpdateBuilder()
                .append("enable", enable)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, MemberFileInfo.class);
        return updateResult.wasAcknowledged();
    }

    public boolean updateExtJSONById(String fileId, String updatedTime, MemberFileInfoExtJSON extJSON) {
        if (StringUtils.isEmpty(fileId)) {
            return false;
        }
        Query query = new QueryBuilder().append("fileId", fileId).build();
        Update update = new UpdateBuilder().
                append("extJson", extJSON).
                append("updatedTime", updatedTime).
                build();

        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, MemberFileInfo.class);
        return updateResult.wasAcknowledged();
    }
}
