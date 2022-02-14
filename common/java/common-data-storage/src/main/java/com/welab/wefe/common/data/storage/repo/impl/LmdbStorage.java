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

package com.welab.wefe.common.data.storage.repo.impl;

import com.welab.wefe.common.data.storage.config.LmdbParamConfig;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.repo.AbstractStorage;
import net.razorvine.pickle.Pickler;
import net.razorvine.pickle.Unpickler;
import org.apache.commons.collections4.CollectionUtils;
import org.lmdbjava.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.Env.create;

/**
 * @author lonnie
 */
@Component
public class LmdbStorage extends AbstractStorage {
    Pickler pickler = new Pickler();
    Unpickler unpickler = new Unpickler();

    private Logger log = LoggerFactory.getLogger(LmdbStorage.class);

    @Autowired
    private LmdbParamConfig lmdbParamConfig;

    @Override
    public void put(String dbName, String tbName, DataItemModel model) throws Exception {
        int p = hashKeyToPartition(model.getK().toString(), lmdbParamConfig.getPartitions());
        Env<byte[]> env = getEnv(dbName, tbName, p);

        Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);
        Txn<byte[]> txn = env.txnWrite();
        Cursor<byte[]> cursor = db.openCursor(txn);

        try {
            cursor.put(pickler.dumps(model.getK()), pickler.dumps(model.getV()));
        } finally {
            close(cursor, txn, db, env);
        }
    }

    @Override
    public <K, V> void putAll(String dbName, String tbName, List<DataItemModel<K, V>> list) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DataItemModel model : list) {
            put(dbName, tbName, model);
        }
    }

    @Override
    public DataItemModel get(String dbName, String tbName, String key) throws Exception {
        int p = hashKeyToPartition(key, lmdbParamConfig.getPartitions());
        Env<byte[]> env = getEnv(dbName, tbName, p);

        Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);
        Txn<byte[]> txn = env.txnRead();
        Cursor<byte[]> cursor = db.openCursor(txn);
        DataItemModel model = null;
        try {
            model = new DataItemModel();
            cursor.get(pickler.dumps(key), GetOp.MDB_SET_KEY);
            model.setK(unpickler.loads(cursor.key()));
            model.setV(unpickler.loads(cursor.val()));
        } finally {
            close(cursor, txn, db, env);
        }
        return model;
    }

    /**
     * get data by partition
     */
    public List<DataItemModel> getPartitionData(String dbName, String tbName, int partition) throws Exception {
        Env<byte[]> env = getEnvNotMkDirs(dbName, tbName, partition);
        List<DataItemModel> list = new ArrayList<>();
        if (env == null) {
            return list;
        }
        Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);
        Txn<byte[]> txn = env.txnRead();
        Cursor<byte[]> cursor = db.openCursor(txn);
        try {
            while (cursor.next()) {
                DataItemModel model = new DataItemModel();
                byte[] key = cursor.key();
                byte[] val = cursor.val();
                model.setK(unpickler.loads(key));
                model.setV(unpickler.loads(val));
                list.add(model);
            }
        } finally {
            close(cursor, txn, db, env);
        }
        return list;
    }

    @Override
    public List<DataItemModel> collect(String dbName, String tbName) throws Exception {
        List<DataItemModel> list = new ArrayList<>();

        for (int i = 0; i < lmdbParamConfig.getPartitions(); i++) {
            list.addAll(getPartitionData(dbName, tbName, i));
        }
        return list;
    }

    @Override
    public List<DataItemModel<byte[], byte[]>> collectBytes(String dbName, String tbName) throws Exception {
        List<DataItemModel<byte[], byte[]>> list = new ArrayList<>();
        for (int i = 0; i < lmdbParamConfig.getPartitions(); i++) {
            Env<byte[]> env = getEnvNotMkDirs(dbName, tbName, i);
            if (env == null) {
                continue;
            }
            Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);

            Txn<byte[]> txn = env.txnRead();
            Cursor<byte[]> cursor = db.openCursor(txn);
            try {
                while (cursor.next()) {
                    DataItemModel model = new DataItemModel();
                    byte[] key = cursor.key();
                    byte[] val = cursor.val();
                    model.setK(key);
                    model.setV(val);
                    list.add(model);
                }
            } finally {
                close(cursor, txn, db, env);
            }
        }
        return list;
    }

    @Override
    public void delete(String dbName, String tbName, String key) throws Exception {
        int p = hashKeyToPartition(key, lmdbParamConfig.getPartitions());
        Env<byte[]> env = getEnvNotMkDirs(dbName, tbName, p);
        if (env == null) {
            return;
        }

        Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);
        Txn<byte[]> txn = env.txnWrite();
        Cursor<byte[]> cursor = db.openCursor(txn);
        try {
            cursor.get(pickler.dumps(key), GetOp.MDB_SET_KEY);
            byte[] val = cursor.val();
            if (val != null && val.length > 0) {
                cursor.delete();
            }
        } finally {
            close(cursor, txn, db, env);
        }
    }

    @Override
    public int count(String dbName, String tbName) throws Exception {
        return collect(dbName, tbName).size();
    }

    @Override
    public List<DataItemModel> take(String dbName, String tbName, int size) throws Exception {
        if (size < 0) {
            size = 0;
        }
        List<DataItemModel> result = new ArrayList<>();
        List<DataItemModel> list = collect(dbName, tbName);
        for (int i = 0; i < list.size(); i++) {
            if (i == size) {
                break;
            }
            result.add(list.get(i));
        }

        return result;
    }

    /**
     * get data page by pageInputModel
     *
     * @param dbName         database name
     * @param tbName         table name
     * @param pageInputModel pageInputModel
     */
    @Override
    public PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) throws Exception {
        PageOutputModel pageOutputModel = new PageOutputModel();
        int total = count(dbName, tbName);
        List<DataItemModel> list = new ArrayList<>();

        pageOutputModel.setCurrentNum(pageInputModel.getPageNum());
        pageOutputModel.setTotalPages((total + pageInputModel.getPageSize() - 1) / pageInputModel.getPageSize());
        int start = pageInputModel.getPageNum() == 0 ? 0 : pageInputModel.getPageNum() * pageInputModel.getPageSize();

        for (int i = 0; i < lmdbParamConfig.getPartitions(); i++) {

            List<DataItemModel> partitionList = getPartitionData(dbName, tbName, i);

            if (list.size() == pageInputModel.getPageSize()) {
                break;
            }

            if (partitionList.size() <= start) {
                start = start - partitionList.size();
                continue;
            }

            Env<byte[]> env = getEnvNotMkDirs(dbName, tbName, i);
            if (env == null) {
                continue;
            }

            Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);
            Txn<byte[]> txn = env.txnRead();
            Cursor<byte[]> cursor = db.openCursor(txn);
            try {
                while (cursor.next()) {
                    DataItemModel model = new DataItemModel();
                    byte[] key = cursor.key();
                    byte[] val = cursor.val();
                    model.setK(unpickler.loads(key));
                    model.setV(unpickler.loads(val));
                    //Need to find the fetch position in the current shard
                    if (start > 0) {
                        start = start - 1;
                        continue;
                    }
                    list.add(model);
                    if (list.size() == pageInputModel.getPageSize()) {
                        pageOutputModel.setData(list);
                        break;
                    }
                }
            } finally {
                close(cursor, txn, db, env);
            }
        }
        return pageOutputModel;
    }

    @Override
    public PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) throws Exception {
        PageOutputModel pageOutputModel = new PageOutputModel();
        int total = count(dbName, tbName);
        List<DataItemModel> list = new ArrayList<>();

        pageOutputModel.setCurrentNum(pageInputModel.getPageNum());
        pageOutputModel.setTotalPages((total + pageInputModel.getPageSize() - 1) / pageInputModel.getPageSize());
        int start = pageInputModel.getPageNum() == 0 ? 0 : pageInputModel.getPageNum() * pageInputModel.getPageSize();

        for (int i = 0; i < lmdbParamConfig.getPartitions(); i++) {
            List<DataItemModel> partitionList = getPartitionData(dbName, tbName, i);
            if (list.size() == pageInputModel.getPageSize()) {
                break;
            }

            if (partitionList.size() <= start) {
                start = start - partitionList.size();
                continue;
            }

            Env<byte[]> env = getEnvNotMkDirs(dbName, tbName, i);
            if (env == null) {
                continue;
            }

            Dbi<byte[]> db = env.openDbi(dbName, MDB_CREATE);
            Txn<byte[]> txn = env.txnRead();
            Cursor<byte[]> cursor = db.openCursor(txn);
            try {
                while (cursor.next()) {
                    DataItemModel model = new DataItemModel();
                    byte[] key = cursor.key();
                    byte[] val = cursor.val();
                    model.setK(key);
                    model.setV(val);
                    if (start > 0) {
                        start = start - 1;
                        continue;
                    }
                    list.add(model);
                    if (list.size() == pageInputModel.getPageSize()) {
                        pageOutputModel.setData(list);
                        break;
                    }
                }
            } finally {
                close(cursor, txn, db, env);
            }
        }

        return pageOutputModel;
    }

    @Override
    public void dropTB(String dbName, String tbName) throws Exception {

        String path = lmdbParamConfig.getLmdbPath() + File.separator + dbName;
        File file = new File(path);
        deleteDir(file);
    }

    @Override
    public void dropDB(String dbName) throws Exception {

    }

    @Override
    public int getAddBatchSize(int columnCount) {
        return 10000;
    }

    @Override
    public boolean isExists(String dbName, String tbName) throws SQLException {
        return false;
    }

    private void close(Cursor<byte[]> cursor, Txn<byte[]> txn, Dbi<byte[]> dbi, Env<byte[]> env) {
        if (cursor != null) {
            cursor.close();
        }

        if (txn != null) {
            txn.commit();
            txn.close();
        }

        if (dbi != null) {
            dbi.close();
        }

        if (env != null) {
            env.close();
        }
    }

    /**
     * convert String to ByteBuffer
     */
    private ByteBuffer stringToByteBuffer(String value) {
        byte[] val = value.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb = allocateDirect(val.length);
        bb.put(val).flip();
        return bb;
    }

    private int hashKeyToPartition(String key, int partitions) {
        int p = key.hashCode() % partitions;
        return Math.abs(p);
    }

    public Env<byte[]> getEnv(String dbName, String tableName, int p) {
        String path = lmdbParamConfig.getLmdbPath() + File.separator + dbName + File.separator + tableName + File.separator + p;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        Env<byte[]> env = create(new ByteArrayProxy())
                .setMapSize(256 * 1024 * 1024)
                .setMaxDbs(10)
                .setMaxReaders(10 * 2)
                .open(file);
        return env;
    }

    public Env<byte[]> getEnvNotMkDirs(String dbName, String tableName, int p) {
        String path = lmdbParamConfig.getLmdbPath() + File.separator + dbName + File.separator + tableName + File.separator + p;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Env<byte[]> env = create(new ByteArrayProxy())
                .setMapSize(256 * 1024 * 1024)
                .setMaxDbs(10)
                .setMaxReaders(10 * 2)
                .open(file);
        return env;
    }

    /**
     * obtain the fragments in the file directory through the file path
     */
    public List<String> getPartitions(String dbName, String tbName) {
        String path = lmdbParamConfig.getLmdbPath() + File.separator + dbName + File.separator + tbName;
        File file = new File(path);
        String[] strs = file.list();
        List<String> list = new ArrayList<>();

        if (strs == null) {
            return list;
        }
        for (int i = 0; i < strs.length; i++) {
            try {
                Integer.parseInt(strs[i]);
                list.add(strs[i]);
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return list;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();

            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
