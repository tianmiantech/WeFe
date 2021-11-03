/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.mongodb.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * @author yuxin.zhang
 **/
@Configuration
public class MongoConfig {

    @Value("${spring.datasource.mongodb.uri}")
    private String uri;
    @Value("${spring.datasource.mongodb.databaseName}")
    private String databaseName;

    @Bean
    public MongoClient mongoUnionClient() {
        return new MongoClient(new MongoClientURI(uri));
    }

    @Bean
    public MongoClient mongoManagerClient() {
        return new MongoClient(new MongoClientURI(uri));
    }

    @Bean
    public MongoDbFactory mongoDbFactory(MongoClient mongoUnionClient) {
        return new SimpleMongoDbFactory(mongoUnionClient, databaseName);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDbFactory mongoDbFactory) {
        return new MongoTransactionManager(mongoDbFactory);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory, getConverter(mongoDbFactory));
    }

    @Bean
    public GridFsTemplate gridFsTemplate(MongoDbFactory mongoDbFactory) {
        return new GridFsTemplate(mongoDbFactory, getConverter(mongoDbFactory));
    }

    @Bean
    public GridFSBucket getGridFSBucket(MongoClient mongoUnionClient){
        MongoDatabase database = mongoUnionClient.getDatabase(databaseName);
        GridFSBucket bucket = GridFSBuckets.create(database);
        return bucket;
    }

    private MappingMongoConverter getConverter(MongoDbFactory mongoDbFactory) {
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
