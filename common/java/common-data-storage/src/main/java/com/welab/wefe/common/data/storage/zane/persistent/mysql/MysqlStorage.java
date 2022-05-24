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
package com.welab.wefe.common.data.storage.zane.persistent.mysql;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.zane.persistent.PersistentStorage;

import java.util.List;
import java.util.Map;

/**
 * @author zane
 * @date 2022/5/24
 */
public class MysqlStorage extends PersistentStorage {

    private MysqlConfig config;

    public MysqlStorage(MysqlConfig config) {
        this.config = config;
    }

    @Override
    public void save(String dbName, String tbName, DataItemModel model) {

    }

    @Override
    public <K, V> void saveList(String dbName, String tbName, List<DataItemModel<K, V>> data) {

    }

    @Override
    public <K, V> void saveList(List<DataItemModel<K, V>> data, Map<String, Object> args) {

    }

    @Override
    public DataItemModel getByKey(String dbName, String tbName, String key) {
        return null;
    }

    @Override
    public List<DataItemModel> getList(String dbName, String tbName) {
        return null;
    }

    @Override
    public List<DataItemModel<byte[], byte[]>> getListBytes(String dbName, String tbName) {
        return null;
    }

    @Override
    public PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) {
        return null;
    }

    @Override
    public PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) {
        return null;
    }

    @Override
    public int count(String dbName, String tbName) {
        return 0;
    }

    @Override
    public void dropTB(String dbName, String tbName) {

    }

    @Override
    public void dropDB(String dbName) {

    }

    @Override
    public int getAddBatchSize(int columnCount) {
        return 0;
    }

    @Override
    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        return 0;
    }

    @Override
    public boolean isExists(String dbName, String tbName) {
        return false;
    }
}
