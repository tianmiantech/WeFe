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
