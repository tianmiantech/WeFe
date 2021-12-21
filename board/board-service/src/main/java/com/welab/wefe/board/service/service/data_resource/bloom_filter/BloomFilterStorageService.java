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

package com.welab.wefe.board.service.service.data_resource.bloom_filter;

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
 * bloom_filter storage service read and write class
 * <p>
 *
 * @author jacky.jiang
 */
@Service
public class BloomFilterStorageService extends AbstractService {
    public static final String DATABASE_NAME = Constant.DBName.WEFE_DATA;

    @Autowired
    StorageService storageService;

    /**
     * Determine whether the specified key exists
     */
    public boolean containsKey(String dataSetId, String key) {
        String table = createRawBloomfilterTableName(dataSetId);
        boolean contains = storageService.getByKey(DATABASE_NAME, table, key) != null;
        return contains;
    }

    /**
     * remove bloom_filter from storage
     */
    public void deleteBloomfilter(String bloomfilterId) {
        String table = createRawBloomfilterTableName(bloomfilterId);
        storageService.dropTB(DATABASE_NAME, table);
    }

    /**
     * save data set header info to storage
     */
    public void saveHeaderRow(String dataSetId, List<String> row) {
        String sid = null;
        List<String> header = new ArrayList<>();

        for (String item : row) {
            if (sid == null) {
                sid = String.valueOf(item);
            } else {
                header.add(String.valueOf(item));
            }
        }

        String tableName = createRawBloomfilterTableName(dataSetId) + ".meta";

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
        save(createRawBloomfilterTableName(dataSetId), buildDataItemModel(values));
    }

    /**
     * save data rows to storage
     */
    public void saveDataRows(String bloomfilterId, List<List<Object>> rows) {

        List<DataItemModel<String, String>> list = rows
                .stream()
                .map(x -> buildDataItemModel(x))
                .collect(Collectors.toList());

        saveList(createRawBloomfilterTableName(bloomfilterId), list);
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
     * view the bloom_filter data rows
     */
    public List<List<String>> previewBloomfilter(String dbName, String tableName, int limit) {
        PageOutputModel<?, ?> page = storageService.getPage(dbName, tableName, new PageInputModel(0, limit));

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
     * read by pagination
     */
    public PageOutputModel getListByPage(String namespace, String tableName, PageInputModel inputModel) {
        return storageService.getPage(namespace, tableName, inputModel);
    }

    /**
     * real all record from storage table
     */
    public List<DataItemModel> getList(String tableName) {
        return storageService.getList(DATABASE_NAME, tableName);
    }

    /**
     * Generate the raw bloom_filter table name
     */
    public String createRawBloomfilterTableName(String bloomfilterId) {
        return "blommfilter_" + bloomfilterId;
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

}
