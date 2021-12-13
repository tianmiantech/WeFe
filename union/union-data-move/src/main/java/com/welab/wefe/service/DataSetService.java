package com.welab.wefe.service;

import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author aaron.li
 * @date 2021/12/13 16:49
 **/
@Service
public class DataSetService {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    public List<DataSet> findAll() {
        return mongoUnionTemplate.findAll(DataSet.class);
    }


}
