/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.mongodb.repo;

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.member.MemberServiceQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.MemberService;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberServiceExtJSON;
import com.welab.wefe.common.data.mongodb.util.AddFieldsOperation;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuxin.zhang
 */
@Repository
public class MemberServiceMongoReop extends AbstractMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    public boolean deleteMemberServiceById(String serviceId) {
        Query query = new QueryBuilder().append("serviceId", serviceId).build();
        Update update = new UpdateBuilder().remove().build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, MemberService.class);
        return updateResult.wasAcknowledged();
    }

    public void upsert(MemberService memberService) {
        mongoUnionTemplate.save(memberService);
    }

    public boolean existsByServiceId(String serviceId) {
        if (StringUtils.isEmpty(serviceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("serviceId", serviceId).build();
        return mongoUnionTemplate.exists(query, MemberService.class);
    }

    public MemberService findByServiceId(String serviceId) {
        if (StringUtils.isEmpty(serviceId)) {
            return null;
        }
        Query query = new QueryBuilder()
                .append("serviceId", serviceId)
                .append("serviceStatus", "1")
                .notRemoved()
                .build();
        return mongoUnionTemplate.findOne(query, MemberService.class);
    }


    public PageOutput<MemberServiceQueryOutput> find(Integer pageIndex, Integer pageSize, String serviceId, String memberId, String memberName, String serviceName, String serviceType) {
        LookupOperation lookupToLots = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");

        Criteria memberServiceCriteria = new QueryBuilder()
                .notRemoved()
                .like("name", serviceName)
                .append("service_id", serviceId)
                .append("member_id", memberId)
                .append("service_type", serviceType)
                .append("service_status", "1")
                .getCriteria();


        AggregationOperation memberServiceMatch = Aggregation.match(memberServiceCriteria);

        Criteria memberCriteria = new QueryBuilder()
                .notRemoved()
                .like("member_name", memberName)
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);


        UnwindOperation unwind = Aggregation.unwind("member");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.serviceName");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(lookupToLots, memberServiceMatch, memberMatch, unwind, addFieldsOperation);
        int total = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.MEMBER_SERVICE, MemberServiceQueryOutput.class).getMappedResults().size();

        SkipOperation skipOperation = Aggregation.skip((long) pageIndex * pageSize);
        LimitOperation limitOperation = Aggregation.limit(pageSize);
        aggregation = Aggregation.newAggregation(lookupToLots, memberServiceMatch, memberMatch, unwind, skipOperation, limitOperation, addFieldsOperation);

        List<MemberServiceQueryOutput> result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.MEMBER_SERVICE, MemberServiceQueryOutput.class).getMappedResults();

        return new PageOutput<>(pageIndex, (long) total, pageSize, result);
    }

    public PageOutput<MemberService> query(Integer pageIndex, Integer pageSize, String serviceId, String memberId, String name, String serviceType) {
        Query query = new QueryBuilder()
                .notRemoved()
                .append("serviceStatus", "1")
                .append("serviceId", serviceId)
                .append("memberId", memberId)
                .like("name", name)
                .append("serviceType", serviceType)
                .page(pageIndex, pageSize)
                .sort("updatedTime")
                .build();

        List<MemberService> list = mongoUnionTemplate.find(query, MemberService.class);
        long total = mongoUnionTemplate.count(query, MemberService.class);
        return new PageOutput<>(pageIndex, total, query.getLimit(), list);
    }


    public void updateExtJSONById(String serviceId, String updatedTime, MemberServiceExtJSON extJSON) {
        Query query = new QueryBuilder().append("serviceId", serviceId).build();
        Update update = new UpdateBuilder().
                append("extJson", extJSON).
                append("updatedTime", updatedTime).
                build();

        mongoUnionTemplate.updateFirst(query, update, MemberFileInfo.class);
    }

}
