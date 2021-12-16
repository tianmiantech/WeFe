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
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.ImageDataSetQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataset.TableDataSetQueryInput;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.TableDataSetExtJSON;
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


    public DataResourceQueryOutput findCurMemberCanSee(String dataResourceId, String curMemeberId) {
        LookupOperation lookupToDataImageDataSet = LookupOperation.newLookup().
                from(MongodbTable.Union.TABLE_DATASET).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as("extra_data");

        LookupOperation lookupToMember = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");


        Criteria dataResouceCriteria = new QueryBuilder()
                .append("enable","0")
                .append("member_id", curMemeberId)
                .append("data_resource_id", dataResourceId)
                .getCriteria();


        AggregationOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);

        UnwindOperation unwind = Aggregation.unwind("member");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(
                dataResourceMatch,
                lookupToDataImageDataSet,
                lookupToMember,
                unwind,
                addFieldsOperation
        );

        DataResourceQueryOutput result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.IMAGE_DATASET, DataResourceQueryOutput.class).getUniqueMappedResult();
        return result;
    }

    /**
     * Query the table data set visible to the current member
     */
    public PageOutput<DataResourceQueryOutput> findCurMemberCanSee(TableDataSetQueryInput tableDataSetQueryInput) {
        LookupOperation lookupToDataImageDataSet = LookupOperation.newLookup().
                from(MongodbTable.Union.TABLE_DATASET).
                localField("data_resource_id").
                foreignField("data_resource_id").
                as("extra_data");

        LookupOperation lookupToMember = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");


        Criteria dataResouceCriteria = new QueryBuilder()
                .append("enable","0")
                .like("name", tableDataSetQueryInput.getName())
                .like("tags", tableDataSetQueryInput.getTag())
                .append("member_id", tableDataSetQueryInput.getCurMemberId())
                .append("data_resource_id", tableDataSetQueryInput.getDataResourceId())
                .append("contains_y", null == tableDataSetQueryInput.getContainsY() ? null : String.valueOf(tableDataSetQueryInput.getContainsY() ? 1 : 0))
                .getCriteria();

        Criteria or = new Criteria();
        or.orOperator(
                new QueryBuilder().append("public_level", "Public").getCriteria(),
                new QueryBuilder().like("public_member_list", tableDataSetQueryInput.getCurMemberId()).getCriteria()
        );

        dataResouceCriteria.andOperator(or);

        AggregationOperation dataResourceMatch = Aggregation.match(dataResouceCriteria);

        Criteria memberCriteria = new QueryBuilder()
                .like("name", tableDataSetQueryInput.getMemberName())
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);
        UnwindOperation unwind = Aggregation.unwind("member");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(
                dataResourceMatch,
                memberMatch,
                lookupToDataImageDataSet,
                lookupToMember,
                unwind,
                addFieldsOperation
        );
        int total = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, DataSetQueryOutput.class).getMappedResults().size();

        SkipOperation skipOperation = Aggregation.skip((long) tableDataSetQueryInput.getPageIndex() * tableDataSetQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(tableDataSetQueryInput.getPageSize());

        aggregation = Aggregation.newAggregation(
                dataResourceMatch,
                memberMatch,
                lookupToDataImageDataSet,
                lookupToMember,
                unwind,
                skipOperation,
                limitOperation,
                addFieldsOperation
        );

        List<DataResourceQueryOutput> result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.IMAGE_DATASET, DataResourceQueryOutput.class).getMappedResults();

        return new PageOutput<>(tableDataSetQueryInput.getPageIndex(), (long) total, tableDataSetQueryInput.getPageSize(), result);
    }


}
