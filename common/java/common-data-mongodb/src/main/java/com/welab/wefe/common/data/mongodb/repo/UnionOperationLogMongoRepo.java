package com.welab.wefe.common.data.mongodb.repo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UnionOperationLogMongoRepo extends AbstractOperationLogMongoRepo{

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }
}
