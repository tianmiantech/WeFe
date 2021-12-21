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

package com.welab.wefe.common.data.storage.repo;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author ivenn.zheng
 */
public abstract class Storage {

    // =================================  AbstractStorage  start ================================

    /**
     * put the single DataItemModel Object
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract void put(String dbName, String tbName, DataItemModel model) throws Exception;

    /**
     * put all the DataItemModel List
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract <K, V> void putAll(String dbName, String tbName, List<DataItemModel<K, V>> list) throws Exception;

    /**
     * get DataItemModel Object by database name, table name and key
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract DataItemModel get(String dbName, String tbName, String key) throws Exception;

    /**
     * collect DataItemModel List by database name and table name
     *
     * @param dbName database name
     * @param tbName table name
     * @return List<DataItemModel>
     */
    public abstract List<DataItemModel> collect(String dbName, String tbName) throws Exception;

    /**
     * collect DataItemModel<byte[], byte[]> List by database name and table name
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract List<DataItemModel<byte[], byte[]>> collectBytes(String dbName, String tbName) throws Exception;

    /**
     * delete data by database name, table name and key
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract void delete(String dbName, String tbName, String key) throws Exception;

    /**
     * count data by database name and table name
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract int count(String dbName, String tbName) throws Exception;

    /**
     * take size of DataItemModel
     *
     * @param dbName database name
     * @param tbName table name
     * @return List<DataItemModel>
     */
    public abstract List<DataItemModel> take(String dbName, String tbName, int size) throws Exception;

    /**
     * get List<DataItemModel<K, V>> data by page info
     *
     * @param dbName         database name
     * @param tbName         table name
     * @param pageInputModel page info
     * @return PageOutputModel contains List<DataItemModel<K, V>> data
     */
    public abstract PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) throws Exception;


    /**
     * get List<DataItemModel<byte[], byte[]>> data by page info
     *
     * @param dbName         database name
     * @param tbName         table name
     * @param pageInputModel page info
     */
    public abstract PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) throws Exception;

    /**
     * drop table by database name and table name
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract void dropTB(String dbName, String tbName) throws Exception;

    /**
     * drop database
     */
    public abstract void dropDB(String dbName) throws Exception;

    /**
     * get batch size
     */
    public abstract int getAddBatchSize(int columnCount);


    /**
     * get count by byte size(only use in clickhouse)
     *
     * @param dbName database name
     * @param tbName table name
     */
    public abstract int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception;

    // =================================  AbstractStorage  end ================================

    // =================================  MiddleStorage  start ================================

    /**
     * put all data to cloud storage
     *
     * @param list data list
     */
    public abstract <K, V> void putAll(List<DataItemModel<K, V>> list, Map<String, Object> args) throws Exception;

    // =================================  MiddleStorage  end ================================


    /**
     * Check and create table
     */
    public abstract boolean isExists(String dbName, String tbName) throws SQLException;
}
