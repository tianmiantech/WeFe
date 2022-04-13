package com.welab.wefe.common.data.mongodb.repo;


import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.common.OperationLog;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class ManagerOperationLogMongoRepo extends AbstractOperationLogMongoRepo{

    @Autowired
    protected MongoTemplate mongoManagerTemplate;

    public PageOutput<OperationLog> findList(String apiName, String callerName, Date startTime, Date endTime, int pageIndex, int pageSize) {
        Query query = new QueryBuilder()
                .append("apiName", apiName)
                .append("callerName", callerName)
                .betweenByDate("requestTime",startTime,endTime)
                .page(pageIndex, pageSize)
                .build();
        List<OperationLog> list = mongoManagerTemplate.find(query, OperationLog.class);
        long count = mongoManagerTemplate.count(query, OperationLog.class);
        return new PageOutput<>(pageIndex, count, pageSize, list);
    }

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoManagerTemplate;
    }
}
