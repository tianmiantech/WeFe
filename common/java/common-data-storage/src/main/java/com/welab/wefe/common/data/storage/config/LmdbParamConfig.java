/*
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

package com.welab.wefe.common.data.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


/**
 * @author lonnie
 */
@Component
@PropertySource(value = {"file:${config.path}"}, encoding = "utf-8")
public class LmdbParamConfig {

    @Value(value = "${lmdb.database.name:lmdbtest}")
    private String dbName;

    @Value(value = "${lmdb.max.size:256}")
    private int lmdbMaxSize;

    @Value(value = "${lmdb.database.count:10}")
    private int lmdbDatabaseCount;

    @Value(value = "${lmdb.path:null}")
    private String lmdbPath;

    @Value(value = "${lmdb.partitions:1}")
    private int partitions;

    public int getLmdbMaxSize() {
        return lmdbMaxSize;
    }

    public void setLmdbMaxSize(int lmdbMaxSize) {
        this.lmdbMaxSize = lmdbMaxSize;
    }

    public int getLmdbDatabaseCount() {
        return lmdbDatabaseCount;
    }

    public void setLmdbDatabaseCount(int lmdbDatabaseCount) {
        this.lmdbDatabaseCount = lmdbDatabaseCount;
    }

    public String getLmdbPath() {
        return lmdbPath;
    }

    public void setLmdbPath(String lmdbPath) {
        this.lmdbPath = lmdbPath;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }
}
