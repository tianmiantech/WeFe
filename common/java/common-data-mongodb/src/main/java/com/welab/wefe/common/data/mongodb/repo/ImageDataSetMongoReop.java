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
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
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
public class ImageDataSetMongoReop extends AbstractDataSetMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    @Override
    protected String getTableName() {
        return MongodbTable.Union.IMAGE_DATASET;
    }


    public boolean existsByDataSetId(String dataResouceId) {
        if (StringUtils.isEmpty(dataResouceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResouceId", dataResouceId).build();
        return mongoUnionTemplate.exists(query, ImageDataSet.class);
    }

    public ImageDataSet findByDataResourceId(String dataResouceId) {
        if (StringUtils.isEmpty(dataResouceId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResouceId", dataResouceId).build();
        return mongoUnionTemplate.findOne(query, ImageDataSet.class);
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

        String forJobType = dataResourceQueryInput.getForJobType() == null ? null : dataResourceQueryInput.getForJobType().name();
        Criteria imageDataSetCriteria = new QueryBuilder()
                .append("image_data_set.for_job_type", forJobType)
                .getCriteria();

        AggregationOperation memberMatch = Aggregation.match(memberCriteria);

        AggregationOperation imageDataSetMatch = Aggregation.match(imageDataSetCriteria);
        UnwindOperation unwind = Aggregation.unwind("member");
        UnwindOperation unwindImageDataSet = Aggregation.unwind("image_data_set");
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
                unwind,
                unwindImageDataSet,
                addFieldsOperation,
                dataResourceMatch,
                memberMatch,
                imageDataSetMatch,
                skipOperation,
                limitOperation,
                sortOperation
        ).as("data").and(
                lookupToDataImageDataSet,
                lookupToMember,
                unwind,
                unwindImageDataSet,
                dataResourceMatch,
                memberMatch,
                imageDataSetMatch,
                countOperation
        ).as("total");

        Aggregation aggregation = Aggregation.newAggregation(facetOperation);
        JObject result = mongoUnionTemplate.aggregate(aggregation, MongodbTable.Union.DATA_RESOURCE, JObject.class).getUniqueMappedResult();
        Long total = 0L;
        List<DataResourceQueryOutput> list = result.getJSONList("data",DataResourceQueryOutput.class);
        if(list != null && !list.isEmpty()){
            total = result.getJSONList("total",JObject.class).get(0).getLongValue("count");
        }
        return new PageOutput<>(dataResourceQueryInput.getPageIndex(), total, dataResourceQueryInput.getPageSize(), list);
    }


    public void upsert(ImageDataSet imageDataSet) {
        mongoUnionTemplate.save(imageDataSet);
    }


    public void deleteByDataResourceId(String dataResourceId) {
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        mongoUnionTemplate.updateFirst(query, udpate, ImageDataSet.class);
    }
}
