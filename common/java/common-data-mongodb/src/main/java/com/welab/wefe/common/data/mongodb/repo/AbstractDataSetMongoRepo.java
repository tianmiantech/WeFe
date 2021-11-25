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

import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetTagsQueryOutput;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
public abstract class AbstractDataSetMongoRepo extends AbstractMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    protected abstract String getTableName();

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

        List<DataSetTagsQueryOutput> result = mongoUnionTemplate.aggregate(aggregation, getTableName(), DataSetTagsQueryOutput.class).getMappedResults();

        return result;
    }

}
