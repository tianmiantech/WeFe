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

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
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
import java.util.stream.Collectors;


/**
 * @author yuxin.zhang
 */
@Repository
public class DataResourceMongoReop extends AbstractDataSetMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    @Override
    protected String getTableName() {
        return MongodbTable.Union.DATA_RESOURCE;
    }


    public void deleteByDataResourceId(String dataResourceId) {
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        mongoUnionTemplate.updateFirst(query, udpate, DataResource.class);
    }


    public boolean existsByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.exists(query, DataResource.class);
    }

    public DataResource findByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.findOne(query, DataResource.class);
    }

    public DataResource find(String dataResourceId, String curMemberId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return null;
        }
        Query query = new QueryBuilder()
                .notRemoved()
                .append("dataResourceId", dataResourceId)
                .append("curMemberId", curMemberId)
                .build();
        return mongoUnionTemplate.findOne(query, DataResource.class);
    }

    public List<String> findByDataResourceType(String dataResourceType) {
        Query query = new QueryBuilder().append("dataResourceType", dataResourceType).notRemoved().build();
        query.fields().exclude("_id").include("tags");
        List<DataResource> dataResourceList = mongoUnionTemplate.find(query, DataResource.class);
        List<String> tagsList = dataResourceList.stream().map(DataResource::getTags).collect(Collectors.toList());
        return tagsList;
    }


    public void upsert(DataResource dataResource) {
        mongoUnionTemplate.save(dataResource);
    }


    public DataResourceQueryOutput findCurMemberCanSee(String dataResourceId, String curMemeberId, String joinCollectionName) {

        LookupOperation lookupToDataImageDataSet = LookupOperation.newLookup().
                from(joinCollectionName).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as(joinCollectionName);

        LookupOperation lookupToMember = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");


        Criteria dataResouceCriteria = new QueryBuilder()
                .notRemoved()
                .append("enable", "1")
                .append("member_id", curMemeberId)
                .append("data_resource_id", dataResourceId)
                .getCriteria();


        AggregationOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);

        UnwindOperation unwind = Aggregation.unwind("member");
        UnwindOperation unwindExtraData = Aggregation.unwind(joinCollectionName);
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(
                dataResourceMatch,
                lookupToDataImageDataSet,
                lookupToMember,
                unwind,
                unwindExtraData,
                addFieldsOperation
        );

        DataResourceQueryOutput result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, DataResourceQueryOutput.class).getUniqueMappedResult();
        return result;
    }


    /**
     * Query the image data set visible to the current member
     */
    public PageOutput<DataResourceQueryOutput> findCurMemberCanSee(DataResourceQueryInput dataResourceQueryInput) {
        LookupOperation lookupToDataImageDataSet = LookupOperation.newLookup().
                from(MongodbTable.Union.IMAGE_DATASET).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as("image_data_set");


        LookupOperation lookupToDataTableDataSet = LookupOperation.newLookup().
                from(MongodbTable.Union.TABLE_DATASET).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as("table_data_set");

        LookupOperation lookupToDataBloomFilter = LookupOperation.newLookup().
                from(MongodbTable.Union.BLOOM_FILTER).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as("bloom_filter");

        LookupOperation lookupToMember = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");

        Criteria dataResouceCriteria = new QueryBuilder()
                .notRemoved()
                .append("enable", "1")
                .like("name", dataResourceQueryInput.getName())
                .like("tags", dataResourceQueryInput.getTag())
                .append("member_id", dataResourceQueryInput.getCurMemberId())
                .append("data_resource_id", dataResourceQueryInput.getDataResourceId())
                .getCriteria();

        Criteria or = new Criteria();
        or.orOperator(
                new QueryBuilder().append("public_level", "Public").getCriteria(),
                new QueryBuilder().like("public_member_list", dataResourceQueryInput.getCurMemberId()).getCriteria()
        );

        dataResouceCriteria.andOperator(or);

        AggregationOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);

        Criteria memberCriteria = new QueryBuilder()
                .like("member_name", dataResourceQueryInput.getMemberName())
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);
        UnwindOperation unwindMember = Aggregation.unwind("member");
        UnwindOperation unwindImageDataSet = Aggregation.unwind("image_data_set", true);
        UnwindOperation unwindTableDataSet = Aggregation.unwind("table_data_set", true);
        UnwindOperation unwindBloomFilter = Aggregation.unwind("bloom_filter", true);
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(
                dataResourceMatch,
                memberMatch,
                lookupToDataImageDataSet,
                lookupToDataTableDataSet,
                lookupToDataBloomFilter,
                lookupToMember,
                unwindMember,
                unwindImageDataSet,
                unwindTableDataSet,
                unwindBloomFilter,
                addFieldsOperation
        );
        int total = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, DataResourceQueryOutput.class).getMappedResults().size();

        SkipOperation skipOperation = Aggregation.skip((long) dataResourceQueryInput.getPageIndex() * dataResourceQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(dataResourceQueryInput.getPageSize());

        aggregation = Aggregation.newAggregation(
                dataResourceMatch,
                memberMatch,
                lookupToDataImageDataSet,
                lookupToDataTableDataSet,
                lookupToDataBloomFilter,
                lookupToMember,
                unwindImageDataSet,
                unwindTableDataSet,
                unwindBloomFilter,
                skipOperation,
                limitOperation,
                addFieldsOperation
        );

        List<DataResourceQueryOutput> result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, DataResourceQueryOutput.class).getMappedResults();

        return new PageOutput<>(dataResourceQueryInput.getPageIndex(), (long) total, dataResourceQueryInput.getPageSize(), result);
    }

}
