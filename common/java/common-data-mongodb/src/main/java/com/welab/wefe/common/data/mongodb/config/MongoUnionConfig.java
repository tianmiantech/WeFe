/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.apache.commons.lang3.StringUtils;
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
public class MongoUnionConfig extends AbstractConfig{

    @Value("${spring.datasource.mongodb.union.uri}")
    private String unionUri;
    @Value("${spring.datasource.mongodb.union.databaseName}")
    private String unionDatabaseName;


    @Bean
    public MongoClient mongoUnionClient() {
        return new MongoClient(new MongoClientURI(unionUri));
    }


    @Bean
    public MongoDbFactory mongoDbUnionFactory(MongoClient mongoUnionClient) {
        return new SimpleMongoDbFactory(mongoUnionClient, unionDatabaseName);
    }


    @Bean
    public MongoTransactionManager transactionUnionManager(MongoDbFactory mongoDbUnionFactory) {
        return new MongoTransactionManager(mongoDbUnionFactory);
    }


    @Bean
    public MongoTemplate mongoUnionTemplate(MongoDbFactory mongoDbUnionFactory) {
        return new MongoTemplate(mongoDbUnionFactory, getConverter(mongoDbUnionFactory));
    }


    @Bean
    public GridFsTemplate gridFsTemplate(MongoDbFactory mongoDbUnionFactory) {
        return new GridFsTemplate(mongoDbUnionFactory, getConverter(mongoDbUnionFactory));
    }

    @Bean
    public GridFSBucket getGridFSBucket(MongoClient mongoUnionClient) {
        MongoDatabase database = mongoUnionClient.getDatabase(unionDatabaseName);
        GridFSBucket bucket = GridFSBuckets.create(database);
        return bucket;
    }

}
