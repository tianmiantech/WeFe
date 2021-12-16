package com.welab.wefe.board.service.service.fusion;

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


import com.alibaba.fastjson.JSON;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Service
public class FusionResultStorageService  extends AbstractService {

    public static final String DATABASE_NAME = Constant.DBName.WEFE_DATA;

    @Autowired
    StorageService storageService;

    /**
     * Determine whether the specified key exists
     */
    public boolean containsKey(String dataSetId, String key) {
        String table = createRawDataSetTableName(dataSetId);
        boolean contains = storageService.getByKey(DATABASE_NAME, table, key) != null;
        return contains;
    }

    /**
     * remove data set from storage
     */
    public void deleteDataSet(String dataSetId) {
        String table = createRawDataSetTableName(dataSetId);
        storageService.dropTB(DATABASE_NAME, table);
    }

    /**
     * save data set header info to storage
     */
    public void saveHeaderRow(String businessId, List<String> row) {
        String sid = null;
        List<String> header = new ArrayList<>();

        for (String item : row) {
            if (sid == null) {
                sid = String.valueOf(item);
            } else {
                header.add(String.valueOf(item));
            }
        }

        String tableName = createRawDataSetTableName(businessId) + ".meta";

        // According to the convention,
        // sid needs to be converted to json string so that double quotation marks are added before and after.
        sid = JSON.toJSONString(sid);
        save(tableName, "sid", sid);

        // According to the convention,
        // the header needs to be converted to json string
        // so that double quotation marks are added before and after it.
        String headerRow = JSON.toJSONString(StringUtil.join(header, ","));
        save(tableName, "header", headerRow);

    }

    /**
     * save data row to storage
     */
    public void saveDataRow(String dataSetId, Collection<Object> values) {
        save(createRawDataSetTableName(dataSetId), buildDataItemModel(values));
    }

    /**
     * save data rows to storage
     */
    public void saveDataRows(String dataSetId, List<List<Object>> rows) {

        List<DataItemModel<String, String>> list = rows
                .stream()
                .map(x -> buildDataItemModel(x))
                .collect(Collectors.toList());

        saveList(createRawDataSetTableName(dataSetId), list);
    }

    /**
     * Convert the data rows in the dataset to DataItemModel
     */
    private DataItemModel<String, String> buildDataItemModel(Collection<Object> values) {
        String key = null;
        List<String> list = new ArrayList<>();

        for (Object item : values) {
            if (key == null) {
                key = String.valueOf(item);
            } else {
                list.add(String.valueOf(item));
            }
        }
        return new DataItemModel<>(key, StringUtil.join(list, ","));
    }


    /**
     * save a record to storage
     */
    private void save(String tableName, String key, String value) {
        storageService.save(DATABASE_NAME, tableName, new DataItemModel<>(key, value));
    }

    /**
     * save a record to storage
     */
    private void save(String tableName, DataItemModel item) {
        storageService.save(DATABASE_NAME, tableName, item);
    }

    /**
     * save multi records to storage
     */
    public <K, V> void saveList(String tableName, List<DataItemModel<K, V>> list) {
        storageService.saveList(DATABASE_NAME, tableName, list);
    }

    /**
     * Generate the raw data set table name
     */
    public String createRawDataSetTableName(String fruitId) {
        return "fusion_result_" + fruitId;
    }

    /**
     * Get row count of table
     */
    public int count(String tableName) {
        return storageService.count(DATABASE_NAME, tableName);
    }

    /**
     * Get row count of table
     */
    public int count(String databaseName, String tableName) {
        return storageService.count(databaseName, tableName);
    }

    /**
     * Calculate the appropriate batch size based on the number of columns in the data set
     */
    public int getAddBatchSize(int columns) {
        return storageService.getAddBatchSize(columns);
    }

    public DataItemModel getByKey(String databaseName, String tableName, String key) {
        return storageService.getByKey(databaseName, tableName, key);
    }
}
