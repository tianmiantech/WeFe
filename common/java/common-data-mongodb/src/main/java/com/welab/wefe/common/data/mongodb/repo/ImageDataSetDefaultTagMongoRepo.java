package com.welab.wefe.common.data.mongodb.repo;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import org.springframework.stereotype.Repository;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/25
 */
@Repository
public class ImageDataSetDefaultTagMongoRepo extends AbstractDataSetDefaultTagMongoRepo{
    @Override
    protected String getTableName() {
        return MongodbTable.Union.IMAGE_DATA_SET_DEFAULT_TAG;
    }

}
