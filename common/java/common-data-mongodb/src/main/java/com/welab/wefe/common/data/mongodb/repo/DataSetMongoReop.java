/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetTagsQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.contract.data.DataSet;
import com.welab.wefe.common.data.mongodb.entity.contract.data.DataSetMemberPermission;
import com.welab.wefe.common.data.mongodb.util.AddFieldsOperation;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
public class DataSetMongoReop extends AbstractMongoRepo {

    @Autowired
    private DataSetMemberPermissionMongoRepo dataSetMemberPermissionMongoRepo;

    public boolean deleteByDataSetId(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, udpate, DataSet.class);
        return updateResult.wasAcknowledged();
    }


    public boolean existsByDataSetId(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).notRemoved().build();
        return mongoTemplate.exists(query, DataSet.class);
    }

    public DataSet findDataSetId(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).notRemoved().build();
        return mongoTemplate.findOne(query, DataSet.class);
    }


    /**
     * Query the data set visible to the current member
     */
    public PageOutput<DataSetQueryOutput> findCurMemberCanSee(DataSetQueryInput dataSetQueryInput) {
        LookupOperation lookupToLots = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");

        List<String> dataSetIds = null;
        List<DataSetMemberPermission> dataSetMemberPermissionList = dataSetMemberPermissionMongoRepo.findByMemberId(dataSetQueryInput.getCurMemberId());
        if (dataSetMemberPermissionList != null) {
            dataSetIds = dataSetMemberPermissionList.stream().map(DataSetMemberPermission::getDataSetId).collect(Collectors.toList());
        }

        Criteria dataSetCriteria = new QueryBuilder()
                .notRemoved()
                .like("name", dataSetQueryInput.getName())
                .like("tags", dataSetQueryInput.getTag())
                .append("member_id", dataSetQueryInput.getMemberId())
                .append("data_set_id", dataSetQueryInput.getDataSetId())
                .append("contains_y", dataSetQueryInput.getContainsY())
                .getCriteria();


        Criteria or = new Criteria();
        or.orOperator(
                new QueryBuilder().append("public_level", "Public").getCriteria(),
                new QueryBuilder().in("data_set_id", dataSetIds).getCriteria()
        );
        dataSetCriteria.andOperator(or);
        AggregationOperation dataSetMatch = Aggregation.match(dataSetCriteria);

        Criteria memberCriteria = new QueryBuilder()
                .like("name", dataSetQueryInput.getMemberName())
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);
        UnwindOperation unwind = Aggregation.unwind("member");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(dataSetMatch, memberMatch, lookupToLots, unwind, addFieldsOperation);
        int total = mongoTemplate.aggregate(aggregation, MongodbTable.Union.DATASET, DataSetQueryOutput.class).getMappedResults().size();

        SkipOperation skipOperation = Aggregation.skip((long) dataSetQueryInput.getPageIndex() * dataSetQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(dataSetQueryInput.getPageSize());
        aggregation = Aggregation.newAggregation(dataSetMatch, memberMatch, lookupToLots, unwind, skipOperation, limitOperation, addFieldsOperation);

        List<DataSetQueryOutput> result = mongoTemplate.aggregate(aggregation, MongodbTable.Union.DATASET, DataSetQueryOutput.class).getMappedResults();

        return new PageOutput<>(dataSetQueryInput.getPageIndex(), (long) total, dataSetQueryInput.getPageSize(), result);
    }


    public List<DataSetTagsQueryOutput> findByTags(String tagName) {
        Criteria criteria = new QueryBuilder()
                .like("tags", tagName)
                .getCriteria();
        AggregationOperation match = Aggregation.match(criteria);

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                Aggregation.group("tags").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Order.desc("count"))),
                Aggregation.project().and(Aggregation.previousOperation()).as("tags")

        );

        List<DataSetTagsQueryOutput> result = mongoTemplate.aggregate(aggregation, MongodbTable.Union.DATASET, DataSetTagsQueryOutput.class).getMappedResults();

        return result;
    }

    public void upsert(DataSet dataSet) {
        DataSet dbDataSet = findDataSetId(dataSet.getDataSetId());
        if (dbDataSet != null) {
            dataSet.setId(dbDataSet.getId());
        }
        mongoTemplate.save(dataSet);
    }
}
