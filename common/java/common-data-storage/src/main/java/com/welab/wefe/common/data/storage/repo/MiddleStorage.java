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

import java.util.List;

/**
 * @author ivenn.zheng
 */
public abstract class MiddleStorage extends Storage {

    @Override
    public void put(String dbName, String tbName, DataItemModel model) {

    }

    @Override
    public <K, V> void putAll(String dbName, String tbName, List<DataItemModel<K, V>> list) throws Exception {

    }

    @Override
    public DataItemModel get(String dbName, String tbName, String key) throws Exception {
        return null;
    }

    @Override
    public List<DataItemModel> collect(String dbName, String tbName) throws Exception {
        return null;
    }

    @Override
    public List<DataItemModel<byte[], byte[]>> collectBytes(String dbName, String tbName) throws Exception {
        return null;
    }

    @Override
    public void delete(String dbName, String tbName, String key) throws Exception {

    }

    @Override
    public int count(String dbName, String tbName) throws Exception {
        return 0;
    }

    @Override
    public List<DataItemModel> take(String dbName, String tbName, int size) throws Exception {
        return null;
    }

    @Override
    public PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) throws Exception {
        return null;
    }

    @Override
    public PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) throws Exception {
        return null;
    }

    @Override
    public void dropTB(String dbName, String tbName) throws Exception {

    }

    @Override
    public void dropDB(String dbName) throws Exception {

    }

    @Override
    public int getAddBatchSize(int columnCount) {
        return 0;
    }

    @Override
    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        return 0;
    }
}
