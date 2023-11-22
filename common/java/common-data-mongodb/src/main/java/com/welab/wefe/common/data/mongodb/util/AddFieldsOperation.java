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

package com.welab.wefe.common.data.mongodb.util;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

import java.util.Map;

/**
 * @author: yuxin.zhang
 * @date: 2021/9/27
 */
public class AddFieldsOperation implements AggregationOperation {
    private Map<String, Object> map;
    public AddFieldsOperation(Map<String, Object> map){
        this.map = map;
    }
    @Override
    public Document toDocument(AggregationOperationContext aggregationOperationContext) {
        return new Document("$addFields",new Document(map));
    }
}
