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

package com.welab.wefe.common.data.storage.config;

import org.springframework.util.Assert;


/**
 * @author lonnie
 */
public class LmdbConfig {

    private String dbName = "lmdbtest";

    private int lmdbMaxSize = 256;

    private int lmdbDatabaseCount = 10;

    private String lmdbPath = "/data/wefe/lmdb/";

    private int partitions = 1;

    public LmdbConfig(){};

    public LmdbConfig(String lmdbPath){
        Assert.notNull(lmdbPath, "lmdbPath == null");
        this.lmdbPath = lmdbPath;
    };

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
