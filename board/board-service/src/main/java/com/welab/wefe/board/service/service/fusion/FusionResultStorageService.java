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
package com.welab.wefe.board.service.service.fusion;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.util.StringUtil;

/**
 * @author hunter.zhao
 */
@Service
public class FusionResultStorageService extends AbstractService {

    public static final String DATABASE_NAME = Constant.DBName.WEFE_DATA;

    /**
     * Determine whether the specified key exists
     */
    public boolean containsKey(String dataSetId, String key) throws Exception {
        String table = createRawDataSetTableName(dataSetId);
        return PersistentStorage.getInstance().get(DATABASE_NAME, table, key) != null;
    }

    /**
     * remove data set from storage
     */
    public void deleteDataSet(String dataSetId) throws Exception {
        String table = createRawDataSetTableName(dataSetId);
        PersistentStorage.getInstance().dropTB(DATABASE_NAME, table);
    }

    /**
     * save data set header info to storage
     */
    public void saveHeaderRow(String businessId, Set<String> row) throws Exception {
        String sid = null;
        List<String> header = new ArrayList<>();

        for (String item : row) {
//            if (sid == null) {
//                sid = String.valueOf(item);
//            } else {
            header.add(String.valueOf(item));
//            }
        }

        String tableName = createRawDataSetTableName(businessId) + ".meta";

        // According to the convention,
        // sid needs to be converted to json string so that double quotation marks are added before and after.
//        sid = JSON.toJSONString(sid);
//        save(tableName, "sid", sid);

        // According to the convention,
        // the header needs to be converted to json string
        // so that double quotation marks are added before and after it.
        String headerRow = JSON.toJSONString(StringUtil.join(header, ","));
        save(tableName, "header", headerRow);

    }

    /**
     * save data row to storage
     */
    public void saveDataRow(String dataSetId, Collection<Object> values) throws Exception {
        save(createRawDataSetTableName(dataSetId), buildDataItemModel(values));
    }

    /**
     * save data rows to storage
     */
    public void saveDataRows(String dataSetId, List<List<Object>> rows) throws Exception {

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
    private void save(String tableName, String key, String value) throws Exception {
        PersistentStorage.getInstance().put(DATABASE_NAME, tableName, new DataItemModel<>(key, value));
    }

    /**
     * save a record to storage
     */
    private void save(String tableName, DataItemModel item) throws Exception {
        PersistentStorage.getInstance().put(DATABASE_NAME, tableName, item);
    }

    /**
     * save multi records to storage
     */
    public <K, V> void saveList(String tableName, List<DataItemModel<K, V>> list) throws Exception {
        PersistentStorage.getInstance().putAll(DATABASE_NAME, tableName, list);
    }

    /**
     * real all record from storage table
     */
    public List<DataItemModel> getList(String tableName) throws Exception {
        return PersistentStorage.getInstance().collect(DATABASE_NAME, tableName);
    }


    /**
     * Generate the raw data set table name
     */
    public String createRawDataSetTableName(String businessId) {
        return "fusion_result_" + businessId;
    }

    /**
     * Generate the raw data set table name
     */
    public String createRawDataSetHeaderTableName(String businessId) {
        return "fusion_result_" + businessId + ".meta";
    }

    /**
     * Get row count of table
     */
    public int count(String tableName) throws Exception {
        return PersistentStorage.getInstance().count(DATABASE_NAME, tableName);
    }

    /**
     * Get row count of table
     */
    public int count(String databaseName, String tableName) throws Exception {
        return PersistentStorage.getInstance().count(databaseName, tableName);
    }

    /**
     * Calculate the appropriate batch size based on the number of columns in the data set
     */
    public int getAddBatchSize(int columns) {
        return PersistentStorage.getInstance().getAddBatchSize(columns);
    }

    public DataItemModel getByKey(String databaseName, String tableName, String key) throws Exception {
        return PersistentStorage.getInstance().get(databaseName, tableName, key);
    }


    public Boolean isExists(String tableName) throws SQLException {
        return PersistentStorage.getInstance().isExists(DATABASE_NAME, tableName);
    }

    /**
     * view the data set data rows
     */
    public List<List<String>> previewDataSet(String tableName, int limit) throws Exception {
        return previewDataSet(DATABASE_NAME, tableName, limit);
    }

    /**
     * view the data set data rows
     */
    public List<List<String>> previewDataSet(String dbName, String tableName, int limit) throws Exception {
        PageOutputModel<?, ?> page = PersistentStorage.getInstance().getPage(dbName, tableName, new PageInputModel(0, limit));

        List<? extends DataItemModel<?, ?>> data = page.getData();
        return data
                .stream()
                .map(x -> {
                    List<String> list = new ArrayList<>();
                    list.add(String.valueOf(x.getK()));

                    Object value = x.getV();
                    if (value != null) {
                        for (String item : String.valueOf(value).split(",")) {
                            list.add(item);
                        }
                    }

                    return list;
                })
                .collect(Collectors.toList());
    }
}
