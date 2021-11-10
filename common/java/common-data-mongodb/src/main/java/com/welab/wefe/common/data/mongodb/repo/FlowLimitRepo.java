package com.welab.wefe.common.data.mongodb.repo;

import com.welab.wefe.common.data.mongodb.entity.common.FlowLimit;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author aaron.li
 * @date 2021/10/22 17:41
 **/
@Repository
public class FlowLimitRepo extends AbstractMongoRepo {

    public FlowLimit findByKey(String key) {
        Query query = new QueryBuilder().append("key", key).build();
        return mongoTemplate.findOne(query, FlowLimit.class);
    }

    public void save(FlowLimit flowLimit) {
        mongoTemplate.save(flowLimit);
    }

    public void removeByKey(String key) {
        Query query = new QueryBuilder().append("key", key).build();
        mongoTemplate.remove(query, FlowLimit.class);
    }

    public List<FlowLimit> findAll() {
        return mongoTemplate.findAll(FlowLimit.class);
    }
}
