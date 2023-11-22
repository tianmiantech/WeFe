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

package com.welab.wefe.data.fusion.service.service.dataset;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.service.DataStorageService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Data sets store service read and write classes
 *
 * @author Jacky.jiang
 */
public class DataSetStorageHelper {

    private final static String DATA_SET_TABLE_PRE = "data_fusion_";

    private static DataStorageService dataStorageService;


    private static Progress process;

    static {
        dataStorageService = Launcher.CONTEXT.getBean(DataStorageService.class);
    }
    /**
     * Create a dataSet table
     */
    public static void createDataSetTable(String dataSetId, List<String> rows) {
        String tableName = createRawDataSetTableName(dataSetId);
        dataStorageService.createTable(tableName, rows);
    }


    /**
     * Insert a dataSet data
     */
    public static void insertDataSet(String dataSetId, Map<String, Object> data) {
        String tableName = createRawDataSetTableName(dataSetId);
        dataStorageService.insert(tableName, data);
    }


    /**
     * Generate the original dataset table name
     */
    public static String createRawDataSetTableName(String dataSetId) {
        return DATA_SET_TABLE_PRE + dataSetId;

    }

    /**
     * Batch save rows of data
     */
    public static void saveDataSetRows(DataSetMySqlModel model, List<Map<String, Object>> rows) {
        System.out.println(rows);
        DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
        dataSetRepository.updateById(model.getId(), "updatedTime", new Date(), DataSetMySqlModel.class);
        dataStorageService.saveDataRows(createRawDataSetTableName(model.getId()), rows);
    }

    /**
     * Statistics row
     */
    public static int countDataSetRows(DataSetMySqlModel model) throws Exception {
        return dataStorageService.count(createRawDataSetTableName(model.getId()));
    }

}
