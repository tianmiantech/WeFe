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
import com.welab.wefe.common.data.mongodb.dto.dataset.*;
import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.entity.union.DataSetMemberPermission;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.ImageDataSetExtJSON;
import com.welab.wefe.common.data.mongodb.util.AddFieldsOperation;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import java.util.stream.Collectors;


/**
 * @author yuxin.zhang
 */
@Repository
public class ImageDataSetMongoReop extends AbstractDataSetMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    @Override
    protected String getDbName() {
        return MongodbTable.Union.IMAGE_DATASET;
    }

    public boolean deleteByDataSetId(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, ImageDataSet.class);
        return updateResult.wasAcknowledged();
    }

    public boolean existsByDataSetId(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).notRemoved().build();
        return mongoUnionTemplate.exists(query, ImageDataSet.class);
    }

    public ImageDataSet findDataSetId(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).notRemoved().build();
        return mongoUnionTemplate.findOne(query, ImageDataSet.class);
    }


    /**
     * Query the image data set visible to the current member
     */
    public PageOutput<ImageDataSetQueryOutput> find(ImageDataSetQueryInput imageDataSetQueryInput) {
        LookupOperation lookupToLots = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");

        Criteria dataSetCriteria = new QueryBuilder()
                .like("name", imageDataSetQueryInput.getName())
                .like("tags", imageDataSetQueryInput.getTag())
                .append("member_id", imageDataSetQueryInput.getMemberId())
                .append("data_set_id", imageDataSetQueryInput.getDataSetId())
                .append("enable", imageDataSetQueryInput.getEnable() != null ? (imageDataSetQueryInput.getEnable() ? 1 : 0) : null)
                .append("status", imageDataSetQueryInput.getStatus() != null ? (imageDataSetQueryInput.getStatus() ? 1 : 0) : null)
                .getCriteria();


        AggregationOperation dataSetMatch = Aggregation.match(dataSetCriteria);

        Criteria memberCriteria = new QueryBuilder()
                .like("name", imageDataSetQueryInput.getMemberName())
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);
        UnwindOperation unwind = Aggregation.unwind("member");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(dataSetMatch, memberMatch, lookupToLots, unwind, addFieldsOperation);
        int total = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.IMAGE_DATASET, DataSetQueryOutput.class).getMappedResults().size();

        SkipOperation skipOperation = Aggregation.skip((long) imageDataSetQueryInput.getPageIndex() * imageDataSetQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(imageDataSetQueryInput.getPageSize());
        aggregation = Aggregation.newAggregation(dataSetMatch, memberMatch, lookupToLots, unwind, skipOperation, limitOperation, addFieldsOperation);

        List<ImageDataSetQueryOutput> result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.IMAGE_DATASET, ImageDataSetQueryOutput.class).getMappedResults();

        return new PageOutput<>(imageDataSetQueryInput.getPageIndex(), (long) total, imageDataSetQueryInput.getPageSize(), result);
    }


    /**
     * Query the image data set visible to the current member
     */
    public PageOutput<ImageDataSetQueryOutput> findCurMemberCanSee(ImageDataSetQueryInput imageDataSetQueryInput) {
        LookupOperation lookupToLots = LookupOperation.newLookup().
                from(MongodbTable.Union.MEMBER).
                localField("member_id").
                foreignField("member_id").
                as("member");


        Criteria dataSetCriteria = new QueryBuilder()
                .notRemoved()
                .like("name", imageDataSetQueryInput.getName())
                .like("tags", imageDataSetQueryInput.getTag())
                .append("member_id", imageDataSetQueryInput.getMemberId())
                .append("data_set_id", imageDataSetQueryInput.getDataSetId())
                .getCriteria();

        Criteria or = new Criteria();
        or.orOperator(
                new QueryBuilder().append("public_level", "Public").getCriteria(),
                new QueryBuilder().like("public_member_list", imageDataSetQueryInput.getCurMemberId()).getCriteria()
        );
        dataSetCriteria.andOperator(or);
        AggregationOperation dataSetMatch = Aggregation.match(dataSetCriteria);

        Criteria memberCriteria = new QueryBuilder()
                .like("name", imageDataSetQueryInput.getMemberName())
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);
        UnwindOperation unwind = Aggregation.unwind("member");
        Map<String, Object> addfieldsMap = new HashMap<>();
        addfieldsMap.put("member_name", "$member.name");

        AddFieldsOperation addFieldsOperation = new AddFieldsOperation(addfieldsMap);

        Aggregation aggregation = Aggregation.newAggregation(dataSetMatch, memberMatch, lookupToLots, unwind, addFieldsOperation);
        int total = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.IMAGE_DATASET, DataSetQueryOutput.class).getMappedResults().size();

        SkipOperation skipOperation = Aggregation.skip((long) imageDataSetQueryInput.getPageIndex() * imageDataSetQueryInput.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(imageDataSetQueryInput.getPageSize());
        aggregation = Aggregation.newAggregation(dataSetMatch, memberMatch, lookupToLots, unwind, skipOperation, limitOperation, addFieldsOperation);

        List<ImageDataSetQueryOutput> result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.IMAGE_DATASET, ImageDataSetQueryOutput.class).getMappedResults();

        return new PageOutput<>(imageDataSetQueryInput.getPageIndex(), (long) total, imageDataSetQueryInput.getPageSize(), result);
    }


    public void upsert(ImageDataSet dataSet) {
        ImageDataSet imageDataSet = findDataSetId(dataSet.getDataSetId());
        if (imageDataSet != null) {
            dataSet.setId(imageDataSet.getId());
            dataSet.setExtJson(imageDataSet.getExtJson());
        }
        mongoUnionTemplate.save(dataSet);
    }

    public boolean updateExtJSONById(String dataSetId, ImageDataSetExtJSON extJSON,String updateTime) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update update = new UpdateBuilder()
                .append("extJson", extJSON)
                .append("updated_time", updateTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, DataSet.class);
        return updateResult.wasAcknowledged();
    }


    public boolean updateEnable(String dataSetId,String enable,String updatedTime) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update udpate = new UpdateBuilder()
                .append("enable", enable)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, ImageDataSet.class);
        return updateResult.wasAcknowledged();
    }


    public boolean updateLabeledCount(String dataSetId,String labeledCount,String completed,String updatedTime) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update udpate = new UpdateBuilder()
                .append("labeledCount", labeledCount)
                .append("completed", completed)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, ImageDataSet.class);
        return updateResult.wasAcknowledged();
    }
}
