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

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.util.AddFieldsOperation;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
                .append("memberId", curMemberId)
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


    public DataResourceQueryOutput findCurMemberCanSee(String dataResourceId, String joinCollectionName) {

        String joinCollectionNameAlias = StringUtil.camelCaseToUnderLineCase(joinCollectionName);
        if (joinCollectionNameAlias.startsWith("_")) {
            joinCollectionNameAlias = joinCollectionNameAlias.replaceFirst("_", "");
        }

        LookupOperation lookupToDataImageDataSet = LookupOperation.newLookup().
                from(joinCollectionName).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as(joinCollectionNameAlias);

        LookupOperation lookupToMember = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");


        Criteria dataResouceCriteria = new QueryBuilder()
                .notRemoved()
                .append("enable", "1")
                .append("data_resource_id", dataResourceId)
                .getCriteria();


        AggregationOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);

        UnwindOperation unwind = Aggregation.unwind("member");
        UnwindOperation unwindExtraData = Aggregation.unwind(joinCollectionNameAlias);
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(
                lookupToDataImageDataSet,
                lookupToMember,
                unwind,
                unwindExtraData,
                addFieldsOperation,
                dataResourceMatch
        );

        DataResourceQueryOutput result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, DataResourceQueryOutput.class).getUniqueMappedResult();
        return result;
    }

    private List<LookupOperation> buildLookupOperations(List<DataResourceType> dataResourceTypeList) {
        List<LookupOperation> lookupOperations = new ArrayList<>();
        LookupOperation lookupToMember = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");
        lookupOperations.add(lookupToMember);

        for (DataResourceType dataResourceType : dataResourceTypeList) {
            String joinCollectionName = null;
            String joinCollectionNameAlias;
            if (DataResourceType.ImageDataSet.compareTo(dataResourceType) == 0) {
                joinCollectionName = MongodbTable.Union.IMAGE_DATASET;
            } else if (DataResourceType.TableDataSet.compareTo(dataResourceType) == 0) {
                joinCollectionName = MongodbTable.Union.TABLE_DATASET;
            } else if (DataResourceType.BloomFilter.compareTo(dataResourceType) == 0) {
                joinCollectionName = MongodbTable.Union.BLOOM_FILTER;
            }

            joinCollectionNameAlias = StringUtil.camelCaseToUnderLineCase(joinCollectionName);

            LookupOperation lookupOperation = LookupOperation.newLookup().
                    from(joinCollectionName).
                    localField("data_resource_id").
                    foreignField("data_resource_id").
                    as(joinCollectionNameAlias);

            lookupOperations.add(lookupOperation);

        }

        return lookupOperations;
    }


    private List<UnwindOperation> buildUnwindOperations(List<DataResourceType> dataResourceTypeList) {
        List<UnwindOperation> unwindOperations = new ArrayList<>();
        UnwindOperation unwindMember = Aggregation.unwind("member", true);
        unwindOperations.add(unwindMember);

        for (DataResourceType dataResourceType : dataResourceTypeList) {
            String unwindField = null;
            if (DataResourceType.ImageDataSet.compareTo(dataResourceType) == 0) {
                unwindField = MongodbTable.Union.IMAGE_DATASET;
            } else if (DataResourceType.TableDataSet.compareTo(dataResourceType) == 0) {
                unwindField = MongodbTable.Union.TABLE_DATASET;
            } else if (DataResourceType.BloomFilter.compareTo(dataResourceType) == 0) {
                unwindField = MongodbTable.Union.BLOOM_FILTER;
            }
            unwindField = StringUtil.camelCaseToUnderLineCase(unwindField);
            unwindOperations.add(Aggregation.unwind(unwindField, true));
        }
        return unwindOperations;
    }


    private List<MatchOperation> buildCurMemberCanSeeMatchOperations(DataResourceQueryInput dataResourceQueryInput) {
        List<MatchOperation> matchOperations = new ArrayList<>();
        Criteria dataResouceCriteria = new QueryBuilder()
                .notRemoved()
                .append("enable", "1")
                .like("name", dataResourceQueryInput.getName())
                .like("tags", dataResourceQueryInput.getTag())
                .append("member_id", dataResourceQueryInput.getMemberId())
                .append("data_resource_id", dataResourceQueryInput.getDataResourceId())
                .in("data_resource_type", dataResourceQueryInput.getDataResourceType().stream().map(Enum::name).collect(Collectors.toList()))
                .getCriteria();

        Criteria or = new Criteria();
        or.orOperator(
                new QueryBuilder().append("public_level", "Public").getCriteria(),
                new QueryBuilder().append("member_id", dataResourceQueryInput.getCurMemberId()).getCriteria(),
                new QueryBuilder().like("public_member_list", dataResourceQueryInput.getCurMemberId()).getCriteria()
        );
        dataResouceCriteria.andOperator(or);

        MatchOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);
        matchOperations.add(dataResourceMatch);
        Criteria memberCriteria = new QueryBuilder()
                .like("member.name", dataResourceQueryInput.getMemberName())
                .append("member.hidden", "0")
                .append("member.freezed", "0")
                .append("member.lost_contact", "0")
                .append("member.allow_open_data_set", "1")
                .getCriteria();

        MatchOperation memberMatch = Aggregation.match(memberCriteria);
        matchOperations.add(memberMatch);

        if (dataResourceQueryInput.getDataResourceType().contains(DataResourceType.ImageDataSet)) {
            String forJobType = dataResourceQueryInput.getForJobType() == null ? null : dataResourceQueryInput.getForJobType().name();
            Criteria imageDataSetCriteria = new QueryBuilder()
                    .append(StringUtil.camelCaseToUnderLineCase(MongodbTable.Union.IMAGE_DATASET) + ".for_job_type", forJobType)
                    .getCriteria();
            MatchOperation imageDataSetMatch = Aggregation.match(imageDataSetCriteria);
            matchOperations.add(imageDataSetMatch);

        }

        if (dataResourceQueryInput.getDataResourceType().contains(DataResourceType.TableDataSet)
                || dataResourceQueryInput.getDataResourceType().contains(DataResourceType.BloomFilter)) {
            Criteria tableDataSetCriteria = new QueryBuilder()
                    .append(StringUtil.camelCaseToUnderLineCase(MongodbTable.Union.TABLE_DATASET) + ".contains_y", null == dataResourceQueryInput.getContainsY() ? null : String.valueOf(dataResourceQueryInput.getContainsY() ? 1 : 0))
                    .getCriteria();
            MatchOperation tableDataSetMatch = Aggregation.match(tableDataSetCriteria);
            matchOperations.add(tableDataSetMatch);
        }


        return matchOperations;
    }


    private List<MatchOperation> buildMatchOperations(DataResourceQueryInput dataResourceQueryInput) {
        List<MatchOperation> matchOperations = new ArrayList<>();
        Criteria dataResouceCriteria = new QueryBuilder()
                .append("status", dataResourceQueryInput.getStatus() == null ? null : String.valueOf(dataResourceQueryInput.getStatus() ? 1 : 0))
                .append("enable", dataResourceQueryInput.getEnable() == null ? null : String.valueOf(dataResourceQueryInput.getEnable() ? 1 : 0))
                .like("name", dataResourceQueryInput.getName())
                .like("tags", dataResourceQueryInput.getTag())
                .append("member_id", dataResourceQueryInput.getMemberId())
                .append("data_resource_id", dataResourceQueryInput.getDataResourceId())
                .in("data_resource_type", dataResourceQueryInput.getDataResourceType().stream().map(Enum::name).collect(Collectors.toList()))
                .getCriteria();


        MatchOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);
        matchOperations.add(dataResourceMatch);
        Criteria memberCriteria = new QueryBuilder()
                .like("member.name", dataResourceQueryInput.getMemberName())
                .getCriteria();

        MatchOperation memberMatch = Aggregation.match(memberCriteria);
        matchOperations.add(memberMatch);

        if (dataResourceQueryInput.getDataResourceType().contains(DataResourceType.ImageDataSet)) {
            String forJobType = dataResourceQueryInput.getForJobType() == null ? null : dataResourceQueryInput.getForJobType().name();
            Criteria imageDataSetCriteria = new QueryBuilder()
                    .append(StringUtil.camelCaseToUnderLineCase(MongodbTable.Union.IMAGE_DATASET) + ".for_job_type", forJobType)
                    .getCriteria();
            MatchOperation imageDataSetMatch = Aggregation.match(imageDataSetCriteria);
            matchOperations.add(imageDataSetMatch);

        }

        if (dataResourceQueryInput.getDataResourceType().contains(DataResourceType.TableDataSet)
                || dataResourceQueryInput.getDataResourceType().contains(DataResourceType.BloomFilter)) {
            Criteria tableDataSetCriteria = new QueryBuilder()
                    .append(StringUtil.camelCaseToUnderLineCase(MongodbTable.Union.TABLE_DATASET) + ".contains_y", null == dataResourceQueryInput.getContainsY() ? null : String.valueOf(dataResourceQueryInput.getContainsY() ? 1 : 0))
                    .getCriteria();
            MatchOperation tableDataSetMatch = Aggregation.match(tableDataSetCriteria);
            matchOperations.add(tableDataSetMatch);
        }


        return matchOperations;
    }


    public PageOutput<DataResourceQueryOutput> findCurMemberCanSee(DataResourceQueryInput dataResourceQueryInput) {
        return find(dataResourceQueryInput, false);
    }


    public PageOutput<DataResourceQueryOutput> findAll(DataResourceQueryInput dataResourceQueryInput) {
        return find(dataResourceQueryInput, true);
    }


    public PageOutput<DataResourceQueryOutput> find(DataResourceQueryInput dataResourceQueryInput, boolean isQueryAll) {
        List<AggregationOperation> dataAggregationOperations = new ArrayList<>();
        List<AggregationOperation> totalAggregationOperations = new ArrayList<>();

        List<LookupOperation> lookupOperations = buildLookupOperations(dataResourceQueryInput.getDataResourceType());
        dataAggregationOperations.addAll(lookupOperations);
        totalAggregationOperations.addAll(lookupOperations);

        List<UnwindOperation> unwindOperations = buildUnwindOperations(dataResourceQueryInput.getDataResourceType());
        dataAggregationOperations.addAll(unwindOperations);
        totalAggregationOperations.addAll(unwindOperations);

        List<MatchOperation> matchOperations;
        if (isQueryAll) {
            matchOperations = buildMatchOperations(dataResourceQueryInput);
        } else {
            matchOperations = buildCurMemberCanSeeMatchOperations(dataResourceQueryInput);
        }

        dataAggregationOperations.addAll(matchOperations);
        totalAggregationOperations.addAll(matchOperations);

        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");
        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        SkipOperation skipOperation = Aggregation.skip((long) dataResourceQueryInput.getPageIndex() * dataResourceQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(dataResourceQueryInput.getPageSize());
        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Order.desc("created_time")));

        dataAggregationOperations.add(addFieldsOperation);
        dataAggregationOperations.add(sortOperation);
        dataAggregationOperations.add(skipOperation);
        dataAggregationOperations.add(limitOperation);

        CountOperation countOperation = Aggregation.count().as("count");
        totalAggregationOperations.add(countOperation);

        FacetOperation facetOperation = Aggregation.facet(
                dataAggregationOperations.toArray(new AggregationOperation[dataAggregationOperations.size()])
        ).as("data").and(
                totalAggregationOperations.toArray(new AggregationOperation[totalAggregationOperations.size()])
        ).as("total");


        Aggregation aggregation = Aggregation.newAggregation(facetOperation);
        JObject result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, JObject.class).getUniqueMappedResult();
        Long total = 0L;
        List<DataResourceQueryOutput> list = result.getJSONList("data", DataResourceQueryOutput.class);
        if (list != null && !list.isEmpty()) {
            total = result.getJSONList("total", JObject.class).get(0).getLongValue("count");
        }
        return new PageOutput<>(dataResourceQueryInput.getPageIndex(), total, dataResourceQueryInput.getPageSize(), list);
    }

}
