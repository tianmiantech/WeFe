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

package com.welab.wefe.data.fusion.service.manager;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.service.DataStorageService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class TaskResultManager {

    private static DataStorageService dataStorageService;

    private final static String TASK_RESULT_TABLE_PRE = "task_result_";


    static {
        dataStorageService = Launcher.CONTEXT.getBean(DataStorageService.class);
    }

    /**
     * Create the TaskResult table
     */
    public static void createTaskResultTable(String businessId, String rows) {
        String tableName = createRawTaskResultTableName(businessId);
        dataStorageService.createTable(tableName, Arrays.asList(rows));
    }

    /**
     * Create the TaskResult table
     */
    public static void createTaskResultTable(String businessId, List<String> rows) {
        String tableName = createRawTaskResultTableName(businessId);
        dataStorageService.createTable(tableName, rows);
    }


    /**
     * Insert a dataSet data
     */
    public static void insertTaskResult(String businessId, LinkedHashMap<String, Object> data) {
        String tableName = createRawTaskResultTableName(businessId);
        dataStorageService.insert(tableName, data);
    }


    /**
     * Generate the original dataset table name
     */
    public static String createRawTaskResultTableName(String businessId) {
        return TASK_RESULT_TABLE_PRE + businessId;

    }

    /**
     * Batch save rows of data
     */
    public static void saveTaskResultRows(String businessId, List<Map<String, Object>> rows) {
        dataStorageService.saveDataRows(createRawTaskResultTableName(businessId), rows);
    }
}
