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

package com.welab.wefe.data.fusion.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Storage service read and write classes
 *
 * @author Jacky.jiang
 */
@Service
public class DataStorageService extends AbstractService {

    @Value("${db.mysql.database}")
    private String DB_NAME;


    @Autowired
    FusionStorageService storageService;

    /**
     * Create a table
     */
    public void createTable(String tableName, List<String> rows) {
        if (rows == null || rows.size() == 0) {
            return;
        }
        storageService.createTable(DB_NAME, tableName, rows);
    }


    /**
     * Insert a piece of data
     */
    public void insert(String tableName, Map<String, Object> data) {
        if (data == null) {
            return;
        }
        storageService.insert(DB_NAME, tableName, data);
    }


    /**
     * Batch save rows of data
     */
    public void saveDataRows(String tableName, List<Map<String, Object>> rows) {
        saveList(tableName, rows);
    }

    /**
     * Batch write
     */
    public <K, V> void saveList(String tableName, List<Map<String, Object>> rows) {
        storageService.saveList(DB_NAME, tableName, rows);
    }

    /**
     * Get the number
     */
    public int count(String tbName) throws Exception {
        return storageService.count(DB_NAME, tbName);
    }

}
