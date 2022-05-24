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
package com.welab.wefe.common.data.storage.zane.persistent;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.zane.persistent.clickhouse.ClickhouseConfig;
import com.welab.wefe.common.data.storage.zane.persistent.clickhouse.ClickhouseStorage;
import com.welab.wefe.common.data.storage.zane.persistent.mysql.MysqlConfig;
import com.welab.wefe.common.data.storage.zane.persistent.mysql.MysqlStorage;

import java.util.List;
import java.util.Map;

/**
 * 持久化存储（persistent storage）：默认以 clickhouse 实现，用于持久化储存数据集。
 *
 * @author zane
 * @date 2022/5/24
 */
public abstract class PersistentStorage {

    // region abstract method

    public abstract void save(String dbName, String tbName, DataItemModel model);

    public abstract <K, V> void saveList(String dbName, String tbName, List<DataItemModel<K, V>> data);

    public abstract <K, V> void saveList(List<DataItemModel<K, V>> data, Map<String, Object> args);

    public abstract DataItemModel getByKey(String dbName, String tbName, String key);

    public abstract List<DataItemModel> getList(String dbName, String tbName);

    public abstract List<DataItemModel<byte[], byte[]>> getListBytes(String dbName, String tbName);

    public abstract PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel);

    public abstract PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel);

    public abstract int count(String dbName, String tbName);

    public abstract void dropTB(String dbName, String tbName);

    public abstract void dropDB(String dbName);

    public abstract int getAddBatchSize(int columnCount);

    public abstract int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception;

    public abstract boolean isExists(String dbName, String tbName);

    // endregion

    private static PersistentStorage storage;

    protected PersistentStorage() {
    }

    /**
     * 初始化对象
     * <p>
     * 当配置信息变化时，重新初始化即可刷新对象。
     */
    public synchronized static void init(ClickhouseConfig config) {
        storage = new ClickhouseStorage(config);
    }

    public synchronized static void init(MysqlConfig config) {
        storage = new MysqlStorage(config);
    }

    public static PersistentStorage getInstance() {
        return storage;
    }

    public static void main(String[] args) {
        PersistentStorage.init(new ClickhouseConfig());
        PersistentStorage storage = PersistentStorage.getInstance();
        PersistentStorage.init(new MysqlConfig());
        storage.getList("", "");
    }
}
