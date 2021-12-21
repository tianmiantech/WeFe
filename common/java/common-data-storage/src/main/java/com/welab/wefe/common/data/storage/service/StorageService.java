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

package com.welab.wefe.common.data.storage.service;

import com.welab.wefe.common.data.storage.StorageManager;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.repo.Storage;
import com.welab.wefe.common.data.storage.repo.impl.ClickhouseStorage;
import com.welab.wefe.common.data.storage.repo.impl.FcStorage;
import com.welab.wefe.common.data.storage.repo.impl.LmdbStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.clickhouse.except.ClickHouseException;

import java.util.List;
import java.util.Map;


/**
 * @author yuxin.zhang
 */
@Service
public class StorageService {
    private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

    @Autowired
    private ClickhouseStorage clickhouseStorage;
    @Autowired
    private LmdbStorage lmdbStorage;
    @Autowired
    private FcStorage fcStorage;

    @Value(value = "${db.storage.type}")
    private DBType dbType;

    public Storage getStorage() {
        Storage result = null;
        switch (dbType) {
            case CLICKHOUSE:
                result = clickhouseStorage;
                break;
            case MYSQL:
                //TODO mysql storage to be implemented
                break;
            case LMDB:
                result = lmdbStorage;
                break;
            case OSS:
            case OTS:
                result = fcStorage;
                break;
            default:
        }
        return result;
    }

    public void save(String dbName, String tbName, DataItemModel model) {
        try {
            getStorage().put(dbName, tbName, model);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public <K, V> void saveList(String dbName, String tbName, List<DataItemModel<K, V>> data) {
        try {
            getStorage().putAll(dbName, tbName, data);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public <K, V> void saveList(List<DataItemModel<K, V>> data, Map<String, Object> args) {
        try {
            fcStorage.putAll(data, args);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public DataItemModel getByKey(String dbName, String tbName, String key) {
        DataItemModel result = null;
        try {
            result = getStorage().get(dbName, tbName, key);
        } catch (Exception e) {
            // Table does not exist
            if (e instanceof ClickHouseException && e.getMessage().contains("doesn't exist")) {
                return null;
            }
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    public List<DataItemModel> getList(String dbName, String tbName) {
        List<DataItemModel> result = null;
        try {
            result = getStorage().collect(dbName, tbName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    public List<DataItemModel<byte[], byte[]>> getListBytes(String dbName, String tbName) {
        List<DataItemModel<byte[], byte[]>> result = null;
        try {
            result = clickhouseStorage.collectBytes(dbName, tbName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) {
        PageOutputModel pageOutputModel = new PageOutputModel();
        try {
            pageOutputModel = getStorage().getPage(dbName, tbName, pageInputModel);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return pageOutputModel;
    }

    public PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) {
        PageOutputModel<byte[], byte[]> pageOutputModel = null;
        try {
            pageOutputModel = getStorage().getPageBytes(dbName, tbName, pageInputModel);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return pageOutputModel;
    }

    public int count(String dbName, String tbName) {
        int result = 0;
        try {
            result = getStorage().count(dbName, tbName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return result;
    }

    public void dropTB(String dbName, String tbName) {
        try {

            getStorage().dropTB(dbName, tbName);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void dropDB(String dbName) {
        try {
            getStorage().dropDB(dbName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public int getAddBatchSize(int columnCount) {
        return getStorage().getAddBatchSize(columnCount);
    }


    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        return getStorage().getCountByByteSize(dbName, tbName, byteSize);
    }

    public boolean isExists(String dbName, String tbName) {
        try {
            return getStorage().isExists(dbName, tbName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }
}
