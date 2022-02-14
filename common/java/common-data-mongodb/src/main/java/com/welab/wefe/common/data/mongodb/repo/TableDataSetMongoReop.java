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
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.data.mongodb.util.AddFieldsOperation;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import com.welab.wefe.common.util.JObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
public class TableDataSetMongoReop extends AbstractDataSetMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    @Override
    protected String getTableName() {
        return MongodbTable.Union.TABLE_DATASET;
    }


    public boolean existsByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.exists(query, TableDataSet.class);
    }

    public TableDataSet findByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.findOne(query, TableDataSet.class);
    }


    public void upsert(TableDataSet tableDataSet) {
        mongoUnionTemplate.save(tableDataSet);
    }


    /**
     * Query the table data set visible to the current member
     */
    public PageOutput<DataResourceQueryOutput> findCurMemberCanSee(DataResourceQueryInput dataResourceQueryInput) {
        LookupOperation lookupToDataImageDataSet = LookupOperation.newLookup().
                from(MongodbTable.Union.TABLE_DATASET).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as("table_data_set");

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
                .append("member_id", dataResourceQueryInput.getMemberId())
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

        Criteria tableDataSetCriteria = new QueryBuilder()
                .append("table_data_set.contains_y", null == dataResourceQueryInput.getContainsY() ? null : String.valueOf(dataResourceQueryInput.getContainsY() ? 1 : 0))
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);
        AggregationOperation tableDataSetMatch = Aggregation.match(tableDataSetCriteria);
        UnwindOperation unwindMember = Aggregation.unwind("member");
        UnwindOperation unwindTableDataSet = Aggregation.unwind("table_data_set");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        SkipOperation skipOperation = Aggregation.skip((long) dataResourceQueryInput.getPageIndex() * dataResourceQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(dataResourceQueryInput.getPageSize());
        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Order.desc("updated_time")));

        CountOperation countOperation = Aggregation.count().as("count");
        FacetOperation facetOperation = Aggregation.facet(
                lookupToDataImageDataSet,
                lookupToMember,
                unwindMember,
                unwindTableDataSet,
                addFieldsOperation,
                dataResourceMatch,
                memberMatch,
                tableDataSetMatch,
                skipOperation,
                limitOperation,
                sortOperation
        ).as("data").and(
                lookupToDataImageDataSet,
                lookupToMember,
                unwindMember,
                unwindTableDataSet,
                dataResourceMatch,
                memberMatch,
                tableDataSetMatch,
                countOperation
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

    public void deleteByDataResourceId(String dataResourceId) {
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        mongoUnionTemplate.updateFirst(query, udpate, TableDataSet.class);
    }

}
