/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.mongodb.repo;

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractMongoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.regex.Pattern;

/**
 * @author yuxin.zhang
 **/
public abstract class AbstractMongoRepo<T extends AbstractMongoModel> {

    protected abstract MongoTemplate getMongoTemplate();

    public void save(T t) {
        getMongoTemplate().save(t);
    }


    public boolean upsert(Query query, Update update, Class<T> clazz) {
        UpdateResult updateResult = getMongoTemplate().upsert(query, update, clazz);
        return updateResult.wasAcknowledged();
    }


    /**
     * Ignore case for fuzzy queries
     */
    public Pattern getPattern(String string) {
        Pattern pattern = Pattern.compile("^.*" + string + ".*$", Pattern.CASE_INSENSITIVE);
        return pattern;
    }

}
