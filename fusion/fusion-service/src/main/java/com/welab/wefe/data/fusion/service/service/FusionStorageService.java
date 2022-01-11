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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.data.fusion.service.enums.DBType;
import com.welab.wefe.data.fusion.service.repo.Storage;
import com.welab.wefe.data.fusion.service.repo.impl.MysqlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * @author jacky.jiang
 */
@Service
public class FusionStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(FusionStorageService.class);

    @Autowired
    private MysqlStorage mysqlStorage;

    @Value(value = "${db.storage.type}")
    private DBType dbType;

    public Storage getStorage() {
        Storage result = null;
        switch (dbType) {
            case MYSQL_FUSION:
                result = mysqlStorage;
                break;
            default:
                break;
        }
        return result;
    }

    public void createTable(String dbName, String tbName, List<String> rows) {
        try {
            getStorage().createTable(dbName, tbName, rows);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Insert single data
     *
     * @param dbName
     * @param tbName
     * @param data
     */
    public void insert(String dbName, String tbName, Map<String, Object> data) {
        try {
            getStorage().insert(dbName, tbName, data);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * saveList
     *
     * @param dbName
     * @param tbName
     * @param rows
     */
    public void saveList(String dbName, String tbName, List<Map<String, Object>> rows) {
        try {
            getStorage().putAll(dbName, tbName, rows);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public int count(String dbName, String tbName) throws Exception {
        return getStorage().count(dbName, tbName);
    }
//
//    public <K, V> void saveList(List<DataItemModel<K, V>> data, Map<String, Object> args) {
//        try {
//            getStorage().putAll(data, args);
//        } catch (Exception e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }


}
