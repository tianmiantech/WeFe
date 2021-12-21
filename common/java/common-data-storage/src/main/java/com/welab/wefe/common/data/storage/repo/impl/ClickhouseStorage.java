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

package com.welab.wefe.common.data.storage.repo.impl;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.repo.AbstractStorage;
import net.razorvine.pickle.Pickler;
import net.razorvine.pickle.Unpickler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author yuxin.zhang
 */
@Component
public class ClickhouseStorage extends AbstractStorage {

    @Override
    public void put(String dbName, String tbName, DataItemModel model) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            checkTB(dbName, tbName);
            conn = getConnection();
            String sql = "INSERT INTO " + formatTableName(dbName, tbName) + " (eventDate,k,v,id) values(?,?,?,?)";
            statement = conn.prepareStatement(sql);
            Pickler pickler = new Pickler();
            statement.setDate(1, model.getEventDate());
            byte[] key = model.getK() instanceof byte[] ? (byte[]) model.getK() : pickler.dumps(model.getK());
            byte[] value = model.getV() instanceof byte[] ? (byte[]) model.getV() : pickler.dumps(model.getV());
            statement.setBytes(2, key);
            statement.setBytes(3, value);
            statement.setString(4, UUID.randomUUID().toString());
            statement.executeQuery();
        } finally {
            close(statement, conn);
        }
    }

    @Override
    public <K, V> void putAll(String dbName, String tbName, List<DataItemModel<K, V>> list) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            checkTB(dbName, tbName);
            conn = getConnection();
            conn.setAutoCommit(false);
            String sql = "INSERT INTO " + formatTableName(dbName, tbName) + " (eventDate,k,v,id) values(?,?,?,?)";
            statement = conn.prepareStatement(sql);
            Pickler pickler = new Pickler();
            for (DataItemModel<K, V> item : list) {
                statement.setDate(1, item.getEventDate());
                byte[] key = item.getK() instanceof byte[] ? (byte[]) item.getK() : pickler.dumps(item.getK());
                byte[] value = item.getV() instanceof byte[] ? (byte[]) item.getV() : pickler.dumps(item.getV());
                statement.setBytes(2, key);
                statement.setBytes(3, value);
                statement.setString(4, UUID.randomUUID().toString());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
        } finally {
            close(statement, conn);
        }
    }

    @Override
    public DataItemModel get(String dbName, String tbName, String key) throws Exception {
        DataItemModel model = null;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select * from " + formatTableName(dbName, tbName) + " where k=? order by id DESC";
            statement = conn.prepareStatement(sql);
            Pickler pickler = new Pickler();
            Unpickler unpickler = new Unpickler();
            statement.setBytes(1, pickler.dumps(key));
            rs = statement.executeQuery();
            if (rs.next()) {
                model = new DataItemModel();
                Date eventDate = rs.getDate(1);
                model.setEventDate(eventDate);
                model.setK(unpickler.loads(rs.getBytes(2)));
                model.setV(unpickler.loads(rs.getBytes(3)));
            }
        } finally {
            close(rs, statement, conn);
        }

        return model;
    }

    @Override
    public List<DataItemModel> collect(String dbName, String tbName) throws Exception {
        List<DataItemModel> list;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select * from " + formatTableName(dbName, tbName) + "  order by id DESC";
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();
            list = new ArrayList();
            Unpickler unpickler = new Unpickler();
            while (rs.next()) {
                DataItemModel model = new DataItemModel();
                Date eventDate = rs.getDate(1);
                model.setEventDate(eventDate);
                model.setK(unpickler.loads(rs.getBytes(2)));
                model.setV(unpickler.loads(rs.getBytes(3)));
                list.add(model);
            }
        } finally {
            close(rs, statement, conn);
        }

        return list;
    }


    @Override
    public List<DataItemModel<byte[], byte[]>> collectBytes(String dbName, String tbName) throws Exception {
        List<DataItemModel<byte[], byte[]>> list;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select * from " + formatTableName(dbName, tbName);
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();
            list = new ArrayList();
            while (rs.next()) {
                DataItemModel<byte[], byte[]> model = new DataItemModel();
                Date eventDate = rs.getDate(1);
                model.setEventDate(eventDate);
                model.setK(rs.getBytes(2));
                model.setV(rs.getBytes(3));
                list.add(model);
            }
        } finally {
            close(rs, statement, conn);
        }

        return list;
    }

    @Override
    public void delete(String dbName, String tbName, String key) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = "alter table " + formatTableName(dbName, tbName) + " delete where k=?";
            statement = conn.prepareStatement(sql);
            Pickler pickler = new Pickler();
            statement.setBytes(1, pickler.dumps(key));
            statement.executeQuery();
        } finally {
            close(statement, conn);
        }
    }

    @Override
    public int count(String dbName, String tbName) throws Exception {
        int result = 0;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select count(*) from " + formatTableName(dbName, tbName);
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } finally {
            close(rs, statement, conn);
        }
        return result;
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

    @Override
    public PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) throws Exception {
        PageOutputModel pageOutputModel = new PageOutputModel();
        int total = count(dbName, tbName);
        pageOutputModel.setTotal(total);

        List<DataItemModel> list;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select * from " + formatTableName(dbName, tbName) + "  order by id DESC limit ?,?";
            statement = conn.prepareStatement(sql);
            int start = pageInputModel.getPageNum() == 0 ? 0 : pageInputModel.getPageNum() * pageInputModel.getPageSize();
            statement.setInt(1, start);
            statement.setInt(2, pageInputModel.getPageSize());
            rs = statement.executeQuery();
            list = new ArrayList();
            Unpickler unpickler = new Unpickler();
            while (rs.next()) {
                DataItemModel model = new DataItemModel();
                Date eventDate = rs.getDate(1);
                model.setEventDate(eventDate);
                model.setK(unpickler.loads(rs.getBytes(2)));
                model.setV(unpickler.loads(rs.getBytes(3)));
                list.add(model);
            }
            pageOutputModel.setCurrentNum(pageInputModel.getPageNum());
            pageOutputModel.setTotalPages((total + pageInputModel.getPageSize() - 1) / pageInputModel.getPageSize());
            pageOutputModel.setData(list);
        } finally {
            close(rs, statement, conn);
        }

        return pageOutputModel;
    }


    @Override
    public PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) throws Exception {
        PageOutputModel<byte[], byte[]> pageOutputModel = new PageOutputModel();
        int total = count(dbName, tbName);
        pageOutputModel.setTotal(total);

        List<DataItemModel<byte[], byte[]>> list;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select * from " + formatTableName(dbName, tbName) + "  order by id DESC limit ?,?";
            statement = conn.prepareStatement(sql);
            int start = pageInputModel.getPageNum() == 0 ? 0 : pageInputModel.getPageNum() * pageInputModel.getPageSize();
            statement.setInt(1, start);
            statement.setInt(2, pageInputModel.getPageSize());
            rs = statement.executeQuery();
            list = new ArrayList();
            while (rs.next()) {
                DataItemModel model = new DataItemModel();
                Date eventDate = rs.getDate(1);
                model.setEventDate(eventDate);
                model.setK(rs.getBytes(2));
                model.setV(rs.getBytes(3));
                list.add(model);
            }
            pageOutputModel.setCurrentNum(pageInputModel.getPageNum());
            pageOutputModel.setTotalPages((total + pageInputModel.getPageSize() - 1) / pageInputModel.getPageSize());
            pageOutputModel.setData(list);
        } finally {
            close(rs, statement, conn);
        }
        return pageOutputModel;
    }

    @Override
    public void dropTB(String dbName, String tbName) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = "DROP TABLE " + formatTableName(dbName, tbName);
            statement = conn.prepareStatement(sql);
            statement.execute();
        } finally {
            close(statement, conn);
        }
    }

    @Override
    public void dropDB(String dbName) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = "DROP DATABASE " + String.format("`%s`", dbName);
            statement = conn.prepareStatement(sql);
            statement.execute();
        } finally {
            close(statement, conn);
        }
    }

    @Override
    public int getAddBatchSize(int columnCount) {
        return 150000 / columnCount;
    }

    @Override
    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "select * from " + formatTableName(dbName, tbName) + " limit 0,1";
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();
            if (rs.next()) {
                byte[] key = rs.getBytes(2);
                byte[] value = rs.getBytes(3);

                // The size of a single piece of data
                int unitSize = key.length + value.length;
                if (unitSize != 0) {
                    //Calculate the optimal page size
                    int count = BigDecimal.valueOf(byteSize)
                            .divide(BigDecimal.valueOf(unitSize), 0, RoundingMode.HALF_UP)
                            .intValue();
                    return count > 0 ? count : super.getCountByByteSize(dbName, tbName, byteSize);
                }
            }
        } finally {
            close(rs, statement, conn);
        }
        return super.getCountByByteSize(dbName, tbName, byteSize);
    }

    @Override
    public boolean isExists(String dbName, String tbName) throws SQLException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = String.format("SELECT  count(*) from system.parts p  where active  and database = '%s' and table ='%s'", dbName, tbName);
            statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;
//            System.out.println();
        } finally {
            close(statement, conn);
        }
//        return false;
    }
}
