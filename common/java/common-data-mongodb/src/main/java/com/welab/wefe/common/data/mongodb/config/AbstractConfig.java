package com.welab.wefe.common.data.mongodb.config;

import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/9
 */
public class AbstractConfig {
    protected MappingMongoConverter getConverter(MongoDbFactory mongoDbFactory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MongoMappingContext mongoMappingContext = new MongoMappingContext();
        //Underscore to hump
        mongoMappingContext.setFieldNamingStrategy(new SnakeCaseFieldNamingStrategy());
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        // When the model is saved with mongoTemplate, a _class field will be added by default, which affects the vision, so we need to remove it here
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return mappingConverter;
    }
}
