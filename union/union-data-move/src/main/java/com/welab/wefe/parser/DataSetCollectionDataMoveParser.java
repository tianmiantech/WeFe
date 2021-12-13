package com.welab.wefe.parser;

import com.mongodb.MongoClient;
import com.welab.wefe.dto.EventDto;
import com.welab.wefe.service.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author aaron.li
 * @date 2021/12/13 14:27
 **/
@Component
public class DataSetCollectionDataMoveParser extends AbstractDataMoveParser {
    @Autowired
    private DataSetService dataSetService;

    @Override
    public boolean fullSync() {

        return false;
    }

    @Override
    protected void insert(EventDto eventDto) {

    }

    @Override
    protected void update(EventDto eventDto) {

    }

    @Override
    protected void delete(EventDto eventDto) {

    }
}
